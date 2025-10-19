package nus.iss.se.product.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Merchant DTO
 * 用于返回商户信息
 */
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
