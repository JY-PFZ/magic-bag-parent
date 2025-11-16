package nus.iss.se.cart;

import nus.iss.se.cart.api.ProductClient;
import nus.iss.se.cart.dto.CartDto;
import nus.iss.se.cart.dto.MagicBagDto;
import nus.iss.se.cart.entity.Cart;
import nus.iss.se.cart.entity.CartItem;
import nus.iss.se.cart.mapper.CartItemMapper;
import nus.iss.se.cart.mapper.CartMapper;
import nus.iss.se.cart.service.impl.CartServiceImpl;
import nus.iss.se.common.Result;
import nus.iss.se.common.constant.ResultStatus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CartServiceImplTest {

    @Mock
    private CartMapper cartMapper;

    @Mock
    private CartItemMapper cartItemMapper;

    @Mock
    private ProductClient productClient;

    @InjectMocks
    private CartServiceImpl cartService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    private MagicBagDto dummyBag() {
        MagicBagDto dto = new MagicBagDto();
        dto.setId(1);
        dto.setTitle("Test Bag");
        dto.setPrice(BigDecimal.valueOf(10.0));
        return dto;
    }

    /** -------------------------
     *  Test: createCart
     *  ------------------------- */
    @Test
    void testCreateCart() {
        Cart cart = new Cart();
        cart.setCartId(100);
        cart.setUserId(1);

        doAnswer(invocation -> {
            ((Cart) invocation.getArgument(0)).setCartId(100);
            return 1;
        }).when(cartMapper).insertCart(any(Cart.class));

        CartDto result = cartService.createCart(1);

        assertNotNull(result);
        assertEquals(100, result.getCartId());
        assertEquals(1, result.getUserId());
    }

    /** -------------------------
     *  Test: addItemToCart (new item)
     *  ------------------------- */
    @Test
    void testAddItemToCart_NewItem() {
        MagicBagDto bag = dummyBag();

        when(productClient.getMagicBagById(1))
                .thenReturn(Result.success(bag));

        Cart cart = new Cart();
        cart.setCartId(10);
        cart.setUserId(1);
        when(cartMapper.findByUserId(1)).thenReturn(cart);

        when(cartItemMapper.selectOne(any())).thenReturn(null);

        when(productClient.getBatchMagicBags(any()))
                .thenReturn(Result.success(List.of(bag)));

        CartDto result = cartService.addItemToCart(1, 1, 2);

        verify(cartItemMapper, times(1)).insert(any());
        assertEquals(10, result.getCartId());
        assertEquals(1, result.getItems().get(0).getMagicBagId());
        assertEquals(2, result.getItems().get(0).getQuantity());
    }

    /** -------------------------
     *  Test: addItemToCart (update qty)
     *  ------------------------- */
    @Test
    void testAddItemToCart_UpdateExisting() {
        MagicBagDto bag = dummyBag();

        when(productClient.getMagicBagById(1))
                .thenReturn(Result.success(bag));

        Cart cart = new Cart();
        cart.setCartId(10);
        cart.setUserId(1);
        when(cartMapper.findByUserId(1)).thenReturn(cart);

        CartItem exist = new CartItem();
        exist.setCartItemId(999);
        exist.setCartId(10);
        exist.setMagicBagId(1);
        exist.setQuantity(3);

        when(cartItemMapper.selectOne(any())).thenReturn(exist);

        when(productClient.getBatchMagicBags(any()))
                .thenReturn(Result.success(List.of(bag)));

        CartDto result = cartService.addItemToCart(1, 1, 2);

        verify(cartItemMapper, times(1)).updateById(any());
        assertEquals(5, result.getItems().get(0).getQuantity());
    }

    /** -------------------------
     *  Test: update quantity
     *  ------------------------- */
    @Test
    void testUpdateItemQuantity() {
        Cart cart = new Cart();
        cart.setCartId(10);
        cart.setUserId(1);
        when(cartMapper.findByUserId(1)).thenReturn(cart);

        CartItem item = new CartItem();
        item.setCartItemId(500);
        item.setCartId(10);
        item.setMagicBagId(1);
        item.setQuantity(3);

        when(cartItemMapper.selectOne(any())).thenReturn(item);

        when(productClient.getBatchMagicBags(any()))
                .thenReturn(Result.success(List.of(dummyBag())));

        CartDto result = cartService.updateItemQuantityInCart(1, 1, 10);

        verify(cartItemMapper, times(1)).updateById(any());
        assertEquals(10, result.getItems().get(0).getQuantity());
    }

    /** -------------------------
     *  Test: remove item
     *  ------------------------- */
    @Test
    void testRemoveItem() {
        Cart cart = new Cart();
        cart.setCartId(10);
        cart.setUserId(1);
        when(cartMapper.findByUserId(1)).thenReturn(cart);

        CartItem item = new CartItem();
        item.setCartItemId(500);
        when(cartItemMapper.selectOne(any())).thenReturn(item);

        when(productClient.getBatchMagicBags(any()))
                .thenReturn(Result.success(List.of(dummyBag())));

        CartDto result = cartService.removeItemFromCart(1, 1);

        verify(cartItemMapper, times(1)).deleteById(500);
    }

    /** -------------------------
     *  Test: getCartItems
     *  ------------------------- */
    @Test
    void testGetCartItems() {
        Cart cart = new Cart();
        cart.setCartId(10);
        when(cartMapper.findByUserId(1)).thenReturn(cart);

        CartItem item = new CartItem();
        item.setCartItemId(1);
        item.setMagicBagId(1);
        item.setQuantity(2);

        when(cartItemMapper.selectList(any()))
                .thenReturn(List.of(item));

        MagicBagDto bag = dummyBag();
        when(productClient.getBatchMagicBags(any()))
                .thenReturn(Result.success(List.of(bag)));

        var items = cartService.getCartItems(1);

        assertEquals(1, items.size());
        assertEquals(2, items.get(0).getQuantity());
    }

    /** -------------------------
     *  Test: clear cart
     *  ------------------------- */
    @Test
    void testClearCart() {
        Cart cart = new Cart();
        cart.setCartId(10);
        when(cartMapper.findByUserId(1)).thenReturn(cart);

        when(cartItemMapper.delete(any())).thenReturn(3);

        CartDto dto = cartService.clearCart(1);

        verify(cartItemMapper, times(1)).delete(any());
        assertEquals(10, dto.getCartId());
    }

    /** -------------------------
     *  Test: getTotal
     *  ------------------------- */
    @Test
    void testGetTotal() {
        Cart cart = new Cart();
        cart.setCartId(10);
        when(cartMapper.findByUserId(1)).thenReturn(cart);

        CartItem item = new CartItem();
        item.setMagicBagId(1);
        item.setQuantity(3);

        when(cartItemMapper.selectList(any()))
                .thenReturn(List.of(item));

        MagicBagDto bag = dummyBag();
        when(productClient.getBatchMagicBags(any()))
                .thenReturn(Result.success(List.of(bag)));

        double total = cartService.getTotal(1);

        assertEquals(30.0, total); // 3 * 10
    }
}
