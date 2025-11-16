package nus.iss.se.merchant.api;

import nus.iss.se.common.Result;
import nus.iss.se.merchant.dto.UpdateRoleQo;
import nus.iss.se.merchant.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "magic-bag-user")
public interface UserClient {

    @GetMapping("/user/profile/{username}")
    Result<UserDto> getUserByUsername(@PathVariable("username") String username);

    @PostMapping("api/inner/user/{userId}/roles")
    Result<Void> updateUserRole(@PathVariable("userId") Long userId, @RequestBody UpdateRoleQo qo);
}
