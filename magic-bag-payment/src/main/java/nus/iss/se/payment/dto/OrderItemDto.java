package nus.iss.se.payment.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemDto {
    private Integer id;
    private Integer orderId;
    private Integer magicBagId;
    private String magicBagTitle;
    private String magicBagImageUrl;
    private String magicBagCategory;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
}

