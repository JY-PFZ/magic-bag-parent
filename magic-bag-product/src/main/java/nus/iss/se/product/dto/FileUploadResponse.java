package nus.iss.se.product.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 文件上传响应DTO
 */
@Data
public class FileUploadResponse {
    private String fileId;
    private String fileName;
    private String fileUrl;
    private Long fileSize;
    private String fileType;
    private LocalDateTime uploadTime;
}
