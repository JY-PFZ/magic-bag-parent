package nus.iss.se.user.service.impl;

import nus.iss.se.common.exception.BusinessException;
import nus.iss.se.user.dto.RegisterReq;
import nus.iss.se.user.entity.User;
import nus.iss.se.user.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserMapper userMapper;

    private RegisterReq validReq;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userService, "baseMapper", userMapper);
        validReq = new RegisterReq();
        validReq.setUsername("testuser");
        validReq.setPassword("123456");
        validReq.setRole("CUSTOMER"); // 使用已知合法角色
    }

    @Test
    void shouldThrowExceptionWhenRoleNotFound() {
        RegisterReq invalidReq = new RegisterReq();
        invalidReq.setUsername("test");
        invalidReq.setPassword("123");
        invalidReq.setRole("INVALID");
        assertThrows(BusinessException.class, () -> userService.register(invalidReq));
    }

    @Test
    void shouldThrowExceptionWhenUserAlreadyExists() {
        User existing = new User();
        existing.setUsername("testuser");
        when(userMapper.selectByUsername("testuser")).thenReturn(existing);
        assertThrows(BusinessException.class, () -> userService.register(validReq));
    }
}