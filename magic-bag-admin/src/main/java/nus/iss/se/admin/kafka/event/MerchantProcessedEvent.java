package nus.iss.se.admin.kafka.event;

import lombok.Data;

import java.util.Date;

@Data
public class MerchantProcessedEvent {
    private Long userId;
    private Integer status;
    private Long operatorId;
    private Date endAt;
    private String reason; // 拒绝理由
}
