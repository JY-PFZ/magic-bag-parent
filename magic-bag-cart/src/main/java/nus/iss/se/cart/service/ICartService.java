package nus.iss.se.cart.service;


import java.util.List;

import nus.iss.se.cart.dto.CartDto;
import nus.iss.se.cart.dto.CartItemDto;

/**
 * 购物车服务接口
 */
public interface ICartService {
    
    /**
     * 创建购物车
     */
    CartDto createCart(Integer userId);
    
    /**
     * 获取用户的活跃购物车
     */
    CartDto getActiveCart(Integer userId);
    
    /**
     * 添加商品到购物车
     */
    CartDto addItemToCart(Integer userId, Integer magicBagId, int quantity);
    
    /**
     * 更新购物车商品数量
     */
    CartDto updateItemQuantityInCart(Integer userId, Integer magicBagId, int newQuantity);
    
    /**
     * 从购物车移除商品
     */
    CartDto removeItemFromCart(Integer userId, Integer magicBagId);
    
    /**
     * 获取购物车商品列表
     */
    List<CartItemDto> getCartItems(Integer userId);
    
    /**
     * 清空购物车
     */
    CartDto clearCart(Integer userId);
    
    /**
     * 获取购物车总价
     */
    double getTotal(Integer userId);
    
    /**
     * 根据盲盒ID获取购物车商品
     */
    List<CartItemDto> getCartItemsByMagicBagId(Integer magicBagId);
}