package nus.iss.se.order.api;

import nus.iss.se.common.Result;
import nus.iss.se.order.dto.MerchantDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Merchant 服务的 Feign 客户端
 */
@FeignClient(
    name = "magic-bag-product" ,  contextId = "merchantClient", path = "/product/merchants"
)
public interface MerchantClient {
    
    /**
     * 根据 ID 获取商户信息
     */
    @GetMapping("/{id}")
    Result<MerchantDto> getMerchantById(@PathVariable("id") Integer id);
}