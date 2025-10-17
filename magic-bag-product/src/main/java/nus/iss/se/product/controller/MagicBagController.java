package nus.iss.se.product.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import nus.iss.se.common.Result;
import nus.iss.se.product.dto.CreateMagicBagRequest;
import nus.iss.se.product.dto.MagicBagDto;
import nus.iss.se.product.dto.MagicBagListResponse;
import nus.iss.se.product.dto.UpdateMagicBagRequest;
import nus.iss.se.product.service.IMagicBagService;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * MagicBag 控制器
 * 提供盲盒相关的REST API接口
 */
@RestController
@RequestMapping("/product/magic-bags")
@RequiredArgsConstructor
@Tag(name = "Magic Bag Product API", description = "盲盒产品管理接口")
public class MagicBagController {
    
    private final IMagicBagService magicBagService;
    
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
     * 创建新产品
     */
    @PostMapping
    @Operation(summary = "创建产品", description = "创建新的盲盒产品")
    public Result<MagicBagDto> createMagicBag(@Valid @RequestBody CreateMagicBagRequest request) {
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
    
    @PostMapping("/batch-query")
    @Operation(summary = "批量查询盲盒", description = "根据ID列表批量查询盲盒信息")
    public Result<List<MagicBagDto>> getBatchMagicBags(@RequestBody List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.success(List.of());
        }
        List<MagicBagDto> magicBags = magicBagService.getBatchByIds(ids);
        return Result.success(magicBags);
    }
    
}
