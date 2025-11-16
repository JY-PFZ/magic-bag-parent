package nus.iss.se.order.api;

import nus.iss.se.common.Result;
import nus.iss.se.order.dto.MagicBagDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * Product 服务的 Feign 客户端
 */
@FeignClient(
    name = "magic-bag-product"
)
public interface ProductClient {
    
    /**
     * 根据 ID 获取盲盒详情
     */
    @GetMapping("/product/{id}")
    Result<MagicBagDto> getMagicBagById(@PathVariable("id") Integer id);
}