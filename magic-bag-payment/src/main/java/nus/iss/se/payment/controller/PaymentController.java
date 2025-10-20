package nus.iss.se.payment.controller;

import com.stripe.exception.StripeException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nus.iss.se.payment.dto.PaymentResponseDto;
import nus.iss.se.payment.service.IPaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 支付控制器
 */
@Slf4j
@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {
    
    private final IPaymentService paymentService;
    
    /**
     * 创建支付会话
     */
    @PostMapping("/checkout")
    @Operation(summary = "Create Stripe Checkout Session")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment session created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid order ID or request"),
            @ApiResponse(responseCode = "500", description = "Stripe API or server error")
    })
    public ResponseEntity<PaymentResponseDto> createCheckout(
            @Parameter(description = "The ID of the order to be paid", required = true)
            @RequestParam Integer orderId) {
        
        log.info("Creating checkout session for order: {}", orderId);
        
        try {
            PaymentResponseDto response = paymentService.createCheckoutSession(orderId);
            
            if (response.isSuccess()) {
                log.info("Checkout session created successfully: orderId={}", orderId);
                return ResponseEntity.ok(response);
            } else {
                log.warn("Failed to create checkout session: orderId={}, message={}", 
                        orderId, response.getMessage());
                return ResponseEntity.badRequest().body(response);
            }
        } catch (StripeException e) {
            log.error("Stripe exception for order {}: {}", orderId, e.getMessage(), e);
            PaymentResponseDto response = new PaymentResponseDto();
            response.setSuccess(false);
            response.setMessage("Stripe exception: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } catch (Exception e) {
            log.error("Unexpected error for order {}: {}", orderId, e.getMessage(), e);
            PaymentResponseDto response = new PaymentResponseDto();
            response.setSuccess(false);
            response.setMessage("Unexpected error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 验证支付结果并更新订单状态
     * 前端在 Stripe 重定向回来后调用这个接口
     */
    @PostMapping("/verify")
    @Operation(summary = "Verify payment and update order status")
    public ResponseEntity<PaymentResponseDto> verifyPayment(
            @RequestParam Integer orderId,
            @RequestParam String sessionId) {
        
        log.info("Verifying payment for order {} with session {}", orderId, sessionId);
        
        try {
            PaymentResponseDto response = paymentService.verifyAndUpdatePayment(orderId, sessionId);
            
            if (response.isSuccess()) {
                log.info("Payment verified successfully: orderId={}", orderId);
                return ResponseEntity.ok(response);
            } else {
                log.warn("Payment verification failed: orderId={}, message={}", 
                        orderId, response.getMessage());
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("Error verifying payment: orderId={}, error={}", orderId, e.getMessage(), e);
            PaymentResponseDto response = new PaymentResponseDto();
            response.setSuccess(false);
            response.setMessage("Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 支付成功回调页面
     * 注意：这个只是展示用，真正的状态更新在 /verify 接口
     */
    @GetMapping("/success")
    @Operation(summary = "Payment success page")
    public ResponseEntity<PaymentResponseDto> paymentSuccess(
            @Parameter(description = "Order ID", required = true)
            @RequestParam Integer orderId,
            @Parameter(description = "Stripe session ID")
            @RequestParam(required = false) String session_id) {
        
        log.info("Payment success callback for order {}", orderId);
        
        PaymentResponseDto response = new PaymentResponseDto();
        response.setSuccess(true);
        response.setMessage("Payment successful! Please verify your payment.");
        
        // 可以返回 sessionId 给前端，让前端调用 /verify
        if (session_id != null) {
            response.setCheckoutUrl(session_id);
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 支付取消页面
     */
    @GetMapping("/cancel")
    @Operation(summary = "Payment cancel page")
    public ResponseEntity<PaymentResponseDto> paymentCancel(
            @Parameter(description = "Order ID", required = true)
            @RequestParam Integer orderId) {
        
        log.info("Payment cancelled for order {}", orderId);
        
        PaymentResponseDto response = new PaymentResponseDto();
        response.setSuccess(false);
        response.setMessage("Payment was cancelled");
        
        return ResponseEntity.ok(response);
    }
}