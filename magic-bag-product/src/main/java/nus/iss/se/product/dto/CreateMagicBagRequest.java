package nus.iss.se.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Date;

/**
 * 创建产品请求DTO
 */
@Data
@Schema(description = "创建产品请求")
public class CreateMagicBagRequest {
    
    @NotNull(message = "商家ID不能为空")
    @Schema(description = "商家ID", example = "1")
    private Integer merchantId;
    
    @NotBlank(message = "产品标题不能为空")
    @Size(max = 100, message = "产品标题长度不能超过100个字符")
    @Schema(description = "产品标题", example = "今日面包盲盒")
    private String title;
    
    @Size(max = 500, message = "产品描述长度不能超过500个字符")
    @Schema(description = "产品描述", example = "新鲜面包组合，包含多种口味")
    private String description;
    
    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.01", message = "价格必须大于0")
    @DecimalMax(value = "9999.99", message = "价格不能超过9999.99")
    @Schema(description = "产品价格", example = "15.50")
    private BigDecimal price;
    
    @NotNull(message = "库存数量不能为空")
    @Min(value = 1, message = "库存数量必须大于0")
    @Max(value = 9999, message = "库存数量不能超过9999")
    @Schema(description = "库存数量", example = "20")
    private Integer quantity;
    
    @NotNull(message = "自提开始时间不能为空")
    @Schema(description = "自提开始时间", example = "18:00")
    private LocalTime pickupStartTime;
    
    @NotNull(message = "自提结束时间不能为空")
    @Schema(description = "自提结束时间", example = "20:00")
    private LocalTime pickupEndTime;
    
    @NotNull(message = "有效日期不能为空")
    @Schema(description = "有效日期", example = "2024-01-15")
    private Date availableDate;
    
    @Size(max = 50, message = "分类名称长度不能超过50个字符")
    @Schema(description = "产品分类", example = "面包")
    private String category;
    
    @Size(max = 255, message = "图片URL长度不能超过255个字符")
    @Schema(description = "产品图片URL", example = "https://example.com/bread.jpg")
    private String imageUrl;
}


