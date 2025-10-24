package nus.iss.se.admin.common;

import lombok.RequiredArgsConstructor;
import nus.iss.se.admin.common.UserContextInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final UserContextInterceptor userContextInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userContextInterceptor)
                .addPathPatterns("/**")          // 拦截所有路径
                .excludePathPatterns("/user/register","/actuator/**", "/health", "/swagger-ui/**"); // 排除健康检查等
    }
}
