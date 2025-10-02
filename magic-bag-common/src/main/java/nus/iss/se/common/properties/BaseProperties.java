package nus.iss.se.common.properties;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@Getter
@Component
@RefreshScope
public class BaseProperties {
    @Value("${common.url}")
    private String URL;
}
