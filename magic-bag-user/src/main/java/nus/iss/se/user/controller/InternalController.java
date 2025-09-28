package nus.iss.se.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nus.iss.se.common.Result;
import nus.iss.se.user.entity.User;
import nus.iss.se.user.service.IUserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("api/inner/user")
@RequiredArgsConstructor
public class InternalController {
    private final IUserService userService;

    /**
     * auth模块调用,查user信息
     * */
    @GetMapping("/{username}")
    public Result<User> getUserByUsername(@PathVariable String username) {
        User user = userService.findByUsername(username);
        log.info("Auth service check user:{}-{}; ",username,user);

        return Result.success(user);
    }

    @GetMapping("/activate/{username}")
    Result<Void> activateUser(@PathVariable("username") String username){
        userService.activateUser(username);
        return Result.success();
    }
}
