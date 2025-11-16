package nus.iss.se.cart.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nus.iss.se.common.Result;
import nus.iss.se.common.constant.ResultStatus;
import nus.iss.se.cart.api.ProductClient;
import nus.iss.se.cart.dto.CartDto;
import nus.iss.se.cart.dto.CartItemDto;
import nus.iss.se.cart.dto.MagicBagDto;
import nus.iss.se.cart.entity.Cart;
import nus.iss.se.cart.entity.CartItem;
import nus.iss.se.cart.mapper.CartItemMapper;
import nus.iss.se.cart.mapper.CartMapper;
import nus.iss.se.cart.service.ICartService;
import nus.iss.se.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 购物车服务实现类 (修复版 - 统一使用 QueryWrapper)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements ICartService {
    
    private final CartMapper cartMapper;
    private final CartItemMapper cartItemMapper;
    private final ProductClient productClient;
    
    @Override
    public CartDto createCart(Integer userId) {
        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setCreatedAt(LocalDateTime.now());
        cart.setUpdatedAt(LocalDateTime.now());
        cartMapper.insertCart(cart);
        return convertToCartDto(cart);
    }
    
    @Override
    public CartDto getActiveCart(Integer userId) {
        Cart cart = cartMapper.findByUserId(userId);
        if (cart != null) {
            // ✅ 修复：使用普通 QueryWrapper
            QueryWrapper<CartItem> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("cart_id", cart.getCartId());
            List<CartItem> items = cartItemMapper.selectList(queryWrapper);
            cart.setCartItems(items);
        }
        return convertToCartDto(cart);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CartDto addItemToCart(Integer userId, Integer magicBagId, int quantity) {
        log.info("Adding item to cart: userId={}, magicBagId={}, quantity={}", userId, magicBagId, quantity);
        
        // 验证产品是否存在
        Result<MagicBagDto> bagResult = productClient.getMagicBagById(magicBagId);
        if (!isResultSuccess(bagResult) || bagResult.getData() == null) {
            log.error("Product not found: {}", magicBagId);
            throw new BusinessException(ResultStatus.PRODUCT_NOT_FOUND,"detail: "+magicBagId);
        }
        
        // 获取或创建购物车
        Cart cart = cartMapper.findByUserId(userId);
        if (cart == null) {
            cart = new Cart();
            cart.setUserId(userId);
            cart.setCreatedAt(LocalDateTime.now());
            cart.setUpdatedAt(LocalDateTime.now());
            cartMapper.insertCart(cart);
            log.info("Created new cart for user: {}, cartId={}", userId, cart.getCartId());
        }
        
        // ✅ 修复：使用普通 QueryWrapper，明确指定数据库列名
        QueryWrapper<CartItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("cart_id", cart.getCartId())
                   .eq("magic_bag_id", magicBagId);
        CartItem existing = cartItemMapper.selectOne(queryWrapper);
        
        log.info("Existing cart item check: cartId={}, magicBagId={}, found={}", 
                cart.getCartId(), magicBagId, existing != null);

        if (existing != null) {
            // 更新数量 - 使用 updateById
            existing.setQuantity(existing.getQuantity() + quantity);
            existing.setAddedAt(LocalDateTime.now());
            int result = cartItemMapper.updateById(existing);
            log.info("Updated cart item quantity: cartId={}, magicBagId={}, newQuantity={}, result={}", 
                    cart.getCartId(), magicBagId, existing.getQuantity(), result);
        } else {
            // 添加新商品 - 使用 insert
            CartItem item = new CartItem();
            item.setCartId(cart.getCartId());
            item.setMagicBagId(magicBagId);
            item.setQuantity(quantity);
            item.setAddedAt(LocalDateTime.now());
            
            int result = cartItemMapper.insert(item);
            log.info("Added new item to cart: cartId={}, magicBagId={}, quantity={}, result={}, generatedId={}", 
                    cart.getCartId(), magicBagId, quantity, result, item.getCartItemId());
            
            // 验证插入是否成功
            if (result <= 0) {
                log.error("Failed to insert cart item! Insert result: {}", result);
                throw new RuntimeException("Failed to insert cart item");
            }
        }
        
        // 更新购物车时间
        cart.setUpdatedAt(LocalDateTime.now());
        cartMapper.updateCart(cart);
        
        // 重新加载购物车项
        return getActiveCart(userId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CartDto updateItemQuantityInCart(Integer userId, Integer magicBagId, int newQuantity) {
        if (newQuantity < 0) {
            log.error("Invalid quantity: {}", newQuantity);
            throw new IllegalArgumentException("Quantity cannot be less than zero.");
        }
        
        Cart cart = cartMapper.findByUserId(userId);
        if (cart == null) {
            log.error("Cart not found for user: {}", userId);
            throw new NoSuchElementException(ResultStatus.CART_NOT_FOUND.getMessage());
        }
        
        QueryWrapper<CartItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("cart_id", cart.getCartId())
                   .eq("magic_bag_id", magicBagId);
        CartItem item = cartItemMapper.selectOne(queryWrapper);
        
        if (item == null) {
            log.error("Cart item not found: cartId={}, magicBagId={}", cart.getCartId(), magicBagId);
            throw new NoSuchElementException(ResultStatus.CART_ITEM_NOT_FOUND.getMessage());
        }
        
        if (newQuantity == 0) {
            // 数量为0，删除商品 - 使用 deleteById
            cartItemMapper.deleteById(item.getCartItemId());
            log.info("Removed cart item: cartItemId={}", item.getCartItemId());
        } else {
            // 更新数量 - 使用 updateById
            item.setQuantity(newQuantity);
            item.setAddedAt(LocalDateTime.now());
            cartItemMapper.updateById(item);
            log.info("Updated cart item quantity: cartItemId={}, newQuantity={}", 
                    item.getCartItemId(), newQuantity);
        }
        
        // 更新购物车时间
        cart.setUpdatedAt(LocalDateTime.now());
        cartMapper.updateCart(cart);
        
        return getActiveCart(userId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CartDto removeItemFromCart(Integer userId, Integer magicBagId) {
        Cart cart = cartMapper.findByUserId(userId);
        if (cart == null) {
            log.error("Cart not found for user: {}", userId);
            throw new NoSuchElementException(ResultStatus.CART_NOT_FOUND.getMessage());
        }
        
        QueryWrapper<CartItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("cart_id", cart.getCartId())
                   .eq("magic_bag_id", magicBagId);
        CartItem item = cartItemMapper.selectOne(queryWrapper);
        
        if (item != null) {
            // 使用 deleteById 删除
            cartItemMapper.deleteById(item.getCartItemId());
            log.info("Removed item from cart: cartId={}, magicBagId={}", cart.getCartId(), magicBagId);
        } else {
            log.warn("Cart item not found when trying to remove: cartId={}, magicBagId={}", 
                    cart.getCartId(), magicBagId);
        }
        
        // 更新购物车时间
        cart.setUpdatedAt(LocalDateTime.now());
        cartMapper.updateCart(cart);
        
        return getActiveCart(userId);
    }
    
    @Override
    public List<CartItemDto> getCartItems(Integer userId) {
        Cart cart = cartMapper.findByUserId(userId);
        if (cart == null) {
            log.info("No cart found for user: {}", userId);
            return List.of();
        }
        
        // 使用 QueryWrapper 查询购物车项
        QueryWrapper<CartItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("cart_id", cart.getCartId())
                   .orderByDesc("added_at");
        List<CartItem> items = cartItemMapper.selectList(queryWrapper);
        
        if (items.isEmpty()) {
            log.info("Cart is empty for user: {}", userId);
            return List.of();
        }
        
        // 批量查询产品信息（性能优化）
        List<Integer> bagIds = items.stream()
                .map(CartItem::getMagicBagId)
                .distinct()
                .collect(Collectors.toList());
        
        log.info("Fetching product info for {} items", bagIds.size());
        Result<List<MagicBagDto>> result = productClient.getBatchMagicBags(bagIds);
        
        if (!isResultSuccess(result) || result.getData() == null) {
            log.error("Failed to fetch product information from Product service");
            throw new RuntimeException("Failed to fetch product information");
        }
        
        Map<Integer, MagicBagDto> bagMap = result.getData().stream()
                .collect(Collectors.toMap(MagicBagDto::getId, Function.identity()));
        
        // 构建返回结果
        List<CartItemDto> cartItemDtos = items.stream().map(item -> {
            MagicBagDto bag = bagMap.get(item.getMagicBagId());
            if (bag == null) {
                log.warn("Product not found for cart item: magicBagId={}", item.getMagicBagId());
                return null;
            }
            double subtotal = bag.getPrice().doubleValue() * item.getQuantity();
            return new CartItemDto(
                item.getCartItemId(),
                item.getMagicBagId(),
                bag.getTitle(),
                bag.getPrice().doubleValue(),
                item.getQuantity(),
                subtotal
            );
        }).filter(dto -> dto != null).collect(Collectors.toList());
        
        log.info("Retrieved {} cart items for user: {}", cartItemDtos.size(), userId);
        return cartItemDtos;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CartDto clearCart(Integer userId) {
        Cart cart = cartMapper.findByUserId(userId);
        if (cart != null) {
            // 使用 QueryWrapper 删除所有购物车项
            QueryWrapper<CartItem> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("cart_id", cart.getCartId());
            int deletedCount = cartItemMapper.delete(queryWrapper);
            log.info("Cleared {} items from cart: cartId={}", deletedCount, cart.getCartId());
            
            cart.setUpdatedAt(LocalDateTime.now());
            cartMapper.updateCart(cart);
            cart.setCartItems(List.of());
        } else {
            log.warn("No cart found to clear for user: {}", userId);
        }
        return convertToCartDto(cart);
    }
    
    @Override
    public double getTotal(Integer userId) {
        Cart cart = cartMapper.findByUserId(userId);
        if (cart == null) {
            log.info("No cart found for user: {}", userId);
            return 0.0;
        }
        
        QueryWrapper<CartItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("cart_id", cart.getCartId());
        List<CartItem> items = cartItemMapper.selectList(queryWrapper);
        
        if (items.isEmpty()) {
            log.info("Cart is empty for user: {}", userId);
            return 0.0;
        }
        
        // 批量查询产品信息
        List<Integer> bagIds = items.stream()
                .map(CartItem::getMagicBagId)
                .distinct()
                .collect(Collectors.toList());
        
        Result<List<MagicBagDto>> result = productClient.getBatchMagicBags(bagIds);
        
        if (!isResultSuccess(result) || result.getData() == null) {
            log.error("Failed to fetch product information when calculating total");
            throw new RuntimeException("Failed to fetch product information");
        }
        
        Map<Integer, MagicBagDto> bagMap = result.getData().stream()
                .collect(Collectors.toMap(MagicBagDto::getId, Function.identity()));
        
        double total = items.stream()
                .mapToDouble(item -> {
                    MagicBagDto bag = bagMap.get(item.getMagicBagId());
                    if (bag == null) {
                        log.warn("Product not found when calculating total: magicBagId={}", 
                                item.getMagicBagId());
                        return 0.0;
                    }
                    return bag.getPrice().doubleValue() * item.getQuantity();
                })
                .sum();
        
        log.info("Cart total for user {}: {}", userId, total);
        return total;
    }
    
    @Override
    public List<CartItemDto> getCartItemsByMagicBagId(Integer magicBagId) {
        // 使用 QueryWrapper 查询
        QueryWrapper<CartItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("magic_bag_id", magicBagId);
        List<CartItem> items = cartItemMapper.selectList(queryWrapper);
        
        if (items.isEmpty()) {
            log.info("No cart items found for magicBagId: {}", magicBagId);
            return List.of();
        }
        
        // 查询产品信息
        Result<MagicBagDto> bagResult = productClient.getMagicBagById(magicBagId);
        if (!isResultSuccess(bagResult) || bagResult.getData() == null) {
            log.error("Product not found: {}", magicBagId);
            throw new RuntimeException(ResultStatus.PRODUCT_NOT_FOUND.getMessage());
        }
        
        MagicBagDto bag = bagResult.getData();
        
        List<CartItemDto> cartItemDtos = items.stream().map(item -> {
            double subtotal = bag.getPrice().doubleValue() * item.getQuantity();
            return new CartItemDto(
                item.getCartItemId(),
                item.getMagicBagId(),
                bag.getTitle(),
                bag.getPrice().doubleValue(),
                item.getQuantity(),
                subtotal
            );
        }).toList();
        
        log.info("Retrieved {} cart items for magicBagId: {}", cartItemDtos.size(), magicBagId);
        return cartItemDtos;
    }
    
    /**
     * 转换 Cart 实体为 DTO
     */
    private CartDto convertToCartDto(Cart cart) {
        if (cart == null) {
            return null;
        }
        
        List<CartItem> cartItems = cart.getCartItems();
        if (cartItems == null || cartItems.isEmpty()) {
            return new CartDto(cart.getCartId(), cart.getUserId(), List.of(), 0.0);
        }
        
        // 批量查询产品信息
        List<Integer> bagIds = cartItems.stream()
                .map(CartItem::getMagicBagId)
                .distinct()
                .toList();
        
        Result<List<MagicBagDto>> result = productClient.getBatchMagicBags(bagIds);
        if (!isResultSuccess(result) || result.getData() == null) {
            log.error("Failed to fetch product information for cart: {}", cart.getCartId());
            return new CartDto(cart.getCartId(), cart.getUserId(), List.of(), 0.0);
        }
        
        Map<Integer, MagicBagDto> bagMap = result.getData().stream()
                .collect(Collectors.toMap(MagicBagDto::getId, Function.identity()));
        
        List<CartItemDto> items = cartItems.stream().map(item -> {
            MagicBagDto bag = bagMap.get(item.getMagicBagId());
            if (bag == null) {
                log.warn("Product not found in cart: magicBagId={}", item.getMagicBagId());
                return null;
            }
            double subtotal = bag.getPrice().doubleValue() * item.getQuantity();
            return new CartItemDto(
                item.getCartItemId(),
                item.getMagicBagId(),
                bag.getTitle(),
                bag.getPrice().doubleValue(),
                item.getQuantity(),
                subtotal
            );
        }).filter(dto -> dto != null).collect(Collectors.toList());
        
        double total = items.stream().mapToDouble(CartItemDto::getSubtotal).sum();
        
        return new CartDto(cart.getCartId(), cart.getUserId(), items, total);
    }
    
    /**
     * 判断 Result 是否成功的辅助方法
     */
    private boolean isResultSuccess(Result<?> result) {
        return result != null && result.getCode() == ResultStatus.SUCCESS.getCode();
    }
}