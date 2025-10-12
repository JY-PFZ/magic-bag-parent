package nus.iss.se.gateway.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nus.iss.se.common.Result;
import nus.iss.se.common.constant.ResultStatus;
import nus.iss.se.common.cache.TokenCacheService;
import nus.iss.se.common.util.JwtUtil;
import nus.iss.se.gateway.config.WhiteListConfig;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;
    private final WhiteListConfig whiteListConfig;
    private final ObjectMapper objectMapper;
    private final TokenCacheService tokenCacheService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 1. 白名单直接放行（响应式判断）
        String path = exchange.getRequest().getURI().getPath();
        if (whiteListConfig.isWhitelisted(path)) {
            return chain.filter(exchange);
        }

        // 2.提取token
        String authorization = exchange.getRequest().getHeaders().getFirst("Authorization");
        log.info("Request authorization: {}",authorization);
        if (authorization == null || !authorization.startsWith("Bearer ")){
            log.info("Invalid Token: {}",authorization);
            return unauthorized(exchange,"Invalid Token");
        }
        String token = authorization.substring(7);


        // 3.验证 Token 有效性
        if (!isValidToken(token)) {
            log.info("Token is invalid or has expired: {}",token);
            return unauthorized(exchange, "Token is invalid or has expired");
        }

        // 4.添加用户信息到请求头
        String username = jwtUtil.getClaims(token).getSubject();
        exchange = exchange.mutate()
                .request(r -> r.header("X-Username", username))
                .build();

        return chain.filter(exchange);
    }

    /**
     * 验证jwt token是否过期和用户是否登出（如果用户登出了，remove redis中的token）
     * */
    private boolean isValidToken(String token) {
        return jwtUtil.isTokenValid(token) && tokenCacheService.isTokenValid(token);
    }

    /**
     * 响应式返回未授权响应（避免 deprecated 的 writeWith 用法）
     */
    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Result<Void> errorResult = Result.error(ResultStatus.FAIL,message);
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(errorResult);
            // 响应式写入
            return exchange.getResponse()
                    .writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)))
                    .then();
        } catch (JsonProcessingException e) {
            return exchange.getResponse().setComplete();
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;  // 优先执行
    }
}


