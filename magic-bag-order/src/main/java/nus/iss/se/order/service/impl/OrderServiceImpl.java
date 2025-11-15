package nus.iss.se.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nus.iss.se.common.Result;
import nus.iss.se.common.constant.ResultStatus;
import nus.iss.se.common.cache.UserContext;
import nus.iss.se.order.api.CartClient;
import nus.iss.se.order.api.MerchantClient;
import nus.iss.se.order.api.ProductClient;
import nus.iss.se.order.api.UserClient;
import nus.iss.se.common.exception.BusinessException;
import nus.iss.se.order.constant.UserRole;
import nus.iss.se.order.dto.*;
import nus.iss.se.order.dto.MerchantDto;
import nus.iss.se.order.entity.Order;
import nus.iss.se.order.entity.OrderItem;
import nus.iss.se.order.entity.OrderVerification;
import nus.iss.se.order.mapper.OrderMapper;
import nus.iss.se.order.mapper.OrderItemMapper;
import nus.iss.se.order.mapper.OrderVerificationMapper;
import nus.iss.se.order.service.IOrderService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    private final OrderItemMapper orderItemMapper;
    private final ProductClient productClient;
    private final UserClient userClient;
    private final MerchantClient merchantClient;
    private final CartClient cartClient;

    @Override
    public IPage<OrderDto> getOrders(UserContext currentUser, OrderQueryDto queryDto) {
        String userRole = currentUser.getRole();
        Page<OrderDto> page = new Page<>(queryDto.getPageNum(), queryDto.getPageSize());

        IPage<OrderDto> orderPage;

        // 将字符串转换为枚举实例
        UserRole role = UserRole.getByCode(userRole)
                .orElseThrow(() -> new BusinessException(ResultStatus.ACCESS_DENIED));

        switch (role) {
            case SUPER_ADMIN, ADMIN:
                orderPage = orderMapper.findAllOrders(page);
                break;
            case MERCHANT:
                Integer merchantUserId = currentUser.getId();
                MerchantDto merchant = merchantClient.getMerchantById(merchantUserId).getData();
                if (merchant == null) {
                    throw new BusinessException(ResultStatus.USER_NOT_FOUND, "Merchant user context not found.");
                }
                orderPage = orderMapper.findByMerchantId(page, merchant.getId());
                break;
            case USER, CUSTOMER:
                orderPage = orderMapper.findByUserId(page, currentUser.getId());
                break;
            default:
                throw new BusinessException(ResultStatus.ACCESS_DENIED);
        }

        // 对查询结果进行处理，填充 OrderItems 和其他信息
        if (orderPage != null && orderPage.getRecords() != null) {
            orderPage.getRecords().forEach(orderDto -> {
                if ("cart".equalsIgnoreCase(orderDto.getOrderType())) {
                    List<OrderItem> items = orderItemMapper.findByOrderId(orderDto.getId());
                    orderDto.setOrderItems(items.stream()
                            .map(this::convertToOrderItemDto)
                            .toList());
                }
                if (orderDto.getBagId() != null) {
                    MagicBagDto bag = productClient.getMagicBagById(orderDto.getBagId()).getData();
                    if (bag != null) {
                        orderDto.setBagTitle(bag.getTitle());
                        MerchantDto m = merchantClient.getMerchantById(bag.getMerchantId()).getData();
                        if (m != null) {
                            orderDto.setMerchantName(m.getName());
                        }
                    }
                } else if ("cart".equalsIgnoreCase(orderDto.getOrderType()) && !CollectionUtils.isEmpty(orderDto.getOrderItems())){
                    OrderItemDto firstItemDto = orderDto.getOrderItems().getFirst();
                    if (firstItemDto != null && firstItemDto.getMagicBagId() != null) {
                        MagicBagDto bag = productClient.getMagicBagById(firstItemDto.getMagicBagId()).getData();
                        if (bag != null) {
                            orderDto.setBagTitle("Multiple Items");
                            MerchantDto m = merchantClient.getMerchantById(bag.getMerchantId()).getData();
                            if (m != null) {
                                orderDto.setMerchantName(m.getName());
                            }
                        }
                    }
                }
                UserDto user = userClient.getUserById(orderDto.getUserId()).getData();
                orderDto.setUserName(user != null &&user.getNickname() != null ? user.getNickname() : user.getUsername());
            });
        }

        return orderPage;
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
                if ("cart".equals(order.getOrderType())) {
                    // 多件商品订单：通过 order_items 验证权限
                    List<OrderItem> orderItems = orderItemMapper.findByOrderId(orderId);
                    boolean hasPermission = orderItems.stream()
                        .anyMatch(item -> {
                            try {
                                Result<MagicBagDto> bagResult = productClient.getMagicBagById(item.getMagicBagId());
                                return isResultSuccess(bagResult) && 
                                       bagResult.getData() != null &&
                                       bagResult.getData().getMerchantId().equals(currentUser.getId());
                            } catch (Exception e) {
                                log.warn("Error checking merchant permission for item {}: {}", item.getMagicBagId(), e.getMessage());
                                return false;
                            }
                        });
                    if (!hasPermission) {
                        log.error("Merchant {} attempted to access cart order {} without permission", 
                                currentUser.getId(), orderId);
                        throw new BusinessException(ResultStatus.PERMISSION_UNAUTHORIZED);
                    }
                    log.info("Merchant {} accessing their cart order: {}", currentUser.getId(), orderId);
                } else {
                    // 单件商品订单：使用原有逻辑
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
        if ("cart".equals(order.getOrderType())) {
            // 多件商品订单：通过 order_items 获取主要商品信息
            List<OrderItem> orderItems = orderItemMapper.findByOrderId(order.getId());
            if (!orderItems.isEmpty()) {
                OrderItem firstItem = orderItems.get(0);
                try {
                    Result<MagicBagDto> bagResult = productClient.getMagicBagById(firstItem.getMagicBagId());
                    if (isResultSuccess(bagResult) && bagResult.getData() != null) {
                        MagicBagDto bag = bagResult.getData();
                        OrderDetailResponse.MagicBagInfo bagInfo = new OrderDetailResponse.MagicBagInfo();
                        bagInfo.setId(bag.getId());
                        bagInfo.setTitle(bag.getTitle());
                        bagInfo.setDescription(bag.getDescription());
                        bagInfo.setCategory(bag.getCategory());
                        bagInfo.setImageUrl(bag.getImageUrl());
                        response.setMagicBag(bagInfo);
                        log.debug("Magic bag info added to cart order detail: bagId={}", bag.getId());
                        
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
                                    log.debug("Merchant info added to cart order detail: merchantId={}", merchant.getId());
                                } else {
                                    log.warn("Failed to get merchant info for cart order {}: merchantId={}", 
                                            order.getId(), bag.getMerchantId());
                                }
                            } catch (Exception e) {
                                log.error("Error fetching merchant info for cart order {}: {}", 
                                        order.getId(), e.getMessage());
                            }
                        }
                    } else {
                        log.warn("Failed to get magic bag info for cart order {}: bagId={}", 
                                order.getId(), firstItem.getMagicBagId());
                    }
                } catch (Exception e) {
                    log.error("Error fetching magic bag info for cart order {}: {}", order.getId(), e.getMessage());
                }
            }
        } else {
            // 单件商品订单：使用原有逻辑
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
    
    @Override
    @Transactional
    public OrderDto createOrderFromCart(Integer userId) {
        log.info("Creating order from cart for user: {}", userId);
        
        // 1. 通过 CartClient 获取购物车
        Result<CartDto> cartResult = cartClient.getActiveCart(userId);
        if (!isResultSuccess(cartResult) || cartResult.getData() == null) {
            throw new BusinessException(ResultStatus.DATA_IS_WRONG);
        }
        
        CartDto cart = cartResult.getData();
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new BusinessException(ResultStatus.DATA_IS_WRONG);
        }
        
        // 2. 计算总价
        BigDecimal totalPrice = cart.getItems().stream()
                .map(item -> BigDecimal.valueOf(item.getSubtotal()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 3. 创建主订单
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setOrderType("cart");
        order.setTotalPrice(totalPrice);
        order.setStatus("pending");
        order.setPickupCode(generatePickupCode());
        order.setCreatedAt(new Date());
        order.setUpdatedAt(new Date());
        
        // 设置 bag_id 和自提时间（使用第一个商品的信息）
        if (!cart.getItems().isEmpty()) {
            CartItemDto firstItem = cart.getItems().get(0);
            order.setBagId(firstItem.getMagicBagId());
            log.info("Set bag_id for cart order: {}", firstItem.getMagicBagId());
            
            try {
                Result<MagicBagDto> bagResult = productClient.getMagicBagById(firstItem.getMagicBagId());
                if (isResultSuccess(bagResult) && bagResult.getData() != null) {
                    // 转换LocalTime为Date（使用当前日期）
                    java.time.LocalDate today = java.time.LocalDate.now();
                    order.setPickupStartTime(java.sql.Date.valueOf(today));
                    order.setPickupEndTime(java.sql.Date.valueOf(today));
                }
            } catch (Exception e) {
                log.warn("Failed to get magic bag info for pickup time: {}", e.getMessage());
            }
        }
        
        orderMapper.insert(order);
        log.info("Main order created: orderId={}, orderNo={}", order.getId(), order.getOrderNo());
        
        // 4. 创建订单明细
        for (CartItemDto item : cart.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setMagicBagId(item.getMagicBagId());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setUnitPrice(BigDecimal.valueOf(item.getPrice()));
            orderItem.setSubtotal(BigDecimal.valueOf(item.getSubtotal()));
            orderItem.setCreatedAt(LocalDateTime.now());
            orderItemMapper.insert(orderItem);
        }
        
        log.info("Created {} order items for order: {}", cart.getItems().size(), order.getId());
        
        // 5. 通过 CartClient 清空购物车
        try {
            Result<Void> clearResult = cartClient.clearCart(userId);
            if (!isResultSuccess(clearResult)) {
                log.warn("Failed to clear cart for user: {}", userId);
            } else {
                log.info("Cart cleared successfully for user: {}", userId);
            }
        } catch (Exception e) {
            log.error("Error clearing cart for user {}: {}", userId, e.getMessage());
        }
        
        // 6. 返回订单信息
        return convertToOrderDto(order);
    }
    
    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        return "ORD" + System.currentTimeMillis();
    }
    
    /**
     * 生成自提码
     */
    private String generatePickupCode() {
        return String.valueOf((int) (Math.random() * 9000) + 1000);
    }
    
    /**
     * 转换订单实体为DTO
     */
    private OrderDto convertToOrderDto(Order order) {
        OrderDto dto = new OrderDto();
        BeanUtils.copyProperties(order, dto);
        
        if ("cart".equals(order.getOrderType())) {
            // 多件商品订单：查询订单明细
            List<OrderItem> orderItems = orderItemMapper.findByOrderId(order.getId());
            List<OrderItemDto> itemDtos = orderItems.stream()
                    .map(this::convertToOrderItemDto)
                    .collect(Collectors.toList());
            dto.setOrderItems(itemDtos);
            
            // 设置主要商品信息（用于显示）
            if (!orderItems.isEmpty()) {
                OrderItem firstItem = orderItems.get(0);
                try {
                    Result<MagicBagDto> bagResult = productClient.getMagicBagById(firstItem.getMagicBagId());
                    if (isResultSuccess(bagResult) && bagResult.getData() != null) {
                        MagicBagDto bag = bagResult.getData();
                        dto.setBagTitle(bag.getTitle());
                        
                        // 获取商户名称
                        if (bag.getMerchantId() != null) {
                            Result<MerchantDto> merchantResult = merchantClient.getMerchantById(bag.getMerchantId());
                            if (isResultSuccess(merchantResult) && merchantResult.getData() != null) {
                                dto.setMerchantName(merchantResult.getData().getName());
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("Failed to get bag info for cart order {}: {}", order.getId(), e.getMessage());
                }
            }
        } else {
            // 单件商品订单：使用原有逻辑
            // 这里可以添加单件商品订单的额外处理逻辑
        }
        
        return dto;
    }
    
    /**
     * 转换订单明细实体为DTO
     */
    private OrderItemDto convertToOrderItemDto(OrderItem orderItem) {
        OrderItemDto dto = new OrderItemDto();
        BeanUtils.copyProperties(orderItem, dto);
        
        // 查询商品信息
        try {
            Result<MagicBagDto> bagResult = productClient.getMagicBagById(orderItem.getMagicBagId());
            if (isResultSuccess(bagResult) && bagResult.getData() != null) {
                MagicBagDto magicBag = bagResult.getData();
                dto.setMagicBagTitle(magicBag.getTitle());
                dto.setMagicBagImageUrl(magicBag.getImageUrl());
                dto.setMagicBagCategory(magicBag.getCategory());
            }
        } catch (Exception e) {
            log.warn("Failed to get magic bag info for order item: {}", e.getMessage());
        }
        
        return dto;
    }
}