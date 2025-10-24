package nus.iss.se.payment.service.impl;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nus.iss.se.common.Result;
import nus.iss.se.common.constant.ResultStatus;
import nus.iss.se.payment.api.OrderClient;
import nus.iss.se.payment.api.ProductClient;
import nus.iss.se.payment.dto.*;
import nus.iss.se.payment.service.IPaymentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 支付服务实现类
 * 职责：处理支付逻辑，与 Stripe 交互
 * 订单状态更新通过 OrderClient (Feign) 调用 order-service
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements IPaymentService {
    
    private final ProductClient productClient;
    private final OrderClient orderClient;  // ✅ 通过 Feign 调用 order-service
    
    @Value("${stripe.api.key}")
    private String stripeApiKey;
    
    @Value("${app.pay-url}")
    private String payUrl;
    
    private boolean stripeInitialized = false;
    
    /**
     * 初始化 Stripe
     */
    private void initStripe() {
        if (!stripeInitialized) {
            Stripe.apiKey = stripeApiKey;
            stripeInitialized = true;
            log.info("Stripe API initialized");
        }
    }
    
    @Override
    public PaymentResponseDto createCheckoutSession(Integer orderId) throws StripeException {
        initStripe();
        
        PaymentResponseDto response = new PaymentResponseDto();
        
        // 通过 Feign 从 order-service 获取订单信息
        Result<OrderDto> orderResult = orderClient.getOrderById(orderId);
        if (!isResultSuccess(orderResult) || orderResult.getData() == null) {
            log.error("Order not found: {}", orderId);
            response.setSuccess(false);
            response.setMessage(ResultStatus.ORDER_NOT_FOUND.getMessage());
            return response;
        }
        
        OrderDto order = orderResult.getData();
        
        if (order.getTotalPrice() == null || order.getQuantity() == null) {
            log.error("Order has invalid data: orderId={}", orderId);
            response.setSuccess(false);
            response.setMessage(ResultStatus.ORDER_INVALID_AMOUNT.getMessage());
            return response;
        }
        
        // 检查订单状态
        if ("paid".equals(order.getStatus())) {
            log.warn("Order already paid: {}", orderId);
            response.setSuccess(false);
            response.setMessage(ResultStatus.ORDER_ALREADY_PAID.getMessage());
            return response;
        }
        
        // 验证金额
        BigDecimal totalPrice = order.getTotalPrice();
        if (totalPrice.compareTo(BigDecimal.ZERO) <= 0) {
            log.error("Invalid order amount: orderId={}, amount={}", orderId, totalPrice);
            response.setSuccess(false);
            response.setMessage(ResultStatus.ORDER_INVALID_AMOUNT.getMessage());
            return response;
        }
        
        // 转换为分（Stripe 要求）
        long amountInCents = totalPrice.multiply(BigDecimal.valueOf(100)).longValue();
        log.info("Creating checkout session for order {}: amount={} SGD ({} cents)", 
                orderId, totalPrice, amountInCents);
        
        // 通过 Product 服务获取产品标题
        String bagTitle = "Magic Bag";
        if (order.getBagId() != null) {
            try {
                Result<MagicBagDto> bagResult = productClient.getMagicBagById(order.getBagId());
                if (isResultSuccess(bagResult) && bagResult.getData() != null) {
                    bagTitle = bagResult.getData().getTitle();
                    log.info("Product title retrieved: {}", bagTitle);
                } else {
                    log.warn("Failed to get product title for bagId: {}", order.getBagId());
                }
            } catch (Exception e) {
                log.error("Error fetching product title: {}", e.getMessage());
            }
        }
        
        // 创建 Stripe Checkout Session
        try {
            SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(payUrl + "/payment/success?orderId=" + orderId + "&session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl(payUrl + "/payment/cancel?orderId=" + orderId)
                    .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                    .putMetadata("orderId", orderId.toString())
                    .putMetadata("orderNo", order.getOrderNo());
            
            // 根据订单类型创建不同的支付项目
            if ("cart".equals(order.getOrderType()) && order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
                // 购物车订单：为每个商品创建支付项目
                for (OrderItemDto item : order.getOrderItems()) {
                    String itemTitle = item.getMagicBagTitle() != null ? item.getMagicBagTitle() : "Magic Bag";
                    long unitAmountInCents = item.getUnitPrice().multiply(BigDecimal.valueOf(100)).longValue();
                    
                    paramsBuilder.addLineItem(SessionCreateParams.LineItem.builder()
                            .setQuantity(item.getQuantity().longValue())
                            .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                    .setCurrency("sgd")
                                    .setUnitAmount(unitAmountInCents)
                                    .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                            .setName(itemTitle)
                                            .build())
                                    .build())
                            .build());
                }
            } else {
                // 单商品订单：使用原有逻辑
                long unitAmountInCents = amountInCents / order.getQuantity();
                
                paramsBuilder.addLineItem(SessionCreateParams.LineItem.builder()
                        .setQuantity(order.getQuantity().longValue())
                        .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("sgd")
                                .setUnitAmount(unitAmountInCents)
                                .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName(bagTitle)
                                        .build())
                                .build())
                        .build());
            }
            
            SessionCreateParams params = paramsBuilder.build();
            
            Session session = Session.create(params);
            
            if (session != null && session.getUrl() != null) {
                response.setSuccess(true);
                response.setCheckoutUrl(session.getUrl());
                response.setMessage("Checkout session created successfully");
                log.info("Checkout session created successfully: orderId={}, sessionId={}", 
                        orderId, session.getId());
            } else {
                response.setSuccess(false);
                response.setMessage(ResultStatus.PAYMENT_FAILED.getMessage());
                log.error("Failed to create Stripe session for order: {}", orderId);
            }
        } catch (StripeException e) {
            log.error("Stripe error when creating checkout session for order {}: {}", 
                    orderId, e.getMessage(), e);
            throw e;
        }
        
        return response;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentResponseDto verifyAndUpdatePayment(Integer orderId, String sessionId) {
        initStripe();
        
        PaymentResponseDto response = new PaymentResponseDto();
        
        try {
            // 从 order-service 获取订单信息
            Result<OrderDto> orderResult = orderClient.getOrderById(orderId);
            if (!isResultSuccess(orderResult) || orderResult.getData() == null) {
                log.error("Order not found when verifying payment: {}", orderId);
                response.setSuccess(false);
                response.setMessage(ResultStatus.ORDER_NOT_FOUND.getMessage());
                return response;
            }
            
            OrderDto order = orderResult.getData();
            
            // 如果已经支付过了，直接返回成功
            if ("paid".equals(order.getStatus())) {
                log.info("Order already paid: {}", orderId);
                response.setSuccess(true);
                response.setMessage(ResultStatus.ORDER_ALREADY_PAID.getMessage());
                return response;
            }
            
            // 验证 sessionId
            if (sessionId == null || sessionId.isEmpty()) {
                log.error("Session ID is empty for order: {}", orderId);
                response.setSuccess(false);
                response.setMessage(ResultStatus.PAYMENT_SESSION_INVALID.getMessage());
                return response;
            }
            
            // 从 Stripe 服务器验证支付状态
            log.info("Verifying payment with Stripe: orderId={}, sessionId={}", orderId, sessionId);
            Session session = Session.retrieve(sessionId);
            
            // 验证 metadata 中的 orderId 是否匹配
            String metadataOrderId = session.getMetadata().get("orderId");
            if (!orderId.toString().equals(metadataOrderId)) {
                log.error("Order ID mismatch: expected={}, got={}", orderId, metadataOrderId);
                response.setSuccess(false);
                response.setMessage(ResultStatus.PAYMENT_SESSION_INVALID.getMessage() + 
                                  " (Order ID mismatch)");
                return response;
            }
            
            // 验证支付状态
            String sessionStatus = session.getStatus();
            String paymentStatus = session.getPaymentStatus();
            log.info("Stripe session status: orderId={}, sessionStatus={}, paymentStatus={}", 
                    orderId, sessionStatus, paymentStatus);
            
            if ("complete".equals(sessionStatus) && "paid".equals(paymentStatus)) {
                // 支付成功，通过 Feign 调用 order-service 更新订单状态
                try {
                    Result<Void> updateResult = orderClient.updateOrderStatus(orderId, "paid");
                    
                    if (isResultSuccess(updateResult)) {
                        response.setSuccess(true);
                        response.setMessage("Payment verified and order updated successfully");
                        log.info("Order {} payment verified and status updated to 'paid'", orderId);
                    } else {
                        response.setSuccess(false);
                        response.setMessage("Failed to update order status");
                        log.error("Failed to update order status for orderId: {}", orderId);
                    }
                } catch (Exception e) {
                    log.error("Error updating order status through Feign: orderId={}, error={}", orderId, e.getMessage(), e);
                    response.setSuccess(false);
                    response.setMessage("Failed to update order status: " + e.getMessage());
                }
            } else {
                // 支付未完成
                response.setSuccess(false);
                response.setMessage(ResultStatus.PAYMENT_VERIFICATION_FAILED.getMessage() + 
                                  ": Session status - " + sessionStatus);
                log.warn("Payment not completed for order {}: sessionStatus={}, paymentStatus={}", 
                        orderId, sessionStatus, paymentStatus);
            }
            
        } catch (StripeException e) {
            log.error("Stripe error when verifying payment for order {}: {}", 
                    orderId, e.getMessage(), e);
            response.setSuccess(false);
            response.setMessage(ResultStatus.PAYMENT_FAILED.getMessage() + ": " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error when verifying payment for order {}: {}", 
                    orderId, e.getMessage(), e);
            response.setSuccess(false);
            response.setMessage(ResultStatus.SERVICE_ERROR.getMessage() + ": " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * 判断 Result 是否成功的辅助方法
     */
    private boolean isResultSuccess(Result<?> result) {
        return result != null && result.getCode() == ResultStatus.SUCCESS.getCode();
    }

}