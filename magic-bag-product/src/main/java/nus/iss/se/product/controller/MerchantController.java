package nus.iss.se.product.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nus.iss.se.common.Result;
import nus.iss.se.common.cache.UserContext;
import nus.iss.se.product.common.UserContextHolder;
import nus.iss.se.product.dto.MerchantDto;
import nus.iss.se.product.dto.MerchantUpdateDto;
import nus.iss.se.product.service.IMerchantService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Merchant Controller
 * 提供商户相关的REST API接口
 */
@RestController
@RequestMapping("/product/merchants")
@RequiredArgsConstructor
@Tag(name = "Merchant API", description = "商户管理服务")
public class MerchantController {
    
    private final IMerchantService merchantService;
    private final UserContextHolder userContextHolder;
    
    /**
     * 获取所有已审核的商户列表
     */
    @GetMapping
    @Operation(summary = "获取商户列表", description = "获取所有已审核的商户列表")
    public Result<List<MerchantDto>> getAllMerchants() {
        List<MerchantDto> merchants = merchantService.getAllMerchants();
        return Result.success(merchants);
    }
    
    /**
     * 根据ID获取商户详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取商户详情", description = "根据商户ID获取详细信息")
    public Result<MerchantDto> getMerchantById(@PathVariable Integer id) {
        MerchantDto merchant = merchantService.getMerchantById(id);
        if (merchant == null) {
            return Result.error("商户不存在");
        }
        return Result.success(merchant);
    }
    
    /**
     * 更新商户信息
     */
    @PutMapping("/profile")
    @Operation(summary = "更新商户信息", description = "商户更新自己的店铺信息")
    public Result<Void> updateMerchantProfile(@RequestBody @Valid MerchantUpdateDto merchantDto) {
        UserContext currentUser = userContextHolder.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }
        merchantService.updateMerchantProfile(merchantDto, currentUser.getId());
        return Result.success();
    }
    
    /**
     * 根据评分排序商户
     */
    @GetMapping("/sorted-by-score")
    @Operation(summary = "根据评分排序商户", description = "根据评分排序商户列表")
    public Result<IPage<MerchantDto>> sortedByScore(
            @RequestParam(defaultValue = "1", name = "current") Integer current,
            @RequestParam(defaultValue = "10", name = "size") Integer size,
            @RequestParam(defaultValue = "0", name = "minScore") Integer minScore) {
        IPage<MerchantDto> listByScore = merchantService.sortedMerchantsByScore(current, size, minScore);
        return Result.success(listByScore);
    }
}
