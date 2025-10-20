package nus.iss.se.product.dto;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;

/**
 * 预签名URL响应DTO
 */
@Data
@Builder
public class PresignedUrlResponse {
    private String presignedUrl;
    private String filePath;
    private Instant expiresAt;
}
