package nus.iss.se.product.service;

import nus.iss.se.product.dto.FileUploadResponse;
import nus.iss.se.product.dto.PresignedUrlResponse;
import nus.iss.se.product.enums.FileType;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传服务接口
 */
public interface FileUploadService {
    
    /**
     * 上传文件到AWS S3
     * @param file 文件
     * @param type 文件类型
     * @return 文件上传响应
     */
    FileUploadResponse uploadFile(MultipartFile file, FileType type);
    
    /**
     * 获取预签名URL
     * @param fileName 文件名
     * @param contentType 内容类型
     * @param type 文件类型
     * @return 预签名URL响应
     */
    PresignedUrlResponse getPresignedUrl(String fileName, String contentType, FileType type);
    
    /**
     * 删除文件
     * @param fileId 文件ID
     * @return 是否删除成功
     */
    boolean deleteFile(String fileId);
}
