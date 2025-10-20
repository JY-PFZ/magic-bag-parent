package nus.iss.se.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import nus.iss.se.common.Result;
import nus.iss.se.user.common.UserContextHolder;
import nus.iss.se.user.dto.RegisterReq;
import nus.iss.se.user.dto.UserDto;
import nus.iss.se.user.entity.User;
import nus.iss.se.user.service.IUserService;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;
    private final UserContextHolder userContextHolder;

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
    public Result<IPage<User>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String role) {

        IPage<User> page = null;
        return Result.success(page);
    }

    @PutMapping("profile")
    public Result<Void> editProfile(@RequestBody @Valid UserDto dto){
        userService.editUser(dto);
        return Result.success();
    }

    @GetMapping("profile/{username}")
    public Result<UserDto> getProfile(@PathVariable @NotBlank @Size(max = 50) String username){
        return Result.success(userService.getUserProfile(username));
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
