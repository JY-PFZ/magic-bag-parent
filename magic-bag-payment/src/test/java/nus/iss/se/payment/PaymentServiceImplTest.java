package nus.iss.se.payment;

import com.stripe.exception.StripeException;
import nus.iss.se.common.Result;
import nus.iss.se.common.constant.ResultStatus;
import nus.iss.se.payment.api.OrderClient;
import nus.iss.se.payment.api.ProductClient;
import nus.iss.se.payment.dto.MagicBagDto;
import nus.iss.se.payment.dto.OrderDto;
import nus.iss.se.payment.dto.OrderItemDto;
import nus.iss.se.payment.dto.PaymentResponseDto;
import nus.iss.se.payment.service.impl.PaymentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PaymentServiceImplTest {

    @Mock
    private ProductClient productClient;

    @Mock
    private OrderClient orderClient;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(paymentService, "stripeApiKey", "sk_test_mock_key");
        ReflectionTestUtils.setField(paymentService, "payUrl", "http://localhost:3000");
    }

    private OrderDto dummySingleOrder() {
        OrderDto order = new OrderDto();
        order.setId(1);
        order.setOrderNo("ORD123456789");
        order.setUserId(1);
        order.setBagId(1);
        order.setOrderType("single");
        order.setTotalPrice(BigDecimal.valueOf(50.00));
        order.setQuantity(2);
        order.setStatus("pending");
        return order;
    }

    private OrderDto dummyCartOrder() {
        OrderDto order = new OrderDto();
        order.setId(2);
        order.setOrderNo("ORD987654321");
        order.setUserId(1);
        order.setOrderType("cart");
        order.setTotalPrice(BigDecimal.valueOf(100.00));
        order.setQuantity(3);
        order.setStatus("pending");

        OrderItemDto item1 = new OrderItemDto();
        item1.setMagicBagId(1);
        item1.setMagicBagTitle("Bag 1");
        item1.setQuantity(2);
        item1.setUnitPrice(BigDecimal.valueOf(25.00));

        OrderItemDto item2 = new OrderItemDto();
        item2.setMagicBagId(2);
        item2.setMagicBagTitle("Bag 2");
        item2.setQuantity(1);
        item2.setUnitPrice(BigDecimal.valueOf(50.00));

        order.setOrderItems(Arrays.asList(item1, item2));
        return order;
    }

    private MagicBagDto dummyBag() {
        MagicBagDto dto = new MagicBagDto();
        dto.setId(1);
        dto.setTitle("Test Magic Bag");
        dto.setPrice(BigDecimal.valueOf(25.0));
        return dto;
    }

    /** -------------------------
     *  Test: createCheckoutSession (order not found)
     *  ------------------------- */
    @Test
    void testCreateCheckoutSession_OrderNotFound() throws StripeException {
        when(orderClient.getOrderById(999)).thenReturn(Result.error(ResultStatus.ORDER_NOT_FOUND));

        PaymentResponseDto result = paymentService.createCheckoutSession(999);

        assertFalse(result.isSuccess());
        assertEquals(ResultStatus.ORDER_NOT_FOUND.getMessage(), result.getMessage());
        verify(orderClient, times(1)).getOrderById(999);
    }

    /** -------------------------
     *  Test: createCheckoutSession (order already paid)
     *  ------------------------- */
    @Test
    void testCreateCheckoutSession_OrderAlreadyPaid() throws StripeException {
        OrderDto order = dummySingleOrder();
        order.setStatus("paid");

        when(orderClient.getOrderById(1)).thenReturn(Result.success(order));

        PaymentResponseDto result = paymentService.createCheckoutSession(1);

        assertFalse(result.isSuccess());
        assertEquals(ResultStatus.ORDER_ALREADY_PAID.getMessage(), result.getMessage());
        verify(orderClient, times(1)).getOrderById(1);
    }

    /** -------------------------
     *  Test: createCheckoutSession (invalid amount - zero)
     *  ------------------------- */
    @Test
    void testCreateCheckoutSession_InvalidAmount() throws StripeException {
        OrderDto order = dummySingleOrder();
        order.setTotalPrice(BigDecimal.ZERO);

        when(orderClient.getOrderById(1)).thenReturn(Result.success(order));

        PaymentResponseDto result = paymentService.createCheckoutSession(1);

        assertFalse(result.isSuccess());
        assertEquals(ResultStatus.ORDER_INVALID_AMOUNT.getMessage(), result.getMessage());
    }

    /** -------------------------
     *  Test: createCheckoutSession (invalid amount - negative)
     *  ------------------------- */
    @Test
    void testCreateCheckoutSession_NegativeAmount() throws StripeException {
        OrderDto order = dummySingleOrder();
        order.setTotalPrice(BigDecimal.valueOf(-10.00));

        when(orderClient.getOrderById(1)).thenReturn(Result.success(order));

        PaymentResponseDto result = paymentService.createCheckoutSession(1);

        assertFalse(result.isSuccess());
        assertEquals(ResultStatus.ORDER_INVALID_AMOUNT.getMessage(), result.getMessage());
    }

    /** -------------------------
     *  Test: createCheckoutSession (null price)
     *  ------------------------- */
    @Test
    void testCreateCheckoutSession_NullPrice() throws StripeException {
        OrderDto order = dummySingleOrder();
        order.setTotalPrice(null);

        when(orderClient.getOrderById(1)).thenReturn(Result.success(order));

        PaymentResponseDto result = paymentService.createCheckoutSession(1);

        assertFalse(result.isSuccess());
        assertEquals(ResultStatus.ORDER_INVALID_AMOUNT.getMessage(), result.getMessage());
    }

    /** -------------------------
     *  Test: createCheckoutSession (null quantity)
     *  ------------------------- */
    @Test
    void testCreateCheckoutSession_NullQuantity() throws StripeException {
        OrderDto order = dummySingleOrder();
        order.setQuantity(null);

        when(orderClient.getOrderById(1)).thenReturn(Result.success(order));

        PaymentResponseDto result = paymentService.createCheckoutSession(1);

        assertFalse(result.isSuccess());
        assertEquals(ResultStatus.ORDER_INVALID_AMOUNT.getMessage(), result.getMessage());
    }

    /** -------------------------
     *  Test: verifyAndUpdatePayment (order not found)
     *  ------------------------- */
    @Test
    void testVerifyAndUpdatePayment_OrderNotFound() {
        when(orderClient.getOrderById(999)).thenReturn(Result.error(ResultStatus.ORDER_NOT_FOUND));

        PaymentResponseDto result = paymentService.verifyAndUpdatePayment(999, "session_123");

        assertFalse(result.isSuccess());
        assertEquals(ResultStatus.ORDER_NOT_FOUND.getMessage(), result.getMessage());
        verify(orderClient, times(1)).getOrderById(999);
    }

    /** -------------------------
     *  Test: verifyAndUpdatePayment (order already paid)
     *  ------------------------- */
    @Test
    void testVerifyAndUpdatePayment_OrderAlreadyPaid() {
        OrderDto order = dummySingleOrder();
        order.setStatus("paid");

        when(orderClient.getOrderById(1)).thenReturn(Result.success(order));

        PaymentResponseDto result = paymentService.verifyAndUpdatePayment(1, "session_123");

        assertTrue(result.isSuccess());
        assertEquals(ResultStatus.ORDER_ALREADY_PAID.getMessage(), result.getMessage());
        verify(orderClient, times(1)).getOrderById(1);
        verify(orderClient, never()).updateOrderStatus(anyInt(), anyString());
    }

    /** -------------------------
     *  Test: verifyAndUpdatePayment (empty session id)
     *  ------------------------- */
    @Test
    void testVerifyAndUpdatePayment_EmptySessionId() {
        OrderDto order = dummySingleOrder();

        when(orderClient.getOrderById(1)).thenReturn(Result.success(order));

        PaymentResponseDto result = paymentService.verifyAndUpdatePayment(1, "");

        assertFalse(result.isSuccess());
        assertEquals(ResultStatus.PAYMENT_SESSION_INVALID.getMessage(), result.getMessage());
        verify(orderClient, times(1)).getOrderById(1);
    }

    /** -------------------------
     *  Test: verifyAndUpdatePayment (null session id)
     *  ------------------------- */
    @Test
    void testVerifyAndUpdatePayment_NullSessionId() {
        OrderDto order = dummySingleOrder();

        when(orderClient.getOrderById(1)).thenReturn(Result.success(order));

        PaymentResponseDto result = paymentService.verifyAndUpdatePayment(1, null);

        assertFalse(result.isSuccess());
        assertEquals(ResultStatus.PAYMENT_SESSION_INVALID.getMessage(), result.getMessage());
        verify(orderClient, times(1)).getOrderById(1);
    }

    /** -------------------------
     *  Test: Multiple validation scenarios
     *  ------------------------- */
    @Test
    void testCreateCheckoutSession_MultipleInvalidScenarios() throws StripeException {
        // Test 1: Order with null order number should still work if other fields are valid
        OrderDto order1 = dummySingleOrder();
        order1.setOrderNo(null);
        order1.setTotalPrice(BigDecimal.ZERO);
        
        when(orderClient.getOrderById(1)).thenReturn(Result.success(order1));
        
        PaymentResponseDto result1 = paymentService.createCheckoutSession(1);
        assertFalse(result1.isSuccess());
        assertEquals(ResultStatus.ORDER_INVALID_AMOUNT.getMessage(), result1.getMessage());
    }

    /** -------------------------
     *  Test: Cart order data structure
     *  ------------------------- */
    @Test
    void testCartOrderStructure() {
        OrderDto cartOrder = dummyCartOrder();
        
        assertNotNull(cartOrder);
        assertEquals("cart", cartOrder.getOrderType());
        assertEquals(2, cartOrder.getOrderItems().size());
        assertEquals(BigDecimal.valueOf(100.00), cartOrder.getTotalPrice());
    }

    /** -------------------------
     *  Test: Single order data structure
     *  ------------------------- */
    @Test
    void testSingleOrderStructure() {
        OrderDto singleOrder = dummySingleOrder();
        
        assertNotNull(singleOrder);
        assertEquals("single", singleOrder.getOrderType());
        assertEquals(1, singleOrder.getBagId());
        assertEquals(BigDecimal.valueOf(50.00), singleOrder.getTotalPrice());
        assertEquals(2, singleOrder.getQuantity());
    }
}