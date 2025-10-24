package nus.iss.se.product.config;

import lombok.RequiredArgsConstructor;
import nus.iss.se.product.config.S3Properties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

/**
 * S3配置类 - 与现有AwsS3Config兼容
 */
@Configuration
@RequiredArgsConstructor
public class S3Config {
    
    @Bean("s3StorageClient")
    public S3Client s3StorageClient(S3Properties s3Properties) {
        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(s3Properties.getRegion()));

        // 如果提供了 accessKey 和 secretKey，则使用静态凭证
        String accessKey = s3Properties.getAwsAccessKeyId();
        String secretKey = s3Properties.getAwsSecretAccessKey();
        if (StringUtils.isNoneBlank(accessKey, secretKey)) {
            AwsBasicCredentials awsCreds = AwsBasicCredentials.create(accessKey, secretKey);
            builder.credentialsProvider(StaticCredentialsProvider.create(awsCreds));
        }

        return builder.build();
    }
}
