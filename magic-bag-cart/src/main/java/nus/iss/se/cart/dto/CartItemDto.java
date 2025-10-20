package nus.iss.se.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CartItemDto {
    private Integer itemId;
    private String bagName;
    private double price;
    private int quantity;
    private double subtotal;
}
