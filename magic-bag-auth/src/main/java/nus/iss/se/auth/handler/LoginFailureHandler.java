//package nus.iss.se.auth.handler;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import nus.iss.se.common.Result;
//import nus.iss.se.common.type.ResultStatus;
//import org.springframework.http.HttpStatus;
//import org.springframework.security.core.AuthenticationException;
//import org.springframework.security.web.authentication.AuthenticationFailureHandler;
//import org.springframework.stereotype.Component;
//
//import java.io.IOException;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class LoginFailureHandler implements AuthenticationFailureHandler {
//    private final ObjectMapper objectMapper;
//
//    @Override
//    public void onAuthenticationFailure(HttpServletRequest request,
//                                        HttpServletResponse response,
//                                        AuthenticationException exception) throws IOException {
//        // 提取用户名（从请求参数或异常中）
//        String username = request.getParameter("username");
//        log.info("user login failed: {}",username);
//
//        // 返回错误信息
//        response.setCharacterEncoding("UTF-8");
//        response.setContentType("application/json;charset=UTF-8");
//        response.setStatus(HttpStatus.UNAUTHORIZED.value());
//
//        log.info("user login failed: {}-{}",username,exception.getMessage());
//        String json = objectMapper.writeValueAsString(Result.error(ResultStatus.FAIL,"Invalid username or password"));
//        response.getWriter().write(json);
//    }
//}
