package nus.iss.se.cart.mapper;

import org.apache.ibatis.annotations.*;

import nus.iss.se.cart.entity.Cart;

/**
 * 购物车 Mapper
 */
@Mapper
public interface CartMapper {
    
    /**
     * 插入购物车
     */
    @Insert("INSERT INTO carts (user_id, created_at, updated_at) " +
            "VALUES (#{userId}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "cartId")
    int insertCart(Cart cart);
    
    /**
     * 根据用户ID查询购物车
     */
    @Select("SELECT cart_id, user_id, created_at, updated_at " +
            "FROM carts WHERE user_id = #{userId} LIMIT 1")
    @Results(id = "CartResultMap", value = {
        @Result(property = "cartId", column = "cart_id", id = true),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    Cart findByUserId(@Param("userId") Integer userId);
    
    /**
     * 更新购物车
     */
    @Update("UPDATE carts SET updated_at = #{updatedAt} WHERE cart_id = #{cartId}")
    int updateCart(Cart cart);
    
    /**
     * 根据ID查询购物车
     */
    @Select("SELECT cart_id, user_id, created_at, updated_at " +
            "FROM carts WHERE cart_id = #{cartId}")
    @ResultMap("CartResultMap")  // 复用上面定义的 ResultMap
    Cart selectById(@Param("cartId") Integer cartId);
    
    /**
     * 删除购物车
     */
    @Delete("DELETE FROM carts WHERE cart_id = #{cartId}")
    int deleteCart(@Param("cartId") Integer cartId);
}