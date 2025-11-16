package nus.iss.se.order.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nus.iss.se.common.Result;
import nus.iss.se.common.cache.UserContext;
import nus.iss.se.order.common.UserContextHolder;
import nus.iss.se.order.dto.*;
import nus.iss.se.order.service.IOrderService;
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
    private final UserContextHolder userContextHolder;

    /**
     * 获取订单列表 - 根据角色自动过滤
     */
    @GetMapping
    @Operation(summary = "获取订单列表", description = "根据用户角色自动过滤订单数据")
    public Result<IPage<OrderDto>> getOrders(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        UserContext currentUser = userContextHolder.getCurrentUser();
        OrderQueryDto queryDto = OrderQueryDto.builder()
                .pageNum(pageNum)
                .pageSize(pageSize)
                .status(status)
                .startDate(startDate)
                .endDate(endDate)
                .build();

        IPage<OrderDto> orders = orderService.getOrders(currentUser, queryDto);
        return Result.success(orders);
    }

    /**
     * 获取订单详情 - 根据角色验证权限
     */
    @GetMapping("/{id}")
//    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MERCHANT', 'USER', 'CUSTOMER')")
    @Operation(summary = "获取订单详情", description = "根据用户角色验证权限后返回订单详情")
    public Result<OrderDetailResponse> getOrderDetail(@PathVariable Integer id) {
        UserContext currentUser = userContextHolder.getCurrentUser();
        OrderDetailResponse orderDetail = orderService.getOrderDetail(id, currentUser);
        return Result.success(orderDetail);
    }

    /**
     * 更新订单状态 - 管理员和商家可以操作
     */
    @PutMapping("/{id}/status")
//    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MERCHANT')")
    @Operation(summary = "更新订单状态", description = "管理员和商家可以更新订单状态")
    public Result<Void> updateOrderStatus(
            @PathVariable Integer id,
            @RequestBody @Valid OrderStatusUpdateDto statusDto) {

        UserContext currentUser = userContextHolder.getCurrentUser();
        orderService.updateOrderStatus(id, statusDto, currentUser);
        return Result.success();
    }

    /**
     * 取消订单 - 用户和管理员可以操作
     */
    @PutMapping("/{id}/cancel")
    @Operation(summary = "取消订单", description = "用户和管理员可以取消订单")
    public Result<Void> cancelOrder(@PathVariable Integer id) {
        UserContext currentUser = userContextHolder.getCurrentUser();
        orderService.cancelOrder(id, currentUser);
        return Result.success();
    }

    /**
     * 核销订单 - 只有商家可以操作
     */
    @PostMapping("/{id}/verify")
    @Operation(summary = "核销订单", description = "商家核销订单")
    public Result<Void> verifyOrder(
            @PathVariable Integer id,
            @RequestBody @Valid OrderVerificationDto verificationDto) {

        UserContext currentUser = userContextHolder.getCurrentUser();
        orderService.verifyOrder(id, verificationDto, currentUser);
        return Result.success();
    }

    /**
     * 获取订单统计信息
     */
    @GetMapping("/stats")
    @Operation(summary = "获取订单统计", description = "根据用户角色返回相应的订单统计信息")
    public Result<OrderStatsDto> getOrderStats() {
        UserContext currentUser = userContextHolder.getCurrentUser();
        OrderStatsDto stats = orderService.getOrderStats(currentUser);
        return Result.success(stats);
    }

    /**
     * 从购物车创建订单
     */
    @PostMapping("/from-cart")
    @Operation(summary = "从购物车创建订单", description = "将购物车中的商品转换为订单")
    public Result<OrderDto> createOrderFromCart() {
        UserContext currentUser = userContextHolder.getCurrentUser();
        OrderDto order = orderService.createOrderFromCart(currentUser.getId());
        return Result.success(order);
    }
}