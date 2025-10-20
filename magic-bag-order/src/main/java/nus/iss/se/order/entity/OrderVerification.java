package nus.iss.se.order.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("order_verifications")
public class OrderVerification {
    @TableId
    private Integer id;
    private Integer orderId;
    private Integer verifiedBy;
    private Date verifiedAt;
    private String location;
}
