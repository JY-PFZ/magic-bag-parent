package nus.iss.se.merchant.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
public class MerchantUpdateDto {
    @NotBlank(message = "商家名称不能为空")
    @Size(max = 100, message = "商家名称长度不能超过100个字符")
    private String name;

    @NotBlank(message = "联系手机号不能为空")
    @Size(max = 15, message = "手机号长度不能超过15个字符")
    private String phone;

    @NotBlank(message = "营业执照不能为空")
    private String businessLicense;

    @NotBlank(message = "详细地址不能为空")
    @Size(max = 255, message = "地址长度不能超过255个字符")
    private String address;
}



