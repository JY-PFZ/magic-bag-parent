package nus.iss.se.payment.service;

import com.stripe.exception.StripeException;

import nus.iss.se.payment.dto.OrderDto;
import nus.iss.se.payment.dto.PaymentResponseDto;

/**
 * 支付服务接口
 */
public interface IPaymentService {
    
    /**
     * 创建 Stripe Checkout Session
     */
    PaymentResponseDto createCheckoutSession(Integer orderId) throws StripeException;
    
    /**
     * 验证支付状态并更新订单
     */
    PaymentResponseDto verifyAndUpdatePayment(Integer orderId, String sessionId);

}