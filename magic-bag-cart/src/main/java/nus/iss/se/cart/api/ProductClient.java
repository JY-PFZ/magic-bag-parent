package nus.iss.se.cart.api;

import nus.iss.se.cart.dto.MagicBagDto;
import nus.iss.se.common.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    @GetMapping("/product/magic-bags/{id}")
    Result<MagicBagDto> getMagicBagById(@PathVariable("id") Integer id);
    
    /**
     * 批量查询盲盒（需要 Product 服务提供此接口）
     */
    @PostMapping("/product/magic-bags/batch-query")
    Result<List<MagicBagDto>> getBatchMagicBags(@RequestBody List<Integer> ids);
}