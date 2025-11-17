package nus.iss.se.product.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import nus.iss.se.common.Result;
import nus.iss.se.product.dto.MerchantDto;
import nus.iss.se.product.dto.MerchantUpdateDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "magic-bag-merchant")
public interface MerchantClient {

    @GetMapping("/merchant")
    Result<List<MerchantDto>> getAllMerchants();

    @GetMapping("/merchant/{id}")
    Result<MerchantDto> getMerchantById(@PathVariable("id") Integer id);

    @PutMapping("/merchant/profile")
    Result<Void> updateMerchantProfile(@RequestBody MerchantUpdateDto merchantDto);

    @GetMapping("/merchant/sorted-by-score")
    Result<IPage<MerchantDto>> sortedByScore(@RequestParam("current") Integer current,
                                            @RequestParam("size") Integer size,
                                            @RequestParam("minScore") Integer minScore);
}


