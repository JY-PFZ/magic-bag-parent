package nus.iss.se.order;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import nus.iss.se.common.Result;
import nus.iss.se.common.cache.UserContext;
import nus.iss.se.common.constant.ResultStatus;
import nus.iss.se.common.exception.BusinessException;
import nus.iss.se.order.api.CartClient;
import nus.iss.se.order.api.MerchantClient;
import nus.iss.se.order.api.ProductClient;
import nus.iss.se.order.api.UserClient;
import nus.iss.se.order.dto.*;
import nus.iss.se.order.entity.Order;
import nus.iss.se.order.entity.OrderItem;
import nus.iss.se.order.entity.OrderVerification;
import nus.iss.se.order.mapper.OrderItemMapper;
import nus.iss.se.order.mapper.OrderMapper;
import nus.iss.se.order.mapper.OrderVerificationMapper;
import nus.iss.se.order.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class OrderServiceImplTest {

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderItemMapper orderItemMapper;

    @Mock
    private OrderVerificationMapper orderVerificationMapper;

    @Mock
    private ProductClient productClient;

    @Mock
    private UserClient userClient;

    @Mock
    private MerchantClient merchantClient;

    @Mock
    private CartClient cartClient;

    @InjectMocks
    private OrderServiceImpl orderService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    private UserContext adminContext() {
        UserContext ctx = new UserContext();
        ctx.setId(1);
        ctx.setRole("ADMIN");
        return ctx;
    }

    private UserContext merchantContext() {
        UserContext ctx = new UserContext();
        ctx.setId(2);
        ctx.setRole("MERCHANT");
        return ctx;
    }

    private UserContext userContext() {
        UserContext ctx = new UserContext();
        ctx.setId(3);
        ctx.setRole("USER");
        return ctx;
    }

    private Order dummyOrder() {
        Order order = new Order();
        order.setId(1);
        order.setOrderNo("ORD123456789");
        order.setUserId(3);
        order.setBagId(1);
        order.setOrderType("single");
        order.setTotalPrice(BigDecimal.valueOf(50.00));
        order.setStatus("pending");
        order.setPickupCode("1234");
        order.setCreatedAt(new Date());
        return order;
    }

    private OrderItem dummyOrderItem() {
        OrderItem item = new OrderItem();
        item.setId(1);
        item.setOrderId(1);
        item.setMagicBagId(1);
        item.setQuantity(2);
        item.setUnitPrice(BigDecimal.valueOf(25.00));
        item.setSubtotal(BigDecimal.valueOf(50.00));
        return item;
    }

    private MagicBagDto dummyBag() {
        MagicBagDto dto = new MagicBagDto();
        dto.setId(1);
        dto.setTitle("Test Bag");
        dto.setPrice(BigDecimal.valueOf(25.0));
        dto.setMerchantId(2);
        return dto;
    }

    private UserDto dummyUser() {
        UserDto dto = new UserDto();
        dto.setId(3);
        dto.setUsername("testuser");
        dto.setNickname("Test User");
        dto.setPhone("12345678");
        return dto;
    }

    private MerchantDto dummyMerchant() {
        MerchantDto dto = new MerchantDto();
        dto.setId(2);
        dto.setName("Test Merchant");
        dto.setPhone("87654321");
        dto.setAddress("123 Test St");
        return dto;
    }

    private CartDto dummyCart() {
        CartItemDto item = new CartItemDto();
        item.setMagicBagId(1);
        item.setQuantity(2);
        item.setPrice(25.0);
        item.setSubtotal(50.0);
        
        return new CartDto(1, 3, List.of(item), 50.0);
    }

    /** -------------------------
     *  Test: getOrders (Admin)
     *  ------------------------- */
    @Test
    void testGetOrders_Admin() {
        UserContext admin = adminContext();
        OrderQueryDto query = new OrderQueryDto();
        query.setPageNum(1);
        query.setPageSize(10);

        Page<OrderDto> page = new Page<>(1, 10);
        OrderDto orderDto = new OrderDto();
        orderDto.setId(1);
        orderDto.setUserId(3);
        page.setRecords(List.of(orderDto));

        when(orderMapper.findAllOrders(any(Page.class))).thenReturn(page);
        when(userClient.getUserById(3)).thenReturn(Result.success(dummyUser()));

        IPage<OrderDto> result = orderService.getOrders(admin, query);

        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
        verify(orderMapper, times(1)).findAllOrders(any(Page.class));
    }

    /** -------------------------
     *  Test: getOrders (User)
     *  ------------------------- */
    @Test
    void testGetOrders_User() {
        UserContext user = userContext();
        OrderQueryDto query = new OrderQueryDto();
        query.setPageNum(1);
        query.setPageSize(10);

        Page<OrderDto> page = new Page<>(1, 10);
        OrderDto orderDto = new OrderDto();
        orderDto.setId(1);
        orderDto.setUserId(3);
        page.setRecords(List.of(orderDto));

        when(orderMapper.findByUserId(any(Page.class), eq(3))).thenReturn(page);
        when(userClient.getUserById(3)).thenReturn(Result.success(dummyUser()));

        IPage<OrderDto> result = orderService.getOrders(user, query);

        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
        verify(orderMapper, times(1)).findByUserId(any(Page.class), eq(3));
    }

    /** -------------------------
     *  Test: getOrderDetail (success)
     *  ------------------------- */
    @Test
    void testGetOrderDetail_Success() {
        Order order = dummyOrder();
        UserContext user = userContext();

        when(orderMapper.selectById(1)).thenReturn(order);
        when(userClient.getUserById(3)).thenReturn(Result.success(dummyUser()));
        when(productClient.getMagicBagById(1)).thenReturn(Result.success(dummyBag()));
        when(merchantClient.getMerchantById(2)).thenReturn(Result.success(dummyMerchant()));
        when(orderVerificationMapper.findByOrderId(1)).thenReturn(new ArrayList<>());

        OrderDetailResponse result = orderService.getOrderDetail(1, user);

        assertNotNull(result);
        assertNotNull(result.getOrder());
        assertEquals(1, result.getOrder().getId());
        verify(orderMapper, times(1)).selectById(1);
    }

    /** -------------------------
     *  Test: getOrderDetail (not found)
     *  ------------------------- */
    @Test
    void testGetOrderDetail_NotFound() {
        when(orderMapper.selectById(999)).thenReturn(null);

        assertThrows(BusinessException.class, () -> 
            orderService.getOrderDetail(999, userContext())
        );
        verify(orderMapper, times(1)).selectById(999);
    }

    /** -------------------------
     *  Test: getOrderDetail (unauthorized)
     *  ------------------------- */
    @Test
    void testGetOrderDetail_Unauthorized() {
        Order order = dummyOrder();
        order.setUserId(999); // Different user
        
        when(orderMapper.selectById(1)).thenReturn(order);

        assertThrows(BusinessException.class, () -> 
            orderService.getOrderDetail(1, userContext())
        );
    }

    /** -------------------------
     *  Test: updateOrderStatus
     *  ------------------------- */
    @Test
    void testUpdateOrderStatus() {
        Order order = dummyOrder();
        OrderStatusUpdateDto statusDto = new OrderStatusUpdateDto();
        statusDto.setStatus("paid");

        when(orderMapper.selectById(1)).thenReturn(order);
        when(orderMapper.updateById(any(Order.class))).thenReturn(1);

        orderService.updateOrderStatus(1, statusDto, adminContext());

        verify(orderMapper, times(1)).updateById(any(Order.class));
    }

    /** -------------------------
     *  Test: updateOrderStatusInternal
     *  ------------------------- */
    @Test
    void testUpdateOrderStatusInternal() {
        Order order = dummyOrder();

        when(orderMapper.selectById(1)).thenReturn(order);
        when(orderMapper.updateById(any(Order.class))).thenReturn(1);

        orderService.updateOrderStatusInternal(1, "paid");

        verify(orderMapper, times(1)).updateById(any(Order.class));
    }

    /** -------------------------
     *  Test: cancelOrder (by user)
     *  ------------------------- */
    @Test
    void testCancelOrder_ByUser() {
        Order order = dummyOrder();

        when(orderMapper.selectById(1)).thenReturn(order);
        when(orderMapper.updateById(any(Order.class))).thenReturn(1);

        orderService.cancelOrder(1, userContext());

        verify(orderMapper, times(1)).updateById(argThat(o -> 
            "cancelled".equals(o.getStatus())
        ));
    }

    /** -------------------------
     *  Test: cancelOrder (already cancelled)
     *  ------------------------- */
    @Test
    void testCancelOrder_AlreadyCancelled() {
        Order order = dummyOrder();
        order.setStatus("cancelled");

        when(orderMapper.selectById(1)).thenReturn(order);

        assertThrows(BusinessException.class, () -> 
            orderService.cancelOrder(1, userContext())
        );
    }

    /** -------------------------
     *  Test: verifyOrder
     *  ------------------------- */
    @Test
    void testVerifyOrder() {
        Order order = dummyOrder();
        order.setStatus("paid");
        
        OrderVerificationDto verificationDto = new OrderVerificationDto();
        verificationDto.setLocation("Store A");

        when(orderMapper.selectById(1)).thenReturn(order);
        when(productClient.getMagicBagById(1)).thenReturn(Result.success(dummyBag()));
        when(orderVerificationMapper.insert(any(OrderVerification.class))).thenReturn(1);
        when(orderMapper.updateById(any(Order.class))).thenReturn(1);

        orderService.verifyOrder(1, verificationDto, merchantContext());

        verify(orderVerificationMapper, times(1)).insert(any(OrderVerification.class));
        verify(orderMapper, times(1)).updateById(argThat(o -> 
            "completed".equals(o.getStatus())
        ));
    }

    /** -------------------------
     *  Test: getOrderStats
     *  ------------------------- */
    @Test
    void testGetOrderStats_Admin() {
        OrderStatsDto stats = new OrderStatsDto();
        stats.setTotalOrders(100L);
        stats.setTotalRevenue(BigDecimal.valueOf(5000.0));

        when(orderMapper.findAllOrderStats()).thenReturn(stats);

        OrderStatsDto result = orderService.getOrderStats(adminContext());

        assertNotNull(result);
        assertEquals(100L, result.getTotalOrders());
        verify(orderMapper, times(1)).findAllOrderStats();
    }

    /** -------------------------
     *  Test: createOrderFromCart
     *  ------------------------- */
    @Test
    void testCreateOrderFromCart() {
        CartDto cart = dummyCart();

        when(cartClient.getActiveCart(3)).thenReturn(Result.success(cart));
        when(productClient.getMagicBagById(1)).thenReturn(Result.success(dummyBag()));
        when(merchantClient.getMerchantById(2)).thenReturn(Result.success(dummyMerchant()));
        
        doAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId(1);
            return 1;
        }).when(orderMapper).insert(any(Order.class));
        
        when(orderItemMapper.insert(any(OrderItem.class))).thenReturn(1);
        when(cartClient.clearCart(3)).thenReturn(Result.success(null));

        OrderDto result = orderService.createOrderFromCart(3);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("cart", result.getOrderType());
        verify(orderMapper, times(1)).insert(any(Order.class));
        verify(orderItemMapper, times(1)).insert(any(OrderItem.class));
        verify(cartClient, times(1)).clearCart(3);
    }

    /** -------------------------
     *  Test: createOrderFromCart (empty cart)
     *  ------------------------- */
    @Test
    void testCreateOrderFromCart_EmptyCart() {
        CartDto emptyCart = new CartDto(1, 3, new ArrayList<>(), 0.0);

        when(cartClient.getActiveCart(3)).thenReturn(Result.success(emptyCart));

        assertThrows(BusinessException.class, () -> 
            orderService.createOrderFromCart(3)
        );
    }
}