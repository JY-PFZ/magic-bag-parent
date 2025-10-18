package nus.iss.se.merchant.api;

import nus.iss.se.common.Result;
import nus.iss.se.merchant.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "magic-bag-user")
public interface UserClient {

    @GetMapping("/user/profile/{username}")
    Result<UserDto> getUserByUsername(@PathVariable("username") String username);
}
