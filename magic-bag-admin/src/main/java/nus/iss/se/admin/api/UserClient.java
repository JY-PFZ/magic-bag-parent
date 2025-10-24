package nus.iss.se.admin.api;

import nus.iss.se.admin.dto.UserDto;
import nus.iss.se.common.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "magic-bag-user")
public interface UserClient {

    @GetMapping("/user/profile/{username}")
    Result<UserDto> getUserByUsername(@PathVariable("username") String username);
}
