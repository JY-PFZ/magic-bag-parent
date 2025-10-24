package nus.iss.se.merchant.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import nus.iss.se.common.constant.ResultStatus;
import nus.iss.se.common.exception.BusinessException;
import nus.iss.se.merchant.dto.MerchantDto;
import nus.iss.se.merchant.dto.MerchantUpdateDto;
import nus.iss.se.merchant.entity.Merchant;
import nus.iss.se.merchant.mapper.MerchantMapper;
import nus.iss.se.merchant.service.IMerchantService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MerchantServiceImpl implements IMerchantService {

    private final MerchantMapper merchantMapper;

    @Override
    public List<MerchantDto> getAllMerchants() {
        List<Merchant> merchants = merchantMapper.findAllApprovedMerchants();
        return merchants.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public MerchantDto getMerchantById(Integer id) {
        Merchant merchant = merchantMapper.selectById(id);
        if (merchant == null) {
            return null;
        }
        return convertToDto(merchant);
    }

    @Override
    public Integer getMerchantIdByUserId(Integer userId) {
        Merchant merchant = merchantMapper.selectById(userId);
        if (merchant != null) {
            return merchant.getId();
        }
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMerchantProfile(MerchantUpdateDto merchantDto, Integer currentUserId) {
        Integer merchantId = getMerchantIdByUserId(currentUserId);
        if (merchantId == null) {
            throw new BusinessException(ResultStatus.MERCHANT_NOT_FOUND, "当前用户不是商户");
        }

        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new BusinessException(ResultStatus.MERCHANT_NOT_FOUND, "商户不存在");
        }

        merchant.setName(merchantDto.getName());
        merchant.setPhone(merchantDto.getPhone());
        merchant.setBusinessLicense(merchantDto.getBusinessLicense());
        merchant.setAddress(merchantDto.getAddress());
        merchant.setUpdatedAt(new Date());

        int result = merchantMapper.updateById(merchant);
        if (result <= 0) {
            throw new BusinessException(ResultStatus.FAIL, "更新商户信息失败");
        }
    }

    @Override
    public IPage<MerchantDto> sortedMerchantsByScore(Integer current, Integer size, Integer minScore) {
        Page<Merchant> page = new Page<>(current, size);
        IPage<Merchant> merchantPage = merchantMapper.findMerchantsByScore(page, minScore.doubleValue());

        IPage<MerchantDto> dtoPage = new Page<>(current, size, merchantPage.getTotal());
        List<MerchantDto> merchantDtos = merchantPage.getRecords().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        dtoPage.setRecords(merchantDtos);

        return dtoPage;
    }

    private MerchantDto convertToDto(Merchant merchant) {
        MerchantDto dto = new MerchantDto();
        BeanUtils.copyProperties(merchant, dto);
        return dto;
    }
}



