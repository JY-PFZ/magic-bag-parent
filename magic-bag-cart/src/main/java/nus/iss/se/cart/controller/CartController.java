package nus.iss.se.cart.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nus.iss.se.cart.dto.CartDto;
import nus.iss.se.cart.dto.CartItemDto;
import nus.iss.se.cart.service.ICartService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * 购物车控制器
 */
@Slf4j
@RestController
@RequestMapping("/cart")
@Tag(name = "Cart API", description = "APIs to manage user shopping cart")
@RequiredArgsConstructor
public class CartController {
    
    private final ICartService cartService;
    
    /**
     * 为用户创建购物车
     */
    @PostMapping("/{userId}")
    @Operation(summary = "Create a new cart for user", description = "Creates a new shopping cart for the given user ID")
    public CartDto createCart(@PathVariable Integer userId) {
        log.info("Creating cart for user: {}", userId);
        try {
            return cartService.createCart(userId);
        } catch (Exception e) {
            log.error("Error creating cart for user {}: {}", userId, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 获取用户的活跃购物车
     */
    @GetMapping("/{userId}")
    @Operation(summary = "Get active cart for user", description = "Retrieve the currently active shopping cart for a user")
    public CartDto getCart(@PathVariable Integer userId) {
        log.info("Getting cart for user: {}", userId);
        try {
            CartDto cart = cartService.getActiveCart(userId);
            if (cart == null) {
                log.warn("Cart not found for user: {}, creating new one", userId);
                return cartService.createCart(userId);
            }
            return cart;
        } catch (Exception e) {
            log.error("Error getting cart for user {}: {}", userId, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 添加商品到购物车
     */
    @PostMapping("/{userId}/items")
    @Operation(summary = "Add item to cart", description = "Add a MagicBag item to the user's shopping cart with specified quantity")
    public CartDto addItemToCart(
            @PathVariable Integer userId,
            @RequestParam Integer magicbagId,
            @RequestParam int quantity) {

        return cartService.addItemToCart(userId, magicbagId, quantity);
    }
    
    /**
     * 更新购物车中商品的数量
     */
    @PutMapping("/{userId}/items/{magicbagId}")
    @Operation(summary = "Update item quantity", description = "Update the quantity of a MagicBag item in the user's cart")
    public CartDto updateItemQuantity(
            @PathVariable Integer userId,
            @PathVariable Integer magicbagId,
            @RequestParam int quantity) {
        
        log.info("Updating cart item quantity: userId={}, magicbagId={}, newQuantity={}", 
                userId, magicbagId, quantity);
        
        try {
            if (quantity < 0) {
                throw new IllegalArgumentException("Quantity cannot be negative");
            }
            return cartService.updateItemQuantityInCart(userId, magicbagId, quantity);
        } catch (NoSuchElementException e) {
            log.error("Cart or item not found: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error updating cart item quantity: userId={}, magicbagId={}, error={}", 
                    userId, magicbagId, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 从购物车中删除商品
     */
    @DeleteMapping("/{userId}/items/{magicbagId}")
    @Operation(summary = "Remove item from cart", description = "Remove a specific MagicBag item from the user's cart")
    public CartDto removeItem(
            @PathVariable Integer userId,
            @PathVariable Integer magicbagId) {
        
        log.info("Removing item from cart: userId={}, magicbagId={}", userId, magicbagId);
        
        try {
            return cartService.removeItemFromCart(userId, magicbagId);
        } catch (NoSuchElementException e) {
            log.error("Cart not found: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error removing item from cart: userId={}, magicbagId={}, error={}", 
                    userId, magicbagId, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 获取购物车中的所有商品
     */
    @GetMapping("/{userId}/items")
    @Operation(summary = "List cart items", description = "Get all items currently in the user's shopping cart")
    public List<CartItemDto> getCartItems(@PathVariable Integer userId) {
        log.info("Getting cart items for user: {}", userId);
        
        try {
            List<CartItemDto> items = cartService.getCartItems(userId);
            log.info("Retrieved {} cart items for user: {}", items.size(), userId);
            return items;
        } catch (RuntimeException e) {
            log.error("Error getting cart items for user {}: {}", userId, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error getting cart items for user {}: {}", userId, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 清空购物车
     */
    @DeleteMapping("/{userId}/items")
    @Operation(summary = "Clear cart", description = "Remove all items from the user's shopping cart")
    public CartDto clearCart(@PathVariable Integer userId) {
        log.info("Clearing cart for user: {}", userId);
        
        try {
            CartDto cart = cartService.clearCart(userId);
            log.info("Cart cleared successfully for user: {}", userId);
            return cart;
        } catch (Exception e) {
            log.error("Error clearing cart for user {}: {}", userId, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 获取购物车总金额
     */
    @GetMapping("/{userId}/total")
    @Operation(summary = "Get cart total", description = "Calculate the total price of all items in the user's cart")
    public double getTotal(@PathVariable Integer userId) {
        log.info("Getting cart total for user: {}", userId);
        
        try {
            double total = cartService.getTotal(userId);
            log.info("Cart total for user {}: {}", userId, total);
            return total;
        } catch (RuntimeException e) {
            log.error("Error getting cart total for user {}: {}", userId, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error getting cart total for user {}: {}", userId, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 获取某个产品在所有购物车中的记录
     */
    @GetMapping("/items/magicbag/{magicbagId}")
    @Operation(summary = "Get cart items by MagicBag ID", description = "Get cart items that correspond to a specific MagicBag ID")
    public List<CartItemDto> getCartItemsByMagicBagId(@PathVariable Integer magicbagId) {
        log.info("Getting cart items for product: {}", magicbagId);
        
        try {
            List<CartItemDto> items = cartService.getCartItemsByMagicBagId(magicbagId);
            log.info("Retrieved {} cart items for product: {}", items.size(), magicbagId);
            return items;
        } catch (RuntimeException e) {
            log.error("Error getting cart items for product {}: {}", magicbagId, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error getting cart items for product {}: {}", magicbagId, e.getMessage(), e);
            throw e;
        }
    }
}