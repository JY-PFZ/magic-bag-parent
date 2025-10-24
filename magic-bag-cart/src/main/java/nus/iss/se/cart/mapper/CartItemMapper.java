package nus.iss.se.cart.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import nus.iss.se.cart.entity.CartItem;

/**
 * 购物车项 Mapper
 */
@Mapper
public interface CartItemMapper extends BaseMapper<CartItem> {
    // BaseMapper 提供了所有基础的 CRUD 方法
    // 不需要手动定义方法
}