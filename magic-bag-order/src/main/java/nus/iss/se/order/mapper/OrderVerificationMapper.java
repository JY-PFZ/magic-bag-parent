package nus.iss.se.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import nus.iss.se.order.entity.OrderVerification;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 订单核销 Mapper - 纯注解方式
 */
@Mapper
public interface OrderVerificationMapper extends BaseMapper<OrderVerification> {
    
    /**
     * 根据订单ID查询核销记录
     */
    @Select("SELECT id, order_id, verified_by, verified_at, location " +
            "FROM order_verifications WHERE order_id = #{orderId} ORDER BY verified_at DESC")
    @Results({
        @Result(property = "id", column = "id", id = true),
        @Result(property = "orderId", column = "order_id"),
        @Result(property = "verifiedBy", column = "verified_by"),
        @Result(property = "verifiedAt", column = "verified_at"),
        @Result(property = "location", column = "location")
    })
    List<OrderVerification> findByOrderId(@Param("orderId") Integer orderId);
}