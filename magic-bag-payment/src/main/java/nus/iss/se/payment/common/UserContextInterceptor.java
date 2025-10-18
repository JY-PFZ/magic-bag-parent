package nus.iss.se.payment.common;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nus.iss.se.common.cache.UserCacheService;
import nus.iss.se.common.cache.UserContext;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserContextInterceptor implements HandlerInterceptor {
    private final UserCacheService userCacheService;
    private final UserContextHolder userContextHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String username = request.getHeader("X-Username");
        if (username != null) {
            UserContext userContext = userCacheService.getCachedUser(username);
            userContextHolder.setCurrentUser(userContext);
        }
        return true;
    }
}
