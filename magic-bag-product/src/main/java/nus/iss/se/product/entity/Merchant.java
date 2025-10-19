package nus.iss.se.product.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Merchant 实体类
 * 对应数据库表 merchants
 */
@Data
@TableName("merchants")
public class Merchant {
    @TableId
    private Integer id;
    private String name;
    private String phone;
    private String passwordHash;
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
