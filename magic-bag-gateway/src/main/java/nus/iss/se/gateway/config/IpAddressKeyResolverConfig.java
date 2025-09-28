package nus.iss.se.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * IP地址解析器配置（用于限流）
 */
@Configuration
public class IpAddressKeyResolverConfig {

    /**
     * 定义基于IP地址的限流键解析器
     * 这个Bean的名称"ipAddressKeyResolver"需要与配置文件中引用的名称一致
     */
    @Bean
    public KeyResolver ipAddressKeyResolver() {
        // 从请求中获取客户端IP地址作为限流键
        return exchange -> Mono.just(
                Objects.requireNonNull(exchange.getRequest().getRemoteAddress())
                        .getAddress()
                        .getHostAddress()
        );
    }
}

