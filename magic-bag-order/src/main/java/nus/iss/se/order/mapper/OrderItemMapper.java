package nus.iss.se.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import nus.iss.se.order.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {
    @Select("SELECT * FROM order_items WHERE order_id = #{orderId}")
    List<OrderItem> findByOrderId(@Param("orderId") Integer orderId);
}

