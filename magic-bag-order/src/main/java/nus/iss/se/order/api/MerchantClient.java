package nus.iss.se.order.api;

import nus.iss.se.common.Result;
import nus.iss.se.order.dto.MerchantDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Merchant服务Feign客户端
 * 用于订单服务调用商户信息
 */
@FeignClient(name = "magic-bag-merchant")
public interface MerchantClient {

    /**
     * 根据商户ID获取商户信息
     * @param id 商户ID
     * @return 商户信息
     */
    @GetMapping("/merchant/{id}")
    Result<MerchantDto> getMerchantById(@PathVariable("id") Integer id);
}