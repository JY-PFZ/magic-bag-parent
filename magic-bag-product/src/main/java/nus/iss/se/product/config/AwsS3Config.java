package nus.iss.se.product.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/**
 * AWS S3 配置类
 */
@Configuration
@ConfigurationProperties(prefix = "aws.s3")
@Data
public class AwsS3Config {
    
    private String bucket = "magic-bag-files-838811708767"; 
    private String region = "ap-southeast-1";                
    private String domain = "https://magic-bag-files-838811708767.s3.ap-southeast-1.amazonaws.com";
    
    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
            .region(Region.AP_SOUTHEAST_1) 
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build();
    }
    
    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
            .region(Region.AP_SOUTHEAST_1)
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build();
    }
}
