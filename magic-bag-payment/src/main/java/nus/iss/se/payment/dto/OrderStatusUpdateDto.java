package nus.iss.se.payment.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class OrderStatusUpdateDto {
    @NotBlank(message = "订单状态不能为空")
    private String status;
    
    private String remark;
}
