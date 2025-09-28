package nus.iss.se.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import nus.iss.se.common.properties.JwtProperties;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class JwtUtil {
    private final JwtProperties jwtProperties;

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 JWT Token
     * @param username 用户名
     * @param role 角色（user/merchant/admin
     * @return token
     */
    public String generateAuthToken(String username, String role) {
        return Jwts.builder()
                .setSubject(username)
                .claim("roles",role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtProperties.getExpireMinutes() * 60 * 1000))
                .signWith(getKey())
                .compact();
    }

    /**
     * 解析 Token 获取 Claims
     */
    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 校验 Token 是否有效
     */
    public boolean isTokenValid(String token) {
        try {
            final String subject = getClaims(token).getSubject();
            return !subject.isEmpty() && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    public String extractToken(String authorization){
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return null;
    }

    /**
     * 判断是否过期
     */
    private boolean isTokenExpired(String token) {
        return getExpirationDate(token).before(new Date());
    }

    // 获取过期时间
    public Date getExpirationDate(String token) {
        return getClaims(token).getExpiration();
    }

    public int getDefaultExpirationMinutes(){
        return jwtProperties.getRenewThresholdMinutes();
    }

    /**
     * 判断 token 是否在指定分钟内即将过期
     * @param token JWT token
     * @param minutes 剩余多少分钟内算“即将过期”
     * @return true 表示需要续期
     */
    public boolean isTokenExpiringWithin(String token, int minutes) {
        Date expiration = getExpirationDate(token);
        long threshold = expiration.getTime() - ((long) minutes * 60 * 1000);
        return System.currentTimeMillis() > threshold;
    }

    public boolean isNeedRenew(String token){
        return isTokenExpiringWithin(token, jwtProperties.getRenewThresholdMinutes());
    }

}
