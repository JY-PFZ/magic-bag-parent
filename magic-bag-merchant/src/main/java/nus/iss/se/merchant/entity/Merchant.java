package nus.iss.se.merchant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("merchants")
public class Merchant {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer userId;
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



