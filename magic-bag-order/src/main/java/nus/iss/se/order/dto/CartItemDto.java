package nus.iss.se.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CartItemDto {
    private Integer id;
    private Integer magicBagId; // 添加商品ID字段
    private String bagName;
    private double price;
    private int quantity;
    private double subtotal;
}

