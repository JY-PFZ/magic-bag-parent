package nus.iss.se.product.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nus.iss.se.common.Result;
import nus.iss.se.product.service.S3StorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 简化的文件上传控制器 - 直接使用S3StorageService
 */
@Slf4j
@RestController
@RequestMapping("/product/files")
@RequiredArgsConstructor
@Tag(name = "File Upload API", description = "文件上传管理接口")
public class SimpleFileUploadController {
    
    private final S3StorageService s3StorageService;
    
    @PostMapping("/upload")
    @Operation(summary = "上传文件", description = "上传图片文件到AWS S3")
    public Result<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            // 验证文件
            if (file.isEmpty()) {
                return Result.error("文件不能为空");
            }
            
            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return Result.error("只支持图片文件");
            }
            
            // 验证文件大小 (5MB)
            if (file.getSize() > 5 * 1024 * 1024) {
                return Result.error("文件大小不能超过5MB");
            }
            
            // 生成文件路径
            String filePath = generateFilePath(file.getOriginalFilename());
            
            // 上传到S3
            s3StorageService.upload(filePath, file.getBytes(), contentType);
            
            // 获取文件URL
            String fileUrl = s3StorageService.getPublicUrl(filePath);
            
            log.info("文件上传成功: {}", filePath);
            return Result.success(fileUrl);
            
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return Result.error("文件上传失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/download/{key}")
    @Operation(summary = "下载文件", description = "从AWS S3下载文件")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String key) {
        try {
            if (!s3StorageService.exists(key)) {
                return ResponseEntity.notFound().build();
            }
            
            byte[] data = s3StorageService.downloadAsBytes(key);
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + key + "\"")
                    .body(data);
                    
        } catch (IOException e) {
            log.error("文件下载失败: {}", key, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @DeleteMapping("/delete/{key}")
    @Operation(summary = "删除文件", description = "从AWS S3删除文件")
    public Result<String> deleteFile(@PathVariable String key) {
        try {
            if (!s3StorageService.exists(key)) {
                return Result.error("文件不存在");
            }
            
            s3StorageService.delete(key);
            log.info("文件删除成功: {}", key);
            return Result.success("文件删除成功");
            
        } catch (Exception e) {
            log.error("文件删除失败: {}", key, e);
            return Result.error("文件删除失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/exists/{key}")
    @Operation(summary = "检查文件是否存在", description = "检查AWS S3中文件是否存在")
    public Result<Boolean> checkFileExists(@PathVariable String key) {
        try {
            boolean exists = s3StorageService.exists(key);
            return Result.success(exists);
        } catch (Exception e) {
            log.error("检查文件存在性失败: {}", key, e);
            return Result.error("检查文件存在性失败: " + e.getMessage());
        }
    }
    
    /**
     * 生成文件路径
     */
    private String generateFilePath(String originalFilename) {
        String fileId = UUID.randomUUID().toString();
        String extension = getFileExtension(originalFilename);
        String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        
        return String.format("uploads/%s/%s.%s", datePath, fileId, extension);
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "jpg";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }
}



