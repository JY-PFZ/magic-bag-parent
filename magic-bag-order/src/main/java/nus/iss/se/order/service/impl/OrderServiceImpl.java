package nus.iss.se.order.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nus.iss.se.common.Result;
import nus.iss.se.common.constant.ResultStatus;
import nus.iss.se.common.cache.UserContext;
import nus.iss.se.order.api.MerchantClient;
import nus.iss.se.order.api.ProductClient;
import nus.iss.se.order.api.UserClient;
import nus.iss.se.common.exception.BusinessException;
import nus.iss.se.order.dto.*;
import nus.iss.se.order.entity.Order;
import nus.iss.se.order.entity.OrderVerification;
import nus.iss.se.order.mapper.OrderMapper;
import nus.iss.se.order.mapper.OrderVerificationMapper;
import nus.iss.se.order.service.IOrderService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements IOrderService {
    
    private final OrderMapper orderMapper;
    private final OrderVerificationMapper orderVerificationMapper;
    private final ProductClient productClient;
    private final UserClient userClient;
    private final MerchantClient merchantClient;
    
    @Override
    public IPage<OrderDto> getOrders(UserContext currentUser, OrderQueryDto queryDto) {
        String userRole = currentUser.getRole();
        Page<OrderDto> page = new Page<>(queryDto.getPageNum(), queryDto.getPageSize());
        
        log.info("Fetching orders: role={}, userId={}, page={}, size={}", 
                userRole, currentUser.getId(), queryDto.getPageNum(), queryDto.getPageSize());
        
        IPage<OrderDto> result;
        switch (userRole) {
            case "SUPER_ADMIN":
            case "ADMIN":
                result = orderMapper.findAllOrders(page);
                log.info("Admin fetched {} orders", result.getRecords().size());
                break;
            case "MERCHANT":
                result = orderMapper.findByMerchantId(page, currentUser.getId());
                log.info("Merchant {} fetched {} orders", currentUser.getId(), result.getRecords().size());
                break;
            case "USER":
                result = orderMapper.findByUserId(page, currentUser.getId());
                log.info("User {} fetched {} orders", currentUser.getId(), result.getRecords().size());
                break;
            default:
                log.error("Invalid user role: {}", userRole);
                throw new BusinessException(ResultStatus.PERMISSION_UNAUTHORIZED);
        }
        
        return result;
    }
    
    @Override
    public OrderDetailResponse getOrderDetail(Integer orderId, UserContext currentUser) {
        log.info("Fetching order detail: orderId={}, userId={}, role={}", 
                orderId, currentUser.getId(), currentUser.getRole());
        
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            log.error("Order not found: {}", orderId);
            throw new BusinessException(ResultStatus.ORDER_NOT_FOUND);
        }
        
        String userRole = currentUser.getRole();
        
        // 权限验证
        switch (userRole) {
            case "SUPER_ADMIN":
            case "ADMIN":
                log.info("Admin accessing order: {}", orderId);
                break;
            case "MERCHANT":
                if (order.getBagId() != null) {
                    Result<MagicBagDto> bagResult = productClient.getMagicBagById(order.getBagId());
                    if (!isResultSuccess(bagResult) || bagResult.getData() == null) {
                        log.error("Product not found when verifying merchant access: bagId={}", order.getBagId());
                        throw new BusinessException(ResultStatus.PRODUCT_NOT_FOUND);
                    }
                    if (!bagResult.getData().getMerchantId().equals(currentUser.getId())) {
                        log.error("Merchant {} attempted to access order {} of another merchant", 
                                currentUser.getId(), orderId);
                        throw new BusinessException(ResultStatus.PERMISSION_UNAUTHORIZED);
                    }
                    log.info("Merchant {} accessing their order: {}", currentUser.getId(), orderId);
                }
                break;
            case "USER":
                if (!order.getUserId().equals(currentUser.getId())) {
                    log.error("User {} attempted to access order {} of another user", 
                            currentUser.getId(), orderId);
                    throw new BusinessException(ResultStatus.PERMISSION_UNAUTHORIZED);
                }
                log.info("User {} accessing their order: {}", currentUser.getId(), orderId);
                break;
            default:
                log.error("Invalid user role: {}", userRole);
                throw new BusinessException(ResultStatus.PERMISSION_UNAUTHORIZED);
        }
        
        return buildOrderDetailResponse(order);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOrderStatus(Integer orderId, OrderStatusUpdateDto statusDto, UserContext currentUser) {
        log.info("Updating order status: orderId={}, newStatus={}, userId={}, role={}", 
                orderId, statusDto.getStatus(), currentUser.getId(), currentUser.getRole());
        
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            log.error("Order not found: {}", orderId);
            throw new BusinessException(ResultStatus.ORDER_NOT_FOUND);
        }
        
        String userRole = currentUser.getRole();
        
        // 权限验证
        if ("MERCHANT".equals(userRole)) {
            if (order.getBagId() != null) {
                Result<MagicBagDto> bagResult = productClient.getMagicBagById(order.getBagId());
                if (!isResultSuccess(bagResult) || bagResult.getData() == null) {
                    log.error("Product not found when updating order: bagId={}", order.getBagId());
                    throw new BusinessException(ResultStatus.PRODUCT_NOT_FOUND);
                }
                if (!bagResult.getData().getMerchantId().equals(currentUser.getId())) {
                    log.error("Merchant {} attempted to update order {} of another merchant", 
                            currentUser.getId(), orderId);
                    throw new BusinessException(ResultStatus.PERMISSION_UNAUTHORIZED);
                }
            }
        } else if (!"SUPER_ADMIN".equals(userRole) && !"ADMIN".equals(userRole)) {
            log.error("User {} with role {} attempted to update order status", 
                    currentUser.getId(), userRole);
            throw new BusinessException(ResultStatus.PERMISSION_UNAUTHORIZED);
        }
        
        doUpdateOrderStatus(order, statusDto.getStatus());
    }
    
    /**
     * 内部方法：直接更新订单状态（不需要权限检查）
     * 这个方法被 payment-service 通过 Feign 调用时使用
     * @param orderId 订单ID
     * @param newStatus 新状态
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOrderStatusInternal(Integer orderId, String newStatus) {
        log.info("Updating order status internally: orderId={}, newStatus={}", orderId, newStatus);
        
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            log.error("Order not found: {}", orderId);
            throw new BusinessException(ResultStatus.ORDER_NOT_FOUND);
        }
        
        doUpdateOrderStatus(order, newStatus);
    }
    
    /**
     * 实际执行状态更新的方法
     */
    private void doUpdateOrderStatus(Order order, String newStatus) {
        String oldStatus = order.getStatus();
        
        order.setStatus(newStatus);
        order.setUpdatedAt(new Date());
        
        // 根据状态设置相应的时间字段
        switch (newStatus) {
            case "paid":
                order.setPaidAt(new Date());
                log.info("Order {} status updated to 'paid'", order.getId());
                break;
            case "completed":
                order.setCompletedAt(new Date());
                log.info("Order {} status updated to 'completed'", order.getId());
                break;
            case "cancelled":
                order.setCancelledAt(new Date());
                log.info("Order {} status updated to 'cancelled'", order.getId());
                break;
            default:
                log.info("Order {} status updated from '{}' to '{}'", order.getId(), oldStatus, newStatus);
        }
        
        int rows = orderMapper.updateById(order);
        if (rows > 0) {
            log.info("Order status updated successfully: orderId={}, oldStatus={}, newStatus={}", 
                    order.getId(), oldStatus, newStatus);
        } else {
            log.error("Failed to update order status: orderId={}", order.getId());
            throw new BusinessException(ResultStatus.SERVICE_ERROR);
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Integer orderId, UserContext currentUser) {
        log.info("Cancelling order: orderId={}, userId={}, role={}", 
                orderId, currentUser.getId(), currentUser.getRole());
        
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            log.error("Order not found: {}", orderId);
            throw new BusinessException(ResultStatus.ORDER_NOT_FOUND);
        }
        
        String userRole = currentUser.getRole();
        
        // 权限验证
        if ("USER".equals(userRole)) {
            if (!order.getUserId().equals(currentUser.getId())) {
                log.error("User {} attempted to cancel order {} of another user", 
                        currentUser.getId(), orderId);
                throw new BusinessException(ResultStatus.PERMISSION_UNAUTHORIZED);
            }
        } else if (!"SUPER_ADMIN".equals(userRole) && !"ADMIN".equals(userRole)) {
            log.error("User {} with role {} attempted to cancel order", 
                    currentUser.getId(), userRole);
            throw new BusinessException(ResultStatus.PERMISSION_UNAUTHORIZED);
        }
        
        // 检查订单状态
        if ("completed".equals(order.getStatus())) {
            log.error("Attempted to cancel completed order: {}", orderId);
            throw new BusinessException(ResultStatus.ORDER_CANNOT_CANCEL);
        }
        if ("cancelled".equals(order.getStatus())) {
            log.warn("Order already cancelled: {}", orderId);
            throw new BusinessException(ResultStatus.ORDER_ALREADY_CANCELLED);
        }
        
        // 取消订单
        String oldStatus = order.getStatus();
        order.setStatus("cancelled");
        order.setCancelledAt(new Date());
        order.setUpdatedAt(new Date());
        
        int rows = orderMapper.updateById(order);
        if (rows > 0) {
            log.info("Order cancelled successfully: orderId={}, oldStatus={}", orderId, oldStatus);
        } else {
            log.error("Failed to cancel order: orderId={}", orderId);
            throw new BusinessException(ResultStatus.SERVICE_ERROR);
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void verifyOrder(Integer orderId, OrderVerificationDto verificationDto, UserContext currentUser) {
        log.info("Verifying order: orderId={}, merchantId={}, location={}", 
                orderId, currentUser.getId(), verificationDto.getLocation());
        
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            log.error("Order not found: {}", orderId);
            throw new BusinessException(ResultStatus.ORDER_NOT_FOUND);
        }
        
        // 只有商家可以核销订单
        if (!"MERCHANT".equals(currentUser.getRole())) {
            log.error("Non-merchant user {} attempted to verify order", currentUser.getId());
            throw new BusinessException(ResultStatus.PERMISSION_UNAUTHORIZED);
        }
        
        // 验证商家权限
        if (order.getBagId() != null) {
            Result<MagicBagDto> bagResult = productClient.getMagicBagById(order.getBagId());
            if (!isResultSuccess(bagResult) || bagResult.getData() == null) {
                log.error("Product not found when verifying order: bagId={}", order.getBagId());
                throw new BusinessException(ResultStatus.PRODUCT_NOT_FOUND);
            }
            if (!bagResult.getData().getMerchantId().equals(currentUser.getId())) {
                log.error("Merchant {} attempted to verify order {} of another merchant", 
                        currentUser.getId(), orderId);
                throw new BusinessException(ResultStatus.PERMISSION_UNAUTHORIZED);
            }
        }
        
        // 检查订单状态
        if (!"paid".equals(order.getStatus())) {
            log.error("Attempted to verify order with status '{}': orderId={}", order.getStatus(), orderId);
            throw new BusinessException(ResultStatus.ORDER_CANNOT_VERIFY);
        }
        
        // 创建核销记录
        OrderVerification verification = new OrderVerification();
        verification.setOrderId(orderId);
        verification.setVerifiedBy(currentUser.getId());
        verification.setVerifiedAt(new Date());
        verification.setLocation(verificationDto.getLocation());
        
        int verificationRows = orderVerificationMapper.insert(verification);
        if (verificationRows <= 0) {
            log.error("Failed to create verification record for order: {}", orderId);
            throw new BusinessException(ResultStatus.SERVICE_ERROR);
        }
        log.info("Verification record created: orderId={}, verificationId={}", orderId, verification.getId());
        
        // 更新订单状态为已完成
        order.setStatus("completed");
        order.setCompletedAt(new Date());
        order.setUpdatedAt(new Date());
        
        int orderRows = orderMapper.updateById(order);
        if (orderRows > 0) {
            log.info("Order verified and completed: orderId={}, merchantId={}", orderId, currentUser.getId());
        } else {
            log.error("Failed to update order status after verification: orderId={}", orderId);
            throw new BusinessException(ResultStatus.SERVICE_ERROR);
        }
    }
    
    @Override
    public OrderStatsDto getOrderStats(UserContext currentUser) {
        String userRole = currentUser.getRole();
        
        log.info("Fetching order stats: userId={}, role={}", currentUser.getId(), userRole);
        
        OrderStatsDto stats;
        switch (userRole) {
            case "SUPER_ADMIN":
            case "ADMIN":
                stats = orderMapper.findAllOrderStats();
                log.info("Admin fetched all order stats");
                break;
            case "MERCHANT":
                stats = orderMapper.findOrderStatsByMerchantId(currentUser.getId());
                log.info("Merchant {} fetched order stats", currentUser.getId());
                break;
            case "USER":
                stats = orderMapper.findOrderStatsByUserId(currentUser.getId());
                log.info("User {} fetched order stats", currentUser.getId());
                break;
            default:
                log.error("Invalid user role: {}", userRole);
                throw new BusinessException(ResultStatus.PERMISSION_UNAUTHORIZED);
        }
        
        return stats;
    }
    
    /**
     * 构建订单详情响应
     */
    private OrderDetailResponse buildOrderDetailResponse(Order order) {
        log.debug("Building order detail response for order: {}", order.getId());
        
        OrderDetailResponse response = new OrderDetailResponse();
        
        // 构建订单基本信息
        OrderDto orderDto = new OrderDto();
        BeanUtils.copyProperties(order, orderDto);
        response.setOrder(orderDto);
        
        // 查询用户信息（通过 User 服务）
        if (order.getUserId() != null) {
            try {
                Result<UserDto> userResult = userClient.getUserById(order.getUserId());
                if (isResultSuccess(userResult) && userResult.getData() != null) {
                    OrderDetailResponse.UserInfo userInfo = new OrderDetailResponse.UserInfo();
                    UserDto user = userResult.getData();
                    userInfo.setId(user.getId());
                    userInfo.setNickname(user.getNickname());
                    userInfo.setPhone(user.getPhone());
                    response.setUser(userInfo);
                    log.debug("User info added to order detail: userId={}", user.getId());
                } else {
                    log.warn("Failed to get user info for order {}: userId={}", 
                            order.getId(), order.getUserId());
                }
            } catch (Exception e) {
                log.error("Error fetching user info for order {}: {}", order.getId(), e.getMessage());
            }
        }
        
        // 查询魔法袋信息（通过 Product 服务）
        if (order.getBagId() != null) {
            try {
                Result<MagicBagDto> bagResult = productClient.getMagicBagById(order.getBagId());
                if (isResultSuccess(bagResult) && bagResult.getData() != null) {
                    MagicBagDto bag = bagResult.getData();
                    OrderDetailResponse.MagicBagInfo bagInfo = new OrderDetailResponse.MagicBagInfo();
                    bagInfo.setId(bag.getId());
                    bagInfo.setTitle(bag.getTitle());
                    bagInfo.setDescription(bag.getDescription());
                    bagInfo.setCategory(bag.getCategory());
                    bagInfo.setImageUrl(bag.getImageUrl());
                    response.setMagicBag(bagInfo);
                    log.debug("Magic bag info added to order detail: bagId={}", bag.getId());
                    
                    // 查询商家信息（通过 Merchant 服务）
                    if (bag.getMerchantId() != null) {
                        try {
                            Result<MerchantDto> merchantResult = merchantClient.getMerchantById(bag.getMerchantId());
                            if (isResultSuccess(merchantResult) && merchantResult.getData() != null) {
                                MerchantDto merchant = merchantResult.getData();
                                OrderDetailResponse.MerchantInfo merchantInfo = new OrderDetailResponse.MerchantInfo();
                                merchantInfo.setId(merchant.getId());
                                merchantInfo.setName(merchant.getName());
                                merchantInfo.setPhone(merchant.getPhone());
                                merchantInfo.setAddress(merchant.getAddress());
                                response.setMerchant(merchantInfo);
                                log.debug("Merchant info added to order detail: merchantId={}", merchant.getId());
                            } else {
                                log.warn("Failed to get merchant info for order {}: merchantId={}", 
                                        order.getId(), bag.getMerchantId());
                            }
                        } catch (Exception e) {
                            log.error("Error fetching merchant info for order {}: {}", 
                                    order.getId(), e.getMessage());
                        }
                    }
                } else {
                    log.warn("Failed to get magic bag info for order {}: bagId={}", 
                            order.getId(), order.getBagId());
                }
            } catch (Exception e) {
                log.error("Error fetching magic bag info for order {}: {}", order.getId(), e.getMessage());
            }
        }
        
        // 查询核销记录
        try {
            List<OrderVerification> verifications = orderVerificationMapper.findByOrderId(order.getId());
            List<OrderVerificationDto> verificationDtos = verifications.stream()
                    .map(this::convertToVerificationDto)
                    .collect(Collectors.toList());
            response.setVerifications(verificationDtos);
            log.debug("Added {} verification records to order detail", verificationDtos.size());
        } catch (Exception e) {
            log.error("Error fetching verification records for order {}: {}", order.getId(), e.getMessage());
            response.setVerifications(List.of());
        }
        
        log.info("Order detail response built successfully: orderId={}", order.getId());
        return response;
    }
    
    /**
     * 转换核销记录为DTO
     */
    private OrderVerificationDto convertToVerificationDto(OrderVerification verification) {
        OrderVerificationDto dto = new OrderVerificationDto();
        BeanUtils.copyProperties(verification, dto);
        return dto;
    }
    
    /**
     * 判断 Result 是否成功的辅助方法
     */
    private boolean isResultSuccess(Result<?> result) {
        return result != null && result.getCode() == ResultStatus.SUCCESS.getCode();
    }
}