package nus.iss.se.order.api;

import nus.iss.se.common.Result;
import nus.iss.se.order.dto.CartDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * Cart 服务的 Feign 客户端
 */
@FeignClient(name = "magic-bag-cart")
public interface CartClient {
    
    /**
     * 获取用户购物车
     */
    @GetMapping("/cart/active")
    Result<CartDto> getActiveCart(@RequestParam("userId") Integer userId);
    
    /**
     * 清空购物车
     */
    @DeleteMapping("/cart/clear")
    Result<Void> clearCart(@RequestParam("userId") Integer userId);
}

