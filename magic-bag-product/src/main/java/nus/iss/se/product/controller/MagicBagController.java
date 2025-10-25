package nus.iss.se.product.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nus.iss.se.common.Result;
import nus.iss.se.common.cache.UserContext;
import nus.iss.se.product.common.UserContextHolder;
import nus.iss.se.product.dto.CreateMagicBagRequest;
import nus.iss.se.product.dto.FileUploadResponse;
import nus.iss.se.product.dto.MagicBagDto;
import nus.iss.se.product.dto.MagicBagListResponse;
import nus.iss.se.product.dto.UpdateMagicBagRequest;
import nus.iss.se.product.enums.FileType;
import nus.iss.se.product.service.FileUploadService;
import nus.iss.se.product.service.IMagicBagService;
import nus.iss.se.product.service.S3StorageService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * MagicBag 控制器
 * 提供盲盒相关的REST API接口
 */
@Slf4j
@RestController
@RequestMapping("/product/magic-bags")
@RequiredArgsConstructor
@Tag(name = "Magic Bag Product API", description = "盲盒产品管理接口")
public class MagicBagController {
    
    private final IMagicBagService magicBagService;
    private final UserContextHolder userContextHolder;
//    private final FileUploadService fileUploadService;
    private final S3StorageService s3StorageService;
    
    /**
     * 获取所有盲盒列表（分页）
     */
    @GetMapping
    @Operation(summary = "获取盲盒列表", description = "分页获取所有活跃的盲盒产品")
    public Result<MagicBagListResponse> getAllMagicBags(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        MagicBagListResponse response = magicBagService.getAllMagicBags(page, size);
        return Result.success(response);
    }
    
    /**
     * 根据ID获取盲盒详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取盲盒详情", description = "根据ID获取盲盒详细信息")
    public Result<MagicBagDto> getMagicBagById(@PathVariable Integer id) {
        MagicBagDto magicBag = magicBagService.getMagicBagById(id);
        if (magicBag == null) {
            return Result.error("盲盒不存在");
        }
        return Result.success(magicBag);
    }
    
    /**
     * 根据分类获取盲盒
     */
    @GetMapping("/category/{category}")
    @Operation(summary = "按分类获取盲盒", description = "根据分类获取盲盒列表")
    public Result<List<MagicBagDto>> getMagicBagsByCategory(@PathVariable String category) {
        List<MagicBagDto> magicBags = magicBagService.getMagicBagsByCategory(category);
        return Result.success(magicBags);
    }
    
    /**
     * 根据商户ID获取盲盒
     */
    @GetMapping("/merchant/{merchantId}")
    @Operation(summary = "按商户获取盲盒", description = "根据商户ID获取盲盒列表")
    public Result<List<MagicBagDto>> getMagicBagsByMerchantId(@PathVariable Integer merchantId) {
        List<MagicBagDto> magicBags = magicBagService.getMagicBagsByMerchantId(merchantId);
        return Result.success(magicBags);
    }
    
    /**
     * 创建新产品
     */
    @PostMapping
    @Operation(summary = "创建产品", description = "创建新的盲盒产品")
    public Result<MagicBagDto> createMagicBag(@Valid @RequestBody CreateMagicBagRequest request) {
        UserContext currentUser = userContextHolder.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }
        
        // 从用户上下文获取商户ID，而不是让前端传递
        request.setMerchantId(currentUser.getId());
        
        MagicBagDto magicBag = magicBagService.createMagicBag(request);
        if (magicBag == null) {
            return Result.error("创建产品失败");
        }
        return Result.success(magicBag);
    }
    
    /**
     * 更新产品信息
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新产品", description = "更新盲盒产品信息")
    public Result<MagicBagDto> updateMagicBag(@PathVariable Integer id, 
                                            @Valid @RequestBody UpdateMagicBagRequest request) {
        MagicBagDto magicBag = magicBagService.updateMagicBag(id, request);
        if (magicBag == null) {
            return Result.error("更新产品失败");
        }
        return Result.success(magicBag);
    }
    
    /**
     * 删除产品（软删除）
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除产品", description = "软删除盲盒产品")
    public Result<Void> deleteMagicBag(@PathVariable Integer id) {
        boolean success = magicBagService.deleteMagicBag(id);
        if (success) {
            return Result.success();
        } else {
            return Result.error("删除产品失败");
        }
    }
    
    /**
     * 批量删除产品（软删除）
     */
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除产品", description = "批量软删除盲盒产品")
    public Result<Integer> batchDeleteMagicBags(@RequestBody List<Integer> ids) {
        int deletedCount = magicBagService.batchDeleteMagicBags(ids);
        return Result.success(deletedCount);
    }
    
    /**
     * 上传产品图片 - 简化版本
     */
    @PostMapping("/upload-image")
    @Operation(summary = "上传产品图片", description = "商户上传盲盒产品图片")
    public Result<String> uploadProductImage(@RequestParam("file") MultipartFile file) {
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
            String filePath = generateImagePath(file.getOriginalFilename());
            
            // 上传到S3
            s3StorageService.upload(filePath, file.getBytes(), contentType);
            
            // 获取文件URL
            String fileUrl = s3StorageService.getPublicUrl(filePath);
            
            return Result.success(fileUrl);
            
        } catch (Exception e) {
            log.error("图片上传失败", e);
            return Result.error("图片上传失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/batch-query")
    @Operation(summary = "批量查询盲盒", description = "根据ID列表批量查询盲盒信息")
    public Result<List<MagicBagDto>> getBatchMagicBags(@RequestBody List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.success(List.of());
        }
        List<MagicBagDto> magicBags = magicBagService.getBatchByIds(ids);
        return Result.success(magicBags);
    }
    
    /**
     * 生成图片文件路径
     */
    private String generateImagePath(String originalFilename) {
        String fileId = UUID.randomUUID().toString();
        String extension = getFileExtension(originalFilename);
        String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        
        return String.format("magic-bag-images/%s/%s.%s", datePath, fileId, extension);
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
