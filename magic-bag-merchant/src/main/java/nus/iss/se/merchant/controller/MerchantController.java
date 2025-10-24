package nus.iss.se.merchant.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nus.iss.se.common.Result;
import nus.iss.se.common.cache.UserContext;
import nus.iss.se.merchant.common.UserContextHolder;
import nus.iss.se.merchant.dto.MerchantDto;
import nus.iss.se.merchant.dto.MerchantUpdateDto;
import nus.iss.se.merchant.service.IMerchantService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/merchant/merchants")
@RequiredArgsConstructor
@Tag(name = "Merchant API", description = "商户管理服务")
public class MerchantController {

    private final IMerchantService merchantService;
    private final UserContextHolder userContextHolder;

    @GetMapping
    public Result<List<MerchantDto>> getAllMerchants() {
        List<MerchantDto> merchants = merchantService.getAllMerchants();
        return Result.success(merchants);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取商家详情")
    public Result<MerchantDto> getMerchantById(@PathVariable Integer id) {
        MerchantDto merchant = merchantService.getMerchantById(id);
        if (merchant == null) {
            return Result.error("商户不存在");
        }
        return Result.success(merchant);
    }

    @PutMapping("/profile")
    @Operation(summary = "更新商户信息")
    public Result<Void> updateMerchantProfile(@RequestBody @Valid MerchantUpdateDto merchantDto) {
        UserContext currentUser = userContextHolder.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }
        merchantService.updateMerchantProfile(merchantDto, currentUser.getId());
        return Result.success();
    }

    @GetMapping("/sorted-by-score")
    @Operation(summary = "根据评分排序商户")
    public Result<IPage<MerchantDto>> sortedByScore(
            @RequestParam(defaultValue = "1", name = "current") Integer current,
            @RequestParam(defaultValue = "10", name = "size") Integer size,
            @RequestParam(defaultValue = "0", name = "minScore") Integer minScore) {
        IPage<MerchantDto> listByScore = merchantService.sortedMerchantsByScore(current, size, minScore);
        return Result.success(listByScore);
    }
}



