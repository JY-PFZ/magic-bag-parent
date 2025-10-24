package nus.iss.se.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import nus.iss.se.order.dto.OrderDto;
import nus.iss.se.order.dto.OrderStatsDto;
import nus.iss.se.order.entity.Order;
import org.apache.ibatis.annotations.*;

/**
 * 订单 Mapper
 * 继承 BaseMapper 自动获得基础 CRUD 方法
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {
    
    /**
     * 查询所有订单（分页）
     */
    @Select("SELECT o.id, o.order_no, o.user_id, o.bag_id, o.quantity, " +
            "o.total_price, o.status, o.pickup_code, o.pickup_start_time, " +
            "o.pickup_end_time, o.created_at, o.paid_at, o.completed_at, o.cancelled_at, o.order_type, " +
            "u.nickname as user_name, " +
            "CASE " +
            "    WHEN o.order_type = 'cart' THEN " +
            "        (SELECT mb.title FROM order_items oi " +
            "         JOIN magic_bags mb ON oi.magic_bag_id = mb.id " +
            "         WHERE oi.order_id = o.id LIMIT 1) " +
            "    ELSE mb.title " +
            "END as bag_title, " +
            "CASE " +
            "    WHEN o.order_type = 'cart' THEN " +
            "        (SELECT m.name FROM order_items oi " +
            "         JOIN magic_bags mb ON oi.magic_bag_id = mb.id " +
            "         JOIN merchants m ON mb.merchant_id = m.id " +
            "         WHERE oi.order_id = o.id LIMIT 1) " +
            "    ELSE m.name " +
            "END as merchant_name " +
            "FROM orders o " +
            "LEFT JOIN users u ON o.user_id = u.id " +
            "LEFT JOIN magic_bags mb ON o.bag_id = mb.id " +
            "LEFT JOIN merchants m ON mb.merchant_id = m.id " +
            "ORDER BY o.created_at DESC")
    IPage<OrderDto> findAllOrders(Page<OrderDto> page);
    
    /**
     * 根据商家ID查询订单（分页）
     * 支持单件商品订单和多件商品订单
     */
    @Select("SELECT o.id, o.order_no, o.user_id, o.bag_id, o.quantity, " +
            "o.total_price, o.status, o.pickup_code, o.created_at, o.order_type, " +
            "u.nickname as user_name, " +
            "CASE " +
            "    WHEN o.order_type = 'cart' THEN " +
            "        (SELECT mb.title FROM order_items oi " +
            "         JOIN magic_bags mb ON oi.magic_bag_id = mb.id " +
            "         WHERE oi.order_id = o.id LIMIT 1) " +
            "    ELSE mb.title " +
            "END as bag_title, " +
            "CASE " +
            "    WHEN o.order_type = 'cart' THEN " +
            "        (SELECT m.name FROM order_items oi " +
            "         JOIN magic_bags mb ON oi.magic_bag_id = mb.id " +
            "         JOIN merchants m ON mb.merchant_id = m.id " +
            "         WHERE oi.order_id = o.id LIMIT 1) " +
            "    ELSE m.name " +
            "END as merchant_name " +
            "FROM orders o " +
            "LEFT JOIN users u ON o.user_id = u.id " +
            "LEFT JOIN magic_bags mb ON o.bag_id = mb.id " +
            "LEFT JOIN merchants m ON mb.merchant_id = m.id " +
            "WHERE (o.order_type = 'single' AND mb.merchant_id = #{merchantId}) " +
            "   OR (o.order_type = 'cart' AND EXISTS " +
            "       (SELECT 1 FROM order_items oi2 " +
            "        JOIN magic_bags mb2 ON oi2.magic_bag_id = mb2.id " +
            "        WHERE oi2.order_id = o.id AND mb2.merchant_id = #{merchantId})) " +
            "ORDER BY o.created_at DESC")
    IPage<OrderDto> findByMerchantId(Page<OrderDto> page, @Param("merchantId") Integer merchantId);
    
    /**
     * 根据用户ID查询订单（分页）
     */
    @Select("SELECT o.id, o.order_no, o.user_id, o.bag_id, o.quantity, " +
            "o.total_price, o.status, o.pickup_code, o.created_at, o.order_type, " +
            "u.nickname as user_name, " +
            "CASE " +
            "    WHEN o.order_type = 'cart' THEN " +
            "        (SELECT mb.title FROM order_items oi " +
            "         JOIN magic_bags mb ON oi.magic_bag_id = mb.id " +
            "         WHERE oi.order_id = o.id LIMIT 1) " +
            "    ELSE mb.title " +
            "END as bag_title, " +
            "CASE " +
            "    WHEN o.order_type = 'cart' THEN " +
            "        (SELECT m.name FROM order_items oi " +
            "         JOIN magic_bags mb ON oi.magic_bag_id = mb.id " +
            "         JOIN merchants m ON mb.merchant_id = m.id " +
            "         WHERE oi.order_id = o.id LIMIT 1) " +
            "    ELSE m.name " +
            "END as merchant_name " +
            "FROM orders o " +
            "LEFT JOIN users u ON o.user_id = u.id " +
            "LEFT JOIN magic_bags mb ON o.bag_id = mb.id " +
            "LEFT JOIN merchants m ON mb.merchant_id = m.id " +
            "WHERE o.user_id = #{userId} " +
            "ORDER BY o.created_at DESC")
    IPage<OrderDto> findByUserId(Page<OrderDto> page, @Param("userId") Integer userId);
    
    /**
     * 查询所有订单统计
     */
    @Select("SELECT " +
            "COUNT(*) as totalOrders, " +
            "SUM(CASE WHEN status = 'pending' THEN 1 ELSE 0 END) as pendingOrders, " +
            "SUM(CASE WHEN status = 'paid' THEN 1 ELSE 0 END) as paidOrders, " +
            "SUM(CASE WHEN status = 'completed' THEN 1 ELSE 0 END) as completedOrders, " +
            "SUM(CASE WHEN status = 'cancelled' THEN 1 ELSE 0 END) as cancelledOrders, " +
            "COALESCE(SUM(total_price), 0) as totalRevenue " +
            "FROM orders")
    OrderStatsDto findAllOrderStats();
    
    /**
     * 根据商家ID查询订单统计
     * 支持单件商品订单和多件商品订单
     */
    @Select("SELECT " +
            "COUNT(*) as totalOrders, " +
            "SUM(CASE WHEN o.status = 'pending' THEN 1 ELSE 0 END) as pendingOrders, " +
            "SUM(CASE WHEN o.status = 'paid' THEN 1 ELSE 0 END) as paidOrders, " +
            "SUM(CASE WHEN o.status = 'completed' THEN 1 ELSE 0 END) as completedOrders, " +
            "SUM(CASE WHEN o.status = 'cancelled' THEN 1 ELSE 0 END) as cancelledOrders, " +
            "COALESCE(SUM(o.total_price), 0) as totalRevenue " +
            "FROM orders o " +
            "WHERE (o.order_type = 'single' AND EXISTS " +
            "       (SELECT 1 FROM magic_bags mb WHERE mb.id = o.bag_id AND mb.merchant_id = #{merchantId})) " +
            "   OR (o.order_type = 'cart' AND EXISTS " +
            "       (SELECT 1 FROM order_items oi " +
            "        JOIN magic_bags mb ON oi.magic_bag_id = mb.id " +
            "        WHERE oi.order_id = o.id AND mb.merchant_id = #{merchantId}))")
    OrderStatsDto findOrderStatsByMerchantId(@Param("merchantId") Integer merchantId);
    
    /**
     * 根据用户ID查询订单统计
     */
    @Select("SELECT " +
            "COUNT(*) as totalOrders, " +
            "SUM(CASE WHEN status = 'pending' THEN 1 ELSE 0 END) as pendingOrders, " +
            "SUM(CASE WHEN status = 'paid' THEN 1 ELSE 0 END) as paidOrders, " +
            "SUM(CASE WHEN status = 'completed' THEN 1 ELSE 0 END) as completedOrders, " +
            "SUM(CASE WHEN status = 'cancelled' THEN 1 ELSE 0 END) as cancelledOrders, " +
            "COALESCE(SUM(total_price), 0) as totalRevenue " +
            "FROM orders " +
            "WHERE user_id = #{userId}")
    OrderStatsDto findOrderStatsByUserId(@Param("userId") Integer userId);
}