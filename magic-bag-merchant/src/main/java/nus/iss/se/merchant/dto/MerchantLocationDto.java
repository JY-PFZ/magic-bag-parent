package nus.iss.se.merchant.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class MerchantLocationDto {
    private String id;
    private String name;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String unit;
    private Double distance;
}



