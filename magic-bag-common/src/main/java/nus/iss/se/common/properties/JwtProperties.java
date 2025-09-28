package nus.iss.se.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@Data
@Component
@RefreshScope
@ConfigurationProperties(prefix = "common.jwt")
public class JwtProperties {
    // jwt 密钥
    private String secret;

    // jwt过期时间
    private Integer expireMinutes;

    // jwt自动续期时间
    private Integer renewThresholdMinutes;
}
