package nus.iss.se.product.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import nus.iss.se.common.Result;
import nus.iss.se.product.dto.FileUploadResponse;
import nus.iss.se.product.dto.PresignedUrlResponse;
import nus.iss.se.product.enums.FileType;
import nus.iss.se.product.service.FileUploadService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传控制器
 */
@RestController
@RequestMapping("/product/files")
@RequiredArgsConstructor
@Tag(name = "File Upload API", description = "文件上传管理接口")
public class FileUploadController {
    
    private final FileUploadService fileUploadService;
    
    @PostMapping("/upload")
    @Operation(summary = "上传文件", description = "上传图片文件到AWS S3")
    public Result<FileUploadResponse> uploadFile(
        @RequestParam("file") MultipartFile file,
        @RequestParam("type") FileType type
    ) {
        FileUploadResponse response = fileUploadService.uploadFile(file, type);
        return Result.success(response);
    }
    
    @GetMapping("/presigned-url")
    @Operation(summary = "获取预签名URL", description = "获取文件上传的预签名URL")
    public Result<PresignedUrlResponse> getPresignedUrl(
        @RequestParam("fileName") String fileName,
        @RequestParam("contentType") String contentType,
        @RequestParam("type") FileType type
    ) {
        PresignedUrlResponse response = fileUploadService.getPresignedUrl(fileName, contentType, type);
        return Result.success(response);
    }
    
    @DeleteMapping("/{fileId}")
    @Operation(summary = "删除文件", description = "从AWS S3删除文件")
    public Result<Void> deleteFile(@PathVariable String fileId) {
        boolean success = fileUploadService.deleteFile(fileId);
        if (success) {
            return Result.success();
        } else {
            return Result.error("文件删除失败");
        }
    }
}
