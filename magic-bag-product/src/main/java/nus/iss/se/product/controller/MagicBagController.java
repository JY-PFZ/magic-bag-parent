package nus.iss.se.product.controller;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nus.iss.se.common.Result;
import nus.iss.se.product.common.UserContextHolder;
import nus.iss.se.product.dto.*;
import nus.iss.se.product.entity.MagicBag;
import nus.iss.se.product.enums.StorageDir;
import nus.iss.se.product.service.FileService;
import nus.iss.se.product.service.IMagicBagService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * MagicBag 控制器
 * 提供盲盒相关的REST API接口
 */
@Slf4j
@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
@Tag(name = "Magic Bag Product API", description = "盲盒产品管理接口")
public class MagicBagController {
    
    private final IMagicBagService magicBagService;
    private final UserContextHolder userContextHolder;
    private final FileService fileService;
    
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
     * 创建新的盲盒商品
     */
    @PostMapping
    @Operation(summary = "创建盲盒商品", description = "创建新的盲盒商品")
    public Result<MagicBagDto> createMagicBag(@RequestBody @Valid MagicBagCreateDto createDto) {
        MagicBagDto magicBag = magicBagService.createMagicBag(createDto);
        return Result.success(magicBag);
    }

    /**
     * 更新盲盒商品信息
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新盲盒商品", description = "更新指定ID的盲盒商品信息")
    public Result<MagicBagDto> updateMagicBag(@PathVariable Integer id,
                                              @RequestBody @Valid MagicBagUpdateDto updateDto) {

        MagicBagDto magicBag = magicBagService.updateMagicBag(id, updateDto);
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
     * 上传产品图片
     */
    @PostMapping("{id}/image")
    @Operation(summary = "上传产品图片", description = "商户上传盲盒产品图片")
    public Result<String> uploadProductImage(@PathVariable Integer id, @RequestParam("file") MultipartFile file) {

        String fileName = id+"_"+System.currentTimeMillis();
        String key = fileService.uploadFile(StorageDir.PRODUCT_IMAGES_DIR.getCode(), fileName, file);
        LambdaUpdateWrapper<MagicBag> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(MagicBag::getId,id).set(MagicBag::getImageUrl,key);
        magicBagService.update(wrapper);

        return Result.success();
    }
    
}
