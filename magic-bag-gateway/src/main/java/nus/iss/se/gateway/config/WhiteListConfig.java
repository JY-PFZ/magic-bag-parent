package nus.iss.se.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

/**
 * 网关白名单配置（支持Nacos动态刷新）
 */
@Data
@Component
@RefreshScope  // 结合Nacos实现配置动态刷新
@ConfigurationProperties(prefix = "security.whitelist")  // 绑定配置前缀
public class WhiteListConfig {

    /**
     * 无需认证的路径列表
     */
    private List<String> paths = new ArrayList<>();

    /**
     * 判断路径是否在白名单中（支持通配符匹配）
     * @param path 请求路径
     * @return 是否在白名单中
     */
    public boolean isWhitelisted(String path) {
        if (paths.isEmpty()) {
            return false;
        }
        // 支持简单的通配符匹配，如 /api/public/** 匹配所有子路径
        return paths.stream().anyMatch(pattern -> pathMatch(pattern, path));
    }

    /**
     * 路径匹配逻辑（支持**通配符）
     */
    private boolean pathMatch(String pattern, String path) {
        // 精确匹配
        if (pattern.equals(path)) {
            return true;
        }
        // 处理 **通配符（匹配所有子路径）
        if (pattern.endsWith("/**")) {
            String prefix = pattern.substring(0, pattern.length() - 3);
            return path.startsWith(prefix);
        }
        // 处理 * 通配符（匹配一级路径）
        if (pattern.contains("*")) {
            String regex = pattern.replace("*", "[^/]*");
            return path.matches(regex);
        }
        return false;
    }
}

