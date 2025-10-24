package nus.iss.se.cart.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class MerchantDto {
    private Integer id;
    private String name;
    private String phone;
    private String businessLicense;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String status;
    private Double score;
    private Date createdAt;
    private Date updatedAt;
    private Date approvedAt;
}



