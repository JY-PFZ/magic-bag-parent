package nus.iss.se.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Validated
@RestController
@RequestMapping("/user")
@Tag(name = "User API", description = "User service")
public class UserController {

    private final IUserService userService;
    private final UserContextHolder userContextHolder;
    
    public UserController(IUserService userService, UserContextHolder userContextHolder) {
        this.userService = userService;
        this.userContextHolder = userContextHolder;
    }

    @GetMapping("/hello")
    public Result<String> hello(){
        return Result.success("hello spring security:"+userContextHolder.getCurrentUser());
    }

    @PostMapping("/register")
    public Result<Void> register(@RequestBody @Valid RegisterReq req){
        userService.register(req);
        return Result.success();
    }

    @GetMapping
    @Operation(summary = "Get current user's profile", description = "Fetches profile information for the currently logged-in user.")
    public Result<User> getCurrentUserProfile() {
        // 从 Spring Security 上下文中获取当前用户信息
        UserContext currentUserContext = userContextHolder.getCurrentUser();
        
        if (currentUserContext == null || currentUserContext.getId() == null) {
            return Result.error(ResultStatus.USER_NOT_FOUND.getCode(), "User not found in security context. Please log in again.");
        }
        
        User user = userService.getById(currentUserContext.getId());
        
        if (user != null) {
            user.setPassword(null);
        } else {
            return Result.error(ResultStatus.USER_NOT_FOUND.getCode(), "User profile not found in database.");
        }
        
        return Result.success(user);
    }

    @PutMapping("profile")
    public Result<Void> editProfile(@RequestBody @Valid UserDto dto){
        userService.editUser(dto);
        return Result.success();
    }

    @GetMapping("/list")
    @Operation(summary = "Get user list", description = "Get paginated list of users with optional role filter")
    public Result<IPage<User>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String role) {
        
        IPage<User> page = userService.getUserList(pageNum, pageSize, role);
        return Result.success(page);
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
