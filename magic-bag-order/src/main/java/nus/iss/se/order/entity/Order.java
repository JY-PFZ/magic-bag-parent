package nus.iss.se.order.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("orders")
public class Order {
    @TableId
    private Integer id;
    private String orderNo;
    private Integer userId;
    private Integer bagId;
    private Integer quantity;
    private BigDecimal totalPrice;
    private String status;
    private String pickupCode;
    private Date pickupStartTime;
    private Date pickupEndTime;
    private Date createdAt;
    private Date updatedAt;
    private Date paidAt;
    private Date completedAt;
    private Date cancelledAt;
    private String orderType; // "single" 或 "cart"
}
