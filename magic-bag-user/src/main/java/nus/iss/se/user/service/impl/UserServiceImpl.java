package nus.iss.se.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nus.iss.se.common.cache.UserCacheService;
import nus.iss.se.common.cache.UserContext;
import nus.iss.se.common.constant.ResultStatus;
import nus.iss.se.common.exception.BusinessException;
import nus.iss.se.kafka.event.EventEnvelope;
import nus.iss.se.kafka.publisher.KafkaEventPublisher;
import nus.iss.se.user.common.UserContextHolder;
import nus.iss.se.user.common.UserRole;
import nus.iss.se.user.common.UserStatus;
import nus.iss.se.user.dto.RegisterReq;
import nus.iss.se.user.dto.UserDto;
import nus.iss.se.user.entity.User;
import nus.iss.se.user.kafka.handler.EventTopicType;
import nus.iss.se.user.mapper.UserMapper;
import nus.iss.se.user.service.IUserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    private final PasswordEncoder passwordEncoder;
    private final KafkaEventPublisher eventPublisher;
    private final UserContextHolder userContextHolder;
    private final UserCacheService userCacheService;

    @Override
    public User findByUsername(String username) {
        return this.baseMapper.selectByUsername(username);
    }

    @Override
    public void register(RegisterReq req) {
        // 检查角色是否存在
        if (!UserRole.hasRole(req.getRole())) {
            throw new BusinessException(ResultStatus.USER_ROLE_NOT_FOUND, req.getRole());
        }

        User existUser = findByUsername(req.getUsername());
        if (existUser != null) {
            throw new BusinessException(ResultStatus.USER_HAS_EXISTED, req.getUsername());
        }

        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRole(req.getRole());
        this.save(user);

        EventEnvelope eventEnvelope = EventEnvelope.of(req.getUsername(), EventTopicType.USER_REGISTERED);
        eventPublisher.publish(eventEnvelope);
    }

    @Override
    public void updateUserInfo(UserDto dto) {
        UserContext currentUser = userContextHolder.getCurrentUser();
        // 1. 权限验证
        if (!Objects.equals(currentUser.getId(), dto.getId())) {
            throw new BusinessException(ResultStatus.ACCESS_DENIED, "无权限修改他人信息");
        }

        // **核心修复：将 "CUSTOMER" 添加为合法的角色**
        String userRole = currentUser.getRole();
        if (!"CUSTOMER".equals(userRole) && !"MERCHANT".equals(userRole)) {
            throw new BusinessException(ResultStatus.ACCESS_DENIED, "角色权限不足");
        }

        // 3. 手机号唯一性验证
        if (org.springframework.util.StringUtils.hasText(dto.getPhone())) {
            User existingUser = baseMapper.selectByPhone(dto.getPhone());
            if (existingUser != null && !Objects.equals(existingUser.getId(), dto.getId())) {
                throw new BusinessException(ResultStatus.USER_HAS_EXISTED, "手机号已被其他用户使用");
            }
        }

        // 4. 更新用户信息
        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(User::getId, dto.getId());

        if (org.springframework.util.StringUtils.hasText(dto.getNickname())) {
            wrapper.set(User::getNickname, dto.getNickname());
        }
        if (org.springframework.util.StringUtils.hasText(dto.getAvatar())) {
            wrapper.set(User::getAvatar, dto.getAvatar());
        }
        if (org.springframework.util.StringUtils.hasText(dto.getPhone())) {
            wrapper.set(User::getPhone, dto.getPhone());
        }

        wrapper.set(User::getUpdatedAt, new Date());

        boolean updated = update(wrapper);
        if (!updated) {
            throw new BusinessException(ResultStatus.FAIL, "用户信息更新失败");
        }

        // 5. 更新缓存
        User updatedUser = baseMapper.selectById(dto.getId());
        UserContext userContext = new UserContext();
        BeanUtils.copyProperties(updatedUser, userContext);
        userCacheService.updateCache(userContext);

        log.info("用户 {} 更新个人信息成功", currentUser.getUsername());
    }

    @Override
    public void editUser(UserDto dto) {
        User user = findByUsername(dto.getUsername());
        user.setAvatar(dto.getAvatar());
        user.setNickname(dto.getNickname());
        updateById(user);
    }

    @Override
    public UserDto getUserProfile(String username) {
        User user = Optional.of(findByUsername(username))
                .orElseThrow(() -> new BusinessException(ResultStatus.USER_NOT_FOUND, username));

        UserDto dto = new UserDto();
        BeanUtils.copyProperties(user, new UserDto());
        return dto;
    }

    @Override
    public void activateUser(String username) {
        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(User::getUsername,username)
                .eq(User::getStatus, UserStatus.INACTIVE.getCode())
                .set(User::getStatus, UserStatus.ACTIVE.getCode());

        update(wrapper);
    }
    @Override
    public UserDto getUserById(Integer id) {
        User user = this.baseMapper.selectById(id);
        if (user == null) return null;

        UserDto dto = new UserDto();
        BeanUtils.copyProperties(user, dto);
        return dto;
    }
    
    @Override
    public IPage<User> getUserList(int pageNum, int pageSize, String role) {
        Page<User> page = new Page<>(pageNum, pageSize);
        
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        
        // 如果指定了角色，添加角色过滤条件
        if (StringUtils.isNotBlank(role)) {
            queryWrapper.eq(User::getRole, role);
        }
        
        // 排除密码字段，按创建时间倒序排列
        queryWrapper.select(User.class, info -> !info.getColumn().equals("password"))
                   .orderByDesc(User::getCreatedAt);
        
        return this.page(page, queryWrapper);
    }
}
