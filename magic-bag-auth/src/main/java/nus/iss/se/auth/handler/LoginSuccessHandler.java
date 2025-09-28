//package nus.iss.se.auth.handler;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.RequiredArgsConstructor;
//import nus.iss.se.auth.common.MyUserDetails;
//import nus.iss.se.common.Result;
//import nus.iss.se.common.cache.TokenCacheService;
//import nus.iss.se.common.cache.UserCacheService;
//import nus.iss.se.common.cache.UserContext;
//import nus.iss.se.common.util.JwtUtil;
//import org.springframework.beans.BeanUtils;
//import org.springframework.http.HttpStatus;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
//import org.springframework.stereotype.Component;
//
//import java.io.IOException;
//import java.util.Date;
//
//@Component
//@RequiredArgsConstructor
//public class LoginSuccessHandler implements AuthenticationSuccessHandler {
//    private final JwtUtil jwtUtil;
//    private final ObjectMapper objectMapper;
//    private final TokenCacheService tokenCacheService;
//    private final UserCacheService userCacheService;
//
//    @Override
//    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
//        // 1. 获取user信息
//        MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();
//
//        // 2. 生成 JWT 令牌
//        String token = jwtUtil.generateToken(userDetails.getUsername(), userDetails.user().getRole());
//
//        // 3.把token放到redis中
//        tokenCacheService.saveToken(userDetails.getUsername(), token, jwtUtil.getDefaultExpirationMinutes() + 5);
//
//        // 4. 发布事件，把用户信息存到redis中
//        UserContext userContext = new UserContext();
//        BeanUtils.copyProperties(userDetails.user(), userContext);
//        userContext.setLoginTime(new Date());
//        userCacheService.cacheUser(userContext);
//
//        // 5. 返回 Token 给前端
//        response.setCharacterEncoding("UTF-8");
//        response.setContentType("application/json;charset=UTF-8");
//        response.setHeader("X-New-Token", token);
//        response.setStatus(HttpStatus.OK.value());
//
//        String json = objectMapper.writeValueAsString(Result.success());
//        response.getWriter().write(json);
//    }
//}
