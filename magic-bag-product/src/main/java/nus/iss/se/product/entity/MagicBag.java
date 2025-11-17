package nus.iss.se.product.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

/**
 * MagicBag 实体类
 * 对应数据库表 magic_bags
 */
@Data
@TableName("magic_bags")
public class MagicBag {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer merchantId;
    private String title;
    private String description;
    private Float price;
    private Integer quantity;
    private LocalTime pickupStartTime;
    private LocalTime pickupEndTime;
    private Date availableDate;
    private String category;
    private String imageUrl;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}


