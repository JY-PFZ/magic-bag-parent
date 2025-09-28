package nus.iss.se.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nus.iss.se.auth.api.UserClient;
import nus.iss.se.auth.common.MyUserDetails;
import nus.iss.se.auth.common.UserStatus;
import nus.iss.se.auth.entity.User;
import nus.iss.se.common.Result;
import nus.iss.se.common.constant.ResultStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class MyUserDetailsService implements UserDetailsService {
    private final UserClient userClient;

    @Override
    public UserDetails loadUserByUsername(String username) {
        Result<User> result = userClient.getUserByUsername(username);
        if (result.getCode() != ResultStatus.SUCCESS.getCode()){
            log.info("Auth service get user fail:{}->{}",username,result.getMessage());
            throw new BadCredentialsException(ResultStatus.FAIL + ": " + result.getMessage());
        }
        User user = result.getData();
        if (user == null){
            throw new UsernameNotFoundException(ResultStatus.USER_NOT_FOUND + ": " + username);
        } else if (user.getStatus() == UserStatus.INACTIVE.getCode()) {
            // 用户未激活
            throw new BadCredentialsException(ResultStatus.USER_ACCOUNT_NOT_ACTIVATE.getMessage() + ": "+ username);
        }

        return new MyUserDetails(result.getData());
    }
}
