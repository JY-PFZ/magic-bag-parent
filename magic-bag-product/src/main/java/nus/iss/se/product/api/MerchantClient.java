package nus.iss.se.product.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import nus.iss.se.common.Result;
import nus.iss.se.merchant.dto.MerchantDto;
import nus.iss.se.merchant.dto.MerchantUpdateDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "magic-bag-merchant")
public interface MerchantClient {

    @GetMapping("/merchant/merchants")
    Result<List<MerchantDto>> getAllMerchants();

    @GetMapping("/merchant/merchants/{id}")
    Result<MerchantDto> getMerchantById(@PathVariable("id") Integer id);

    @PutMapping("/merchant/merchants/profile")
    Result<Void> updateMerchantProfile(@RequestBody MerchantUpdateDto merchantDto);

    @GetMapping("/merchant/merchants/sorted-by-score")
    Result<IPage<MerchantDto>> sortedByScore(@RequestParam("current") Integer current,
                                            @RequestParam("size") Integer size,
                                            @RequestParam("minScore") Integer minScore);
}


