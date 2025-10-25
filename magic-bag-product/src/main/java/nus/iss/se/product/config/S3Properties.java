package nus.iss.se.product.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * S3配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "common.s3")
public class S3Properties {
    private String region;
    private String bucketName;
    private String awsAccessKeyId;
    private String awsSecretAccessKey;
}




