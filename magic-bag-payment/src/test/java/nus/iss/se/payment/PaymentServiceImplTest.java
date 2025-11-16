package nus.iss.se.payment;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
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
        // 设置配置属性
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
     *  Test: createCheckoutSession (invalid amount)
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
     *  Test: createCheckoutSession (single order with product)
     *  注意：实际调用 Stripe API 需要 mock，这里只测试逻辑
     *  ------------------------- */
    @Test
    void testCreateCheckoutSession_SingleOrder() throws StripeException {
        OrderDto order = dummySingleOrder();
        MagicBagDto bag = dummyBag();

        when(orderClient.getOrderById(1)).thenReturn(Result.success(order));
        when(productClient.getMagicBagById(1)).thenReturn(Result.success(bag));

        // 注意：由于 Stripe.apiKey 和 Session.create() 是静态方法，
        // 实际测试会调用真实的 Stripe API。在真实环境中需要使用 
        // PowerMock 或者 mock server 来模拟 Stripe API
        // 这里我们测试会抛出异常，因为 mock key 无效
        assertThrows(StripeException.class, () -> {
            paymentService.createCheckoutSession(1);
        });

        verify(orderClient, times(1)).getOrderById(1);
        verify(productClient, times(1)).getMagicBagById(1);
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
    }

    /** -------------------------
     *  Test: verifyAndUpdatePayment (stripe verification)
     *  注意：实际需要 mock Stripe Session.retrieve()
     *  ------------------------- */
    @Test
    void testVerifyAndUpdatePayment_StripeError() {
        OrderDto order = dummySingleOrder();

        when(orderClient.getOrderById(1)).thenReturn(Result.success(order));

        // 由于 Session.retrieve() 是静态方法，会尝试调用真实 Stripe API
        // 在实际测试环境中应该使用 PowerMock 或 WireMock 来模拟
        PaymentResponseDto result = paymentService.verifyAndUpdatePayment(1, "invalid_session");

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Payment failed"));
    }

    /** -------------------------
     *  Test: Cart order with multiple items
     *  ------------------------- */
    @Test
    void testCreateCheckoutSession_CartOrder() throws StripeException {
        OrderDto cartOrder = dummyCartOrder();

        when(orderClient.getOrderById(2)).thenReturn(Result.success(cartOrder));

        // 会抛出 StripeException 因为使用 mock key
        assertThrows(StripeException.class, () -> {
            paymentService.createCheckoutSession(2);
        });

        verify(orderClient, times(1)).getOrderById(2);
    }

    /** -------------------------
     *  Test: Product client fails gracefully
     *  ------------------------- */
    @Test
    void testCreateCheckoutSession_ProductClientFails() throws StripeException {
        OrderDto order = dummySingleOrder();

        when(orderClient.getOrderById(1)).thenReturn(Result.success(order));
        when(productClient.getMagicBagById(1)).thenReturn(Result.error(ResultStatus.PRODUCT_NOT_FOUND));

        // 即使产品服务失败，支付创建应该继续（使用默认标题）
        assertThrows(StripeException.class, () -> {
            paymentService.createCheckoutSession(1);
        });

        verify(productClient, times(1)).getMagicBagById(1);
    }
}