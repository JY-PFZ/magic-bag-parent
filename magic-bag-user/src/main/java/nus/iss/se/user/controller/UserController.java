package nus.iss.se.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nus.iss.se.common.Result;
import nus.iss.se.common.cache.UserContext;
import nus.iss.se.common.constant.ResultStatus;
import nus.iss.se.user.common.UserContextHolder;
import nus.iss.se.user.dto.RegisterReq;
import nus.iss.se.user.dto.UserDto;
import nus.iss.se.user.entity.User;
import nus.iss.se.user.service.IUserService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Validated
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "User API", description = "User service")
public class UserController {

    private final IUserService userService;
    private final UserContextHolder userContextHolder;

    /**
     * **核心修复：这个新方法用于获取当前登录用户的个人资料**
     * 将 @GetMapping 注解从旧的 list() 方法移到这里
     */
    @GetMapping
    @Operation(summary = "Get current user's profile", description = "Fetches profile information for the currently logged-in user.")
    public Result<User> getCurrentUserProfile() {
        // 从 Spring Security 上下文中获取当前用户信息
        UserContext currentUserContext = userContextHolder.getCurrentUser();

        // 🟢 2. 【修复】检查 ID 而不是 Username，因为 ID 是可靠的
        if (currentUserContext == null || currentUserContext.getId() == null) {
            // 如果安全上下文中没有用户信息，返回错误
            // 🟢 3. 【修复】使用标准错误码返回
            return Result.error(ResultStatus.USER_NOT_FOUND.getCode(), "User not found in security context. Please log in again.");
        }

        // 🟢 4. 【修复】使用 getById (或 selectById) 而不是 findByUsername
        // 我们从 "createOrderFromCart" 接口得知 .getId() 是可靠的
        User user = userService.getById(currentUserContext.getId());

        // 安全措施：在将用户信息发送到前端之前，清除密码字段
        if (user != null) {
            user.setPassword(null);
        } else {
            // 🟢 5. 【修复】如果根据 ID 也找不到，说明数据有问题
            return Result.error(ResultStatus.USER_NOT_FOUND.getCode(), "User profile not found in database.");
        }

        return Result.success(user);
    }

    /**
     * **核心修复：修改或移除旧的 list() 方法的路径，以避免冲突**
     * 我们可以给它一个新的路径，例如 /list，或者暂时注释掉
     */
    @GetMapping("/list") // 路径已修改为 /api/user/list
    public Result<IPage<User>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String role) {

        // 这里的逻辑仍然是空的，但它至少不再占用 /api/user 这个重要的路径
        IPage<User> page = null;
        return Result.success(page);
    }

    @PutMapping("/profile")
    @Operation(summary = "Edit user info", description = "Edit user info, but not include password")
    public Result<Void> update(@RequestBody @Valid UserDto userDto) {
        userService.updateUserInfo(userDto);
        return Result.success();
    }

    @PostMapping("/register")
    @Operation(summary = "User register", description = "If user register, its account will be created with inactive status. Then an activate link will be sent to the user's email")
    public Result<String> register(@RequestBody @Valid RegisterReq req) {
        userService.register(req);
        return Result.success();
    }

    @GetMapping("profile/id/{id}")
    public Result<UserDto> getProfileById(@PathVariable Integer id){
        UserDto userDto = userService.getUserById(id);
        if (userDto == null) {
            return Result.error("User not found");
        }
        return Result.success(userDto);
    }
}
