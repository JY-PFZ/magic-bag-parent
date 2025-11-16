package nus.iss.se.user.controller;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import nus.iss.se.common.Result;
import nus.iss.se.common.cache.UserCacheService;
import nus.iss.se.user.dto.UpdateRoleQo;
import nus.iss.se.user.entity.User;
import nus.iss.se.user.service.IUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("api/inner/user")
public class InternalController {
    private static final Logger log = LoggerFactory.getLogger(InternalController.class);
    private final IUserService userService;
    private final UserCacheService userCacheService;
    
    public InternalController(IUserService userService, UserCacheService userCacheService) {
        this.userService = userService;
        this.userCacheService = userCacheService;
    }

    /**
     * auth模块调用,查user信息
     * */
    @GetMapping("/{username}")
    public Result<User> getUserByUsername(@PathVariable String username) {
        User user = userService.findByUsername(username);
        log.info("Auth service check user: {} - {}", username != null ? username.replaceAll("[\\r\\n]", "") : "null", user);

        return Result.success(user);
    }

    @PostMapping("/{userId}/roles")
    public Result<Void> updateUserRole(@PathVariable("userId") Long userId, @RequestBody UpdateRoleQo qo){
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getId, userId)
                .eq(User::getRole,qo.getOriRole())
                .set(User::getRole,qo.getExpectRole());

        userService.update(updateWrapper);
        return Result.success();
    }

    @PostMapping("/evict/{username}")
    public Result<Void> evictUser(@PathVariable("username") String username) {
        try {
            // 1. 清除用户缓存
            userCacheService.deleteUserCache(username);
            
            // 2. 记录日志
            log.info("User cache evicted successfully: {}", username != null ? username.replaceAll("[\\r\\n]", "") : "null");
            
            return Result.success();
        } catch (Exception e) {
            log.error("Failed to evict user cache: {}", username != null ? username.replaceAll("[\\r\\n]", "") : "null", e);
            return Result.error("Failed to evict user cache: " + e.getMessage());
        }
    }

    @GetMapping("/activate/{username}")
    public Result<Void> activateUser(@PathVariable("username") String username) {
        userService.activateUser(username);
        return Result.success();
    }
}
