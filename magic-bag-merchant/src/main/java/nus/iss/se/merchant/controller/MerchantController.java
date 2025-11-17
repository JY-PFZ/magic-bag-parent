package nus.iss.se.merchant.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nus.iss.se.common.Result;
import nus.iss.se.common.cache.UserContext;
import nus.iss.se.common.constant.ResultStatus;
import nus.iss.se.common.exception.BusinessException;
import nus.iss.se.merchant.common.UserContextHolder;
import nus.iss.se.merchant.dto.MerchantDto;
import nus.iss.se.merchant.dto.MerchantLocationDto;
import nus.iss.se.merchant.dto.MerchantUpdateDto;
import nus.iss.se.merchant.entity.Merchant;
import nus.iss.se.merchant.service.IMerchantService;
import nus.iss.se.merchant.service.MerchantLocationService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/merchant")
@RequiredArgsConstructor
@Tag(name = "Merchant API", description = "商户管理服务")
public class MerchantController {

    private final IMerchantService merchantService;
    private final UserContextHolder userContextHolder;
    private final MerchantLocationService merchantLocationService;

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

    @GetMapping("user/{id}")
    @Operation(summary = "根据ID获取商家详情")
    public Result<MerchantDto> getMerchantByUserId(@PathVariable Integer id) {
        LambdaQueryWrapper<Merchant> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Merchant::getUserId,id);
        Merchant merchant = merchantService.getOne(queryWrapper);

        MerchantDto merchantDto = new MerchantDto();
        BeanUtils.copyProperties(merchantDto,merchant);
        return Result.success(merchantDto);
    }

    @GetMapping("/my")
    @Operation(summary = "获取当前商家信息", description = "获取当前登录用户的商家详细信息")
    public Result<MerchantDto> getMyMerchantProfile() {
        UserContext currentUser = userContextHolder.getCurrentUser();
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ResultStatus.FAIL, "User context not found.");
        }
        
        // 手动检查用户角色
        if (!"MERCHANT".equals(currentUser.getRole())) {
            throw new BusinessException(ResultStatus.FAIL, "Only merchants can access this resource.");
        }
        
        Integer userId = currentUser.getId();
        MerchantDto merchant = merchantService.findByUserId(userId);
        if (merchant == null) {
            return Result.error(ResultStatus.MERCHANT_NOT_FOUND.getCode(), "No merchant profile associated with the current user.");
        }
        return Result.success(merchant);
    }

    @PostMapping("/register")
    @Operation(summary = "注册商家信息", description = "用户注册自己的店铺信息")
    public Result<Void> registerMerchantProfile(@RequestBody @Valid MerchantUpdateDto merchantDto) {
        merchantService.registerMerchant(merchantDto);
        return Result.success();
    }

    @GetMapping("/nearby")
    @Operation(summary = "查询周边商家，根据距离排序", description = "根据经纬度查询周边商家")
    public Result<List<MerchantLocationDto>> getNearby(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(defaultValue = "1") double radius) {
        
        List<MerchantLocationDto> nearbyMerchants = merchantLocationService.getNearbyMerchants(lon, lat, radius);
        return Result.success(nearbyMerchants);
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



