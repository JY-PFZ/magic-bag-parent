package nus.iss.se.merchant.kafka.event;

import lombok.Data;

@Data
public class MerchantRegisterEvent {
    private Integer userId;
    private Integer merchantId;
    private String shopName;
    private String contactInfo;
}
