package nus.iss.se.auth.util;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 基础工具类
 */
public class BaseUtil {
    private BaseUtil() {}

    /**
     * 从请求头中提取 Bearer Token
     */
    public static String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}

