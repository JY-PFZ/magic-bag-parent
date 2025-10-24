package nus.iss.se.order.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import nus.iss.se.common.cache.UserContext;
import nus.iss.se.order.dto.*;

/**
 * 订单服务接口
 */
public interface IOrderService {

    /**
     * 获取订单列表（支持分页和权限控制）
     * @param currentUser 当前用户信息
     * @param queryDto 查询参数（包括分页信息）
     * @return 订单分页数据
     */
    IPage<OrderDto> getOrders(UserContext currentUser, OrderQueryDto queryDto);

    /**
     * 获取订单详情
     * @param orderId 订单ID
     * @param currentUser 当前用户信息
     * @return 订单详情响应
     */
    OrderDetailResponse getOrderDetail(Integer orderId, UserContext currentUser);

    /**
     * 更新订单状态
     * @param orderId 订单ID
     * @param statusDto 订单状态更新信息
     * @param currentUser 当前用户信息
     */
    void updateOrderStatus(Integer orderId, OrderStatusUpdateDto statusDto, UserContext currentUser);

    /**
     * 取消订单
     * @param orderId 订单ID
     * @param currentUser 当前用户信息
     */
    void cancelOrder(Integer orderId, UserContext currentUser);

    /**
     * 核销订单（商家核销已支付订单）
     * @param orderId 订单ID
     * @param verificationDto 核销信息
     * @param currentUser 当前用户信息
     */
    void verifyOrder(Integer orderId, OrderVerificationDto verificationDto, UserContext currentUser);

    /**
     * 获取订单统计数据
     * @param currentUser 当前用户信息
     * @return 订单统计数据
     */
    OrderStatsDto getOrderStats(UserContext currentUser);
    
    /**
     * 从购物车创建订单
     * @param userId 用户ID
     * @return 创建的订单信息
     */
    OrderDto createOrderFromCart(Integer userId);
    
    void updateOrderStatusInternal(Integer orderId, String newStatus);
}