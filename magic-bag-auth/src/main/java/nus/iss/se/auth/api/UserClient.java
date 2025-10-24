package nus.iss.se.auth.api;

import nus.iss.se.auth.entity.User;
import nus.iss.se.common.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "magic-bag-user")
public interface UserClient {

    @GetMapping("api/inner/user/{username}")
    Result<User> getUserByUsername(@PathVariable("username") String username);

    @GetMapping("api/inner/user/activate/{username}")
    Result<Void> activateUser(@PathVariable("username") String username);

    @PostMapping("api/inner/user/evict/{username}")
    Result<Void> evictUser(@PathVariable("username") String username);
}
