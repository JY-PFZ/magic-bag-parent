package nus.iss.se.merchant.common;

import jakarta.annotation.PreDestroy;
import lombok.Data;
import nus.iss.se.common.cache.UserContext;
import nus.iss.se.common.constant.ResultStatus;
import nus.iss.se.common.exception.BusinessException;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

/**
 * 当前用户上下文，每个请求独立实例
 * */
@Data
@Component
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class UserContextHolder {
    private UserContext currentUser;
    private String token;

    public Integer userId(){
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ResultStatus.USER_NOT_FOUND);
        }
        return currentUser.getId();
    }

    /**
     * 清除上下文
     * */
    @PreDestroy
    public void clear(){
        currentUser = null;
        token = null;
    }
}
