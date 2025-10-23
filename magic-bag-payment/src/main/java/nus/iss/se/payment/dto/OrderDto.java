package nus.iss.se.payment.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class OrderDto {
    private Integer id;
    private String orderNo;
    private Integer userId;
    private Integer bagId;
    private Integer quantity;
    private BigDecimal totalPrice;
    private String status;
    private String pickupCode;
    private Date pickupStartTime;
    private Date pickupEndTime;
    private Date createdAt;
    private Date paidAt;
    private Date completedAt;
    private Date cancelledAt;
    
    // 关联信息
    private String userName;
    private String bagTitle;
    private String merchantName;
    
    // 订单类型和明细
    private String orderType; // "single" 或 "cart"
    private List<OrderItemDto> orderItems; // 购物车订单的商品明细
}
