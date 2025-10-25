package nus.iss.se.product.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nus.iss.se.common.cache.UserContext;
import nus.iss.se.common.constant.ResultStatus;
import nus.iss.se.common.exception.BusinessException;
import nus.iss.se.product.common.UserContextHolder;
import nus.iss.se.product.config.AwsS3Config;
import nus.iss.se.product.dto.FileUploadResponse;
import nus.iss.se.product.dto.PresignedUrlResponse;
import nus.iss.se.product.entity.FileInfo;
import nus.iss.se.product.enums.FileType;
import nus.iss.se.product.mapper.FileInfoMapper;
import nus.iss.se.product.service.FileUploadService;
import nus.iss.se.product.service.S3StorageService;
import nus.iss.se.product.validator.FileValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * AWS S3 文件上传服务实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AwsS3FileUploadService implements FileUploadService {
    
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final AwsS3Config s3Properties;
    private final S3StorageService s3StorageService;
    private final FileInfoMapper fileInfoMapper;
    private final UserContextHolder userContextHolder;
    private final FileValidator fileValidator;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public FileUploadResponse uploadFile(MultipartFile file, FileType type) {
        UserContext currentUser = userContextHolder.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException(ResultStatus.USER_NOT_LOGGED_IN);
        }
        
        // 1. 验证文件
        fileValidator.validateFile(file, type, currentUser);
        
        // 2. 生成文件路径
        String filePath = generateFilePath(type, file.getOriginalFilename());
        
        // 3. 上传到S3
        uploadToS3(file, filePath);
        
        // 4. 保存文件信息到数据库
        String fileId = saveFileInfo(file, filePath, type, currentUser.getId());
        
        // 5. 返回文件URL
        return buildFileUploadResponse(fileId, filePath, file);
    }
    
    @Override
    public PresignedUrlResponse getPresignedUrl(String fileName, String contentType, FileType type) {
        UserContext currentUser = userContextHolder.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException(ResultStatus.USER_NOT_LOGGED_IN);
        }
        
        String filePath = generateFilePath(type, fileName);
        
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
            .bucket(s3Properties.getBucket())
            .key(filePath)
            .contentType(contentType)
            .build();
        
        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
            .signatureDuration(java.time.Duration.ofMinutes(10))
            .putObjectRequest(putObjectRequest)
            .build();
        
        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
        
        return PresignedUrlResponse.builder()
            .presignedUrl(presignedRequest.url().toString())
            .filePath(filePath)
            .expiresAt(presignedRequest.expiration())
            .build();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteFile(String fileId) {
        try {
            FileInfo fileInfo = fileInfoMapper.findByFileId(fileId);
            if (fileInfo == null) {
                return false;
            }
            
            // 使用S3StorageService删除文件
            s3StorageService.delete(fileInfo.getS3Key());
            
            // 从数据库删除记录
            fileInfoMapper.deleteById(fileInfo.getId());
            
            log.info("文件删除成功: {}", fileId != null ? fileId.replaceAll("[\\r\\n]", "") : "null");
            return true;
        } catch (Exception e) {
            log.error("文件删除失败: {}", fileId != null ? fileId.replaceAll("[\\r\\n]", "") : "null", e);
            return false;
        }
    }
    
    private void uploadToS3(MultipartFile file, String filePath) {
        try {
            // 使用S3StorageService进行上传
            s3StorageService.upload(filePath, file.getBytes(), file.getContentType());
            log.info("文件上传成功: {}", filePath != null ? filePath.replaceAll("[\\r\\n]", "") : "null");
        } catch (Exception e) {
            log.error("文件上传失败: {}", filePath != null ? filePath.replaceAll("[\\r\\n]", "") : "null", e);
            throw new BusinessException(ResultStatus.FAIL, "文件上传失败");
        }
    }
    
    private String generateFilePath(FileType type, String originalFilename) {
        String fileId = UUID.randomUUID().toString();
        String extension = getFileExtension(originalFilename);
        String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        
        return String.format("%s/%s/%s.%s", 
            type.getPathPrefix(), datePath, fileId, extension);
    }
    
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "jpg";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }
    
    private String saveFileInfo(MultipartFile file, String filePath, FileType type, Integer userId) {
        String fileId = UUID.randomUUID().toString();
        
        FileInfo fileInfo = new FileInfo();
        fileInfo.setFileId(fileId);
        fileInfo.setOriginalName(file.getOriginalFilename());
        fileInfo.setFilePath(filePath);
        fileInfo.setFileSize(file.getSize());
        fileInfo.setFileType(type.name());
        fileInfo.setMimeType(file.getContentType());
        fileInfo.setUploadUserId(userId);
        fileInfo.setFileCategory(type.name());
        fileInfo.setS3Bucket(s3Properties.getBucket());
        fileInfo.setS3Key(filePath);
        fileInfo.setFileUrl(s3Properties.getDomain() + "/" + filePath);
        fileInfo.setCreatedAt(LocalDateTime.now());
        
        fileInfoMapper.insert(fileInfo);
        
        return fileId;
    }
    
    private FileUploadResponse buildFileUploadResponse(String fileId, String filePath, MultipartFile file) {
        FileUploadResponse response = new FileUploadResponse();
        response.setFileId(fileId);
        response.setFileName(file.getOriginalFilename());
        response.setFileUrl(s3Properties.getDomain() + "/" + filePath);
        response.setFileSize(file.getSize());
        response.setFileType(file.getContentType());
        response.setUploadTime(LocalDateTime.now());
        
        return response;
    }
}
