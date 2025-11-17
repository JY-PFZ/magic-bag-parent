package nus.iss.se.order.api;

import nus.iss.se.common.Result;
import nus.iss.se.order.dto.CartDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "magic-bag-cart")
public interface CartClient {

    /**
     * 获取用户购物车（对应 CartController 的 GET /cart/{userId}）
     */
    @GetMapping("/cart/{userId}")
    Result<CartDto> getActiveCart(@PathVariable("userId") Integer userId);

    /**
     * 清空购物车（对应 DELETE /cart/{userId}/items）
     */
    @DeleteMapping("/cart/{userId}/items")
    Result<Void> clearCart(@PathVariable("userId") Integer userId);
}
