package nus.iss.se.order.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nus.iss.se.common.Result;
import nus.iss.se.common.cache.UserContext;
import nus.iss.se.order.dto.*;
import nus.iss.se.order.service.IOrderService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 订单控制器
 */
@Slf4j
@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
@Tag(name = "Order API", description = "订单管理服务")
public class OrderController {
    
    private final IOrderService orderService;
    
    /**
     * 获取订单列表 - 根据角色自动过滤
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MERCHANT', 'USER')")
    @Operation(summary = "获取订单列表", description = "根据用户角色自动过滤订单数据")
    public Result<IPage<OrderDto>> getOrders(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        log.info("Getting orders: pageNum={}, pageSize={}, status={}", pageNum, pageSize, status);
        
        try {
            // 从 Security Context 中获取当前用户
            UserContext currentUser = getCurrentUser();
            
            OrderQueryDto queryDto = OrderQueryDto.builder()
                    .pageNum(pageNum)
                    .pageSize(pageSize)
                    .status(status)
                    .startDate(startDate)
                    .endDate(endDate)
                    .build();
            
            IPage<OrderDto> orders = orderService.getOrders(currentUser, queryDto);
            log.info("Retrieved {} orders", orders.getRecords().size());
            return Result.success(orders);
        } catch (Exception e) {
            log.error("Error getting orders: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 获取订单详情 - 根据角色验证权限
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MERCHANT', 'USER')")
    @Operation(summary = "获取订单详情", description = "根据用户角色验证权限后返回订单详情")
    public Result<OrderDetailResponse> getOrderDetail(@PathVariable Integer id) {
        log.info("Getting order detail: orderId={}", id);
        
        try {
            UserContext currentUser = getCurrentUser();
            OrderDetailResponse orderDetail = orderService.getOrderDetail(id, currentUser);
            log.info("Retrieved order detail: orderId={}", id);
            return Result.success(orderDetail);
        } catch (Exception e) {
            log.error("Error getting order detail: orderId={}, error={}", id, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 更新订单状态 - 管理员和商家可以操作
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MERCHANT')")
    @Operation(summary = "更新订单状态", description = "管理员和商家可以更新订单状态")
    public Result<Void> updateOrderStatus(
            @PathVariable Integer id,
            @RequestBody @Valid OrderStatusUpdateDto statusDto) {
        
        log.info("Updating order status: orderId={}, newStatus={}", id, statusDto.getStatus());
        
        try {
            UserContext currentUser = getCurrentUser();
            orderService.updateOrderStatus(id, statusDto, currentUser);
            log.info("Order status updated successfully: orderId={}", id);
            return Result.success();
        } catch (Exception e) {
            log.error("Error updating order status: orderId={}, error={}", id, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 取消订单 - 用户和管理员可以操作
     */
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'USER')")
    @Operation(summary = "取消订单", description = "用户和管理员可以取消订单")
    public Result<Void> cancelOrder(@PathVariable Integer id) {
        log.info("Cancelling order: orderId={}", id);
        
        try {
            UserContext currentUser = getCurrentUser();
            orderService.cancelOrder(id, currentUser);
            log.info("Order cancelled successfully: orderId={}", id);
            return Result.success();
        } catch (Exception e) {
            log.error("Error cancelling order: orderId={}, error={}", id, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 核销订单 - 只有商家可以操作
     */
    @PostMapping("/{id}/verify")
    @PreAuthorize("hasRole('MERCHANT')")
    @Operation(summary = "核销订单", description = "商家核销订单")
    public Result<Void> verifyOrder(
            @PathVariable Integer id,
            @RequestBody @Valid OrderVerificationDto verificationDto) {
        
        log.info("Verifying order: orderId={}", id);
        
        try {
            UserContext currentUser = getCurrentUser();
            orderService.verifyOrder(id, verificationDto, currentUser);
            log.info("Order verified successfully: orderId={}", id);
            return Result.success();
        } catch (Exception e) {
            log.error("Error verifying order: orderId={}, error={}", id, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 获取订单统计信息
     */
    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MERCHANT', 'USER')")
    @Operation(summary = "获取订单统计", description = "根据用户角色返回相应的订单统计信息")
    public Result<OrderStatsDto> getOrderStats() {
        log.info("Getting order stats");
        
        try {
            UserContext currentUser = getCurrentUser();
            OrderStatsDto stats = orderService.getOrderStats(currentUser);
            log.info("Retrieved order stats for user: {}", currentUser.getId());
            return Result.success(stats);
        } catch (Exception e) {
            log.error("Error getting order stats: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 内部接口：更新订单状态（供 payment-service 通过 Feign 调用）
     * 不需要权限验证，不需要 UserContext
     */
    @PutMapping("/{id}/status/internal")
    @Operation(summary = "内部接口：更新订单状态", description = "供 payment-service 调用，无需权限验证")
    public Result<Void> updateOrderStatusInternal(
            @PathVariable Integer id,
            @RequestParam String status) {
        
        log.info("Updating order status (internal): orderId={}, newStatus={}", id, status);
        
        try {
            orderService.updateOrderStatusInternal(id, status);
            log.info("Order status updated successfully (internal): orderId={}", id);
            return Result.success();
        } catch (Exception e) {
            log.error("Error updating order status (internal): orderId={}, error={}", id, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 获取当前用户信息
     * 从 Spring Security Context 中获取用户信息
     */
    private UserContext getCurrentUser() {
        
        throw new RuntimeException("Need to implement getCurrentUser() based on your auth framework");
    }
}