package nus.iss.se.common.config;

import lombok.RequiredArgsConstructor;
import nus.iss.se.common.properties.RsaProperties;
import nus.iss.se.common.util.RsaUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class BaseConfig {
    @Bean
    public RsaUtil rsaUtil(RsaProperties properties) throws Exception {
        // 检查pem文件是否生成
        RsaUtil.generateIfNotExists(properties.getPrivateKeyPath(), properties.getPublicKeyPath());
        return new RsaUtil(properties.getPrivateKeyPath(), properties.getPublicKeyPath());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt算法自动处理盐值，安全性高
        return new BCryptPasswordEncoder();
    }
}
