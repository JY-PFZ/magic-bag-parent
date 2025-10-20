package nus.iss.se.product.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * Merchant Location DTO
 * 用于返回商户地理位置信息
 */
@Data
public class MerchantLocationDto {
    private String id;
    private String name;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String unit;
    private Double distance;
}

