package nus.iss.se.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import nus.iss.se.common.Result;
import nus.iss.se.common.cache.TokenCacheService;
import nus.iss.se.common.cache.UserCacheService;
import nus.iss.se.common.util.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class MyLogoutSuccessHandler implements LogoutSuccessHandler {
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;
    private final TokenCacheService tokenCacheService;
    private final UserCacheService userCacheService;

    /**
     * 自定义退出成功处理
     * */
    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        // 1. 获取token
        String token = jwtUtil.extractToken(request.getHeader("Authorization"));

        //  2. Redis 标记 token 失效
        tokenCacheService.revokeToken(token);

        // 3. 清除用户信息redis缓存
        String username = jwtUtil.getClaims(token).getSubject();
        userCacheService.deleteUserCache(username);

        // 构建响应体
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpStatus.OK.value());
        response.getWriter().write(objectMapper.writeValueAsString(Result.success("Logout success")));
    }
}
