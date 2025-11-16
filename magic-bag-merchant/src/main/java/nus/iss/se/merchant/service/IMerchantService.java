package nus.iss.se.merchant.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import nus.iss.se.merchant.dto.MerchantDto;
import nus.iss.se.merchant.dto.MerchantUpdateDto;
import nus.iss.se.merchant.entity.Merchant;
import nus.iss.se.merchant.kafka.event.MerchantProcessedEvent;

import java.util.List;

public interface IMerchantService extends IService<Merchant> {

    List<MerchantDto> getAllMerchants();

    MerchantDto getMerchantById(Integer id);

    Integer getMerchantIdByUserId(Integer userId);
    
    MerchantDto findByUserId(Integer userId);
    
    void registerMerchant(MerchantUpdateDto merchantDto);

    void handleRegisterResult(MerchantProcessedEvent event);

    void updateMerchantProfile(MerchantUpdateDto merchantDto, Integer currentUserId);

    IPage<MerchantDto> sortedMerchantsByScore(Integer current, Integer size, Integer minScore);
}



