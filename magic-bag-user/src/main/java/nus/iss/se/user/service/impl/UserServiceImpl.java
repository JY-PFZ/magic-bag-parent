package nus.iss.se.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import nus.iss.se.common.constant.ResultStatus;
import nus.iss.se.common.exception.BusinessException;
import nus.iss.se.kafka.event.EventEnvelope;
import nus.iss.se.kafka.publisher.KafkaEventPublisher;
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

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    private final PasswordEncoder passwordEncoder;
    private final KafkaEventPublisher eventPublisher;

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
    public User updateUserInfo(User dto) {
        if (StringUtils.isBlank(dto.getUsername())) {
            throw new BusinessException(ResultStatus.USER_NOT_FOUND, "username is null or blank");
        }
        User user = findByUsername(dto.getUsername());
        user.setAvatar(dto.getAvatar());
        user.setNickname(dto.getNickname());
        updateById(user);

        return user;
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
}
