package nus.iss.se.payment.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MerchantDto {
    private Integer id;
    private String name;
    private String phone;
    private String address;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;
}




