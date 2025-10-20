package nus.iss.se.cart.mapper;

import org.apache.ibatis.annotations.*;

import nus.iss.se.cart.entity.CartItem;

import java.util.List;

/**
 * 购物车项 Mapper
 */
@Mapper
public interface CartItemMapper {
    
    /**
     * 插入购物车项
     */
    @Insert("INSERT INTO cart_items (cart_id, magicbag_id, quantity, added_at) " +
            "VALUES (#{cartId}, #{magicBagId}, #{quantity}, #{addedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "cartItemId")
    int insertCartItem(CartItem cartItem);
    
    /**
     * 根据购物车ID查询所有项
     */
    @Select("SELECT cart_item_id, cart_id, magicbag_id, quantity, added_at " +
            "FROM cart_items WHERE cart_id = #{cartId} ORDER BY added_at DESC")
    @Results(id = "CartItemResultMap", value = {
        @Result(property = "cartItemId", column = "cart_item_id", id = true),
        @Result(property = "cartId", column = "cart_id"),
        @Result(property = "magicBagId", column = "magicbag_id"),
        @Result(property = "quantity", column = "quantity"),
        @Result(property = "addedAt", column = "added_at")
    })
    List<CartItem> findByCartId(@Param("cartId") Integer cartId);
    
    /**
     * 根据购物车ID和产品ID查询
     */
    @Select("SELECT cart_item_id, cart_id, magicbag_id, quantity, added_at " +
            "FROM cart_items WHERE cart_id = #{cartId} AND magicbag_id = #{magicBagId} LIMIT 1")
    @ResultMap("CartItemResultMap")
    CartItem findByCartIdAndMagicBagId(@Param("cartId") Integer cartId, 
                                       @Param("magicBagId") Integer magicBagId);
    
    /**
     * 更新购物车项
     */
    @Update("UPDATE cart_items SET quantity = #{quantity}, added_at = #{addedAt} " +
            "WHERE cart_item_id = #{cartItemId}")
    int updateCartItem(CartItem cartItem);
    
    /**
     * 删除购物车项
     */
    @Delete("DELETE FROM cart_items WHERE cart_item_id = #{cartItemId}")
    int deleteCartItem(@Param("cartItemId") Integer cartItemId);
    
    /**
     * 根据购物车ID删除所有项
     */
    @Delete("DELETE FROM cart_items WHERE cart_id = #{cartId}")
    int deleteByCartId(@Param("cartId") Integer cartId);
    
    /**
     * 根据产品ID查询购物车项
     */
    @Select("SELECT cart_item_id, cart_id, magicbag_id, quantity, added_at " +
            "FROM cart_items WHERE magicbag_id = #{magicBagId}")
    @ResultMap("CartItemResultMap")
    List<CartItem> findByMagicBagId(@Param("magicBagId") Integer magicBagId);
}