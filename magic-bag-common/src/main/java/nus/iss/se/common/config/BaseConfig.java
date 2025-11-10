package nus.iss.se.common.config;

import lombok.RequiredArgsConstructor;
import nus.iss.se.common.properties.RsaProperties;
import nus.iss.se.common.util.RsaUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Configuration
@RequiredArgsConstructor
public class BaseConfig {
/*    @Bean
    public RsaUtil rsaUtil(RsaProperties properties) throws Exception {
        // 检查pem文件是否生成
        RsaUtil.generateIfNotExists(properties.getPrivateKeyPath(), properties.getPublicKeyPath());
        return new RsaUtil(properties.getPrivateKeyPath(), properties.getPublicKeyPath());
    }*/

    @Bean
    public RsaUtil rsaUtil(RsaProperties properties) {
        try {
            byte[] decodedPrivate = Base64.getDecoder().decode(properties.getPrivateKey());
            PKCS8EncodedKeySpec specPrivate = new PKCS8EncodedKeySpec(decodedPrivate);
            PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(specPrivate);

            byte[] decodedPublic = Base64.getDecoder().decode(properties.getPublicKey());
            X509EncodedKeySpec specPublic = new X509EncodedKeySpec(decodedPublic);
            PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(specPublic);

            return new RsaUtil(privateKey,publicKey);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt算法自动处理盐值，安全性高
        return new BCryptPasswordEncoder();
    }
}
