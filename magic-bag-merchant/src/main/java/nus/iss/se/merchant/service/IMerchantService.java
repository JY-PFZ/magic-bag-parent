package nus.iss.se.merchant.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import nus.iss.se.merchant.dto.MerchantDto;
import nus.iss.se.merchant.dto.MerchantUpdateDto;

import java.util.List;

public interface IMerchantService {

    List<MerchantDto> getAllMerchants();

    MerchantDto getMerchantById(Integer id);

    Integer getMerchantIdByUserId(Integer userId);

    void updateMerchantProfile(MerchantUpdateDto merchantDto, Integer currentUserId);

    IPage<MerchantDto> sortedMerchantsByScore(Integer current, Integer size, Integer minScore);
}



