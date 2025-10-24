package nus.iss.se.admin.kafka.event;

import lombok.Data;

@Data
public class MerchantRegisterEvent {
    private Long userId;
    private Long merchantId;
    private String shopName;
    private String contactInfo;
}
