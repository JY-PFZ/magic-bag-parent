package nus.iss.se.order.api;

import nus.iss.se.common.Result;
import nus.iss.se.order.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * User 服务的 Feign 客户端
 */
@FeignClient(
    name = "magic-bag-user"
)
public interface UserClient {
    
    /**
     * 根据 ID 获取用户信息
     */
    @GetMapping("/user/profile/id/{id}")
    Result<UserDto> getUserById(@PathVariable("id") Integer id);
}