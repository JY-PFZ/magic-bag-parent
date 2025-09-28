package nus.iss.se.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@Data
@Component
@RefreshScope
@ConfigurationProperties(prefix = "common.rsa")
public class RsaProperties {
    private boolean boot;

    private String publicKeyPath;

    private String privateKeyPath;
}
