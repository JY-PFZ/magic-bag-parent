package nus.iss.se.merchant.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import nus.iss.se.common.constant.ResultStatus;
import nus.iss.se.common.exception.BusinessException;
import nus.iss.se.kafka.event.EventEnvelope;
import nus.iss.se.kafka.publisher.KafkaEventPublisher;
import nus.iss.se.merchant.common.UserContextHolder;
import nus.iss.se.merchant.dto.MerchantDto;
import nus.iss.se.merchant.dto.MerchantUpdateDto;
import nus.iss.se.merchant.entity.Merchant;
import nus.iss.se.merchant.kafka.EventTopicType;
import nus.iss.se.merchant.kafka.event.MerchantRegisterEvent;
import nus.iss.se.merchant.mapper.MerchantMapper;
import nus.iss.se.merchant.service.IMerchantService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MerchantServiceImpl extends ServiceImpl<MerchantMapper, Merchant> implements IMerchantService{
    private final ObjectMapper objectMapper;
    private   final KafkaEventPublisher eventPublisher;
    private final UserContextHolder userContextHolder;

    @Override
    public List<MerchantDto> getAllMerchants() {
        List<Merchant> merchants = baseMapper.findAllApprovedMerchants();
        return merchants.stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    public MerchantDto getMerchantById(Integer id) {
        Merchant merchant = baseMapper.selectById(id);
        if (merchant == null) {
            return null;
        }
        return convertToDto(merchant);
    }

    @Override
    public Integer getMerchantIdByUserId(Integer userId) {
        Merchant merchant = baseMapper.selectById(userId);
        if (merchant != null) {
            return merchant.getId();
        }
        return null;
    }
    
    @Override
    public MerchantDto findByUserId(Integer userId) {
        Merchant merchant = baseMapper.findByUserId(userId);
        if (merchant == null) {
            return null;
        }
        return convertToDto(merchant);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void registerMerchant(MerchantUpdateDto merchantDto) {
        // 检查是否已经注册过商户
        Integer userId = userContextHolder.userId();
        Merchant existingMerchant = baseMapper.findByUserId(userId);
        if (existingMerchant != null) {
            throw new BusinessException(ResultStatus.FAIL, "用户已经注册过商户");
        }
        
        Merchant merchant = new Merchant();
        merchant.setUserId(userId);
        merchant.setName(merchantDto.getName());
        merchant.setPhone(merchantDto.getPhone());
        merchant.setBusinessLicense(merchantDto.getBusinessLicense());
        merchant.setAddress(merchantDto.getAddress());
        merchant.setStatus("pending"); // 待审核状态
        merchant.setCreatedAt(new Date());
        merchant.setUpdatedAt(new Date());

        save(merchant);

        try {
            MerchantRegisterEvent event = new MerchantRegisterEvent();
            event.setUserId(userId);
            event.setMerchantId(merchant.getId());
            event.setShopName(merchant.getName());

            String data = objectMapper.writeValueAsString(event);
            EventEnvelope eventEnvelope = EventEnvelope.of(data, EventTopicType.MERCHANT_REGISTERED);
            eventPublisher.publish(eventEnvelope);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMerchantProfile(MerchantUpdateDto merchantDto, Integer currentUserId) {
        Integer merchantId = getMerchantIdByUserId(currentUserId);
        if (merchantId == null) {
            throw new BusinessException(ResultStatus.MERCHANT_NOT_FOUND, "当前用户不是商户");
        }

        Merchant merchant = baseMapper.selectById(merchantId);
        if (merchant == null) {
            throw new BusinessException(ResultStatus.MERCHANT_NOT_FOUND, "商户不存在");
        }

        merchant.setName(merchantDto.getName());
        merchant.setPhone(merchantDto.getPhone());
        merchant.setBusinessLicense(merchantDto.getBusinessLicense());
        merchant.setAddress(merchantDto.getAddress());
        merchant.setUpdatedAt(new Date());

        int result = baseMapper.updateById(merchant);
        if (result <= 0) {
            throw new BusinessException(ResultStatus.FAIL, "更新商户信息失败");
        }
    }

    @Override
    public IPage<MerchantDto> sortedMerchantsByScore(Integer current, Integer size, Integer minScore) {
        Page<Merchant> page = new Page<>(current, size);
        IPage<Merchant> merchantPage = baseMapper.findMerchantsByScore(page, minScore.doubleValue());

        IPage<MerchantDto> dtoPage = new Page<>(current, size, merchantPage.getTotal());
        List<MerchantDto> merchantDtos = merchantPage.getRecords().stream()
                .map(this::convertToDto)
                .toList();
        dtoPage.setRecords(merchantDtos);

        return dtoPage;
    }

    private MerchantDto convertToDto(Merchant merchant) {
        MerchantDto dto = new MerchantDto();
        BeanUtils.copyProperties(merchant, dto);
        return dto;
    }
}



