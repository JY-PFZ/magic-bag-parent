package nus.iss.se.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import nus.iss.se.product.dto.CreateMagicBagRequest;
import nus.iss.se.product.dto.MagicBagDto;
import nus.iss.se.product.dto.MagicBagListResponse;
import nus.iss.se.product.dto.UpdateMagicBagRequest;
import nus.iss.se.product.entity.MagicBag;
import nus.iss.se.product.mapper.MagicBagMapper;
import nus.iss.se.product.service.IMagicBagService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * MagicBag 服务实现类
 * 实现盲盒相关的业务逻辑
 */
@Service
@RequiredArgsConstructor
public class MagicBagServiceImpl implements IMagicBagService {
    
    private final MagicBagMapper magicBagMapper;
    
    @Override
    public MagicBagListResponse getAllMagicBags(Integer page, Integer size) {
        Page<MagicBag> magicBagPage = new Page<>(page, size);
        QueryWrapper<MagicBag> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_active", true);
        
        Page<MagicBag> result = magicBagMapper.selectPage(magicBagPage, queryWrapper);
        
        List<MagicBagDto> magicBagDtos = result.getRecords().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        MagicBagListResponse response = new MagicBagListResponse();
        response.setMagicBags(magicBagDtos);
        response.setTotalItems(result.getTotal());
        response.setCurrentPage((int) result.getCurrent());
        response.setPageSize((int) result.getSize());
        response.setTotalPages((int) result.getPages());
        
        return response;
    }
    
    @Override
    public MagicBagDto getMagicBagById(Integer id) {
        MagicBag magicBag = magicBagMapper.selectById(id);
        if (magicBag == null) {
            return null;
        }
        return convertToDto(magicBag);
    }
    
    @Override
    public List<MagicBagDto> getMagicBagsByCategory(String category) {
        List<MagicBag> magicBags = magicBagMapper.findByCategory(category);
        return magicBags.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public MagicBagDto createMagicBag(CreateMagicBagRequest request) {
        // 检查同一商家同一天是否有相同标题的产品
        QueryWrapper<MagicBag> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("merchant_id", request.getMerchantId())
                   .eq("available_date", request.getAvailableDate())
                   .eq("title", request.getTitle());
        
        MagicBag existingBag = magicBagMapper.selectOne(queryWrapper);
        if (existingBag != null) {
            return null;
        }
        
        // 创建新产品
        MagicBag magicBag = new MagicBag();
        BeanUtils.copyProperties(request, magicBag);
        magicBag.setIsActive(true);
        magicBag.setCreatedAt(LocalDateTime.now());
        magicBag.setUpdatedAt(LocalDateTime.now());
        
        int result = magicBagMapper.insert(magicBag);
        if (result <= 0) {
            return null;
        }
        
        return convertToDto(magicBag);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public MagicBagDto updateMagicBag(Integer id, UpdateMagicBagRequest request) {
        // 检查产品是否存在
        MagicBag existingBag = magicBagMapper.selectById(id);
        if (existingBag == null) {
            return null;
        }
        
        // 更新产品信息
        UpdateWrapper<MagicBag> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id);
        
        if (request.getTitle() != null) {
            updateWrapper.set("title", request.getTitle());
        }
        if (request.getDescription() != null) {
            updateWrapper.set("description", request.getDescription());
        }
        if (request.getPrice() != null) {
            updateWrapper.set("price", request.getPrice());
        }
        if (request.getQuantity() != null) {
            updateWrapper.set("quantity", request.getQuantity());
        }
        if (request.getPickupStartTime() != null) {
            updateWrapper.set("pickup_start_time", request.getPickupStartTime());
        }
        if (request.getPickupEndTime() != null) {
            updateWrapper.set("pickup_end_time", request.getPickupEndTime());
        }
        if (request.getAvailableDate() != null) {
            updateWrapper.set("available_date", request.getAvailableDate());
        }
        if (request.getCategory() != null) {
            updateWrapper.set("category", request.getCategory());
        }
        if (request.getImageUrl() != null) {
            updateWrapper.set("image_url", request.getImageUrl());
        }
        if (request.getIsActive() != null) {
            updateWrapper.set("is_active", request.getIsActive());
        }
        
        updateWrapper.set("updated_at", LocalDateTime.now());
        
        int result = magicBagMapper.update(null, updateWrapper);
        if (result <= 0) {
            return null;
        }
        
        // 返回更新后的产品信息
        MagicBag updatedBag = magicBagMapper.selectById(id);
        return convertToDto(updatedBag);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteMagicBag(Integer id) {
        // 检查产品是否存在
        MagicBag existingBag = magicBagMapper.selectById(id);
        if (existingBag == null) {
            return false;
        }
        
        // 软删除：设置 is_active = false
        UpdateWrapper<MagicBag> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id)
                    .set("is_active", false)
                    .set("updated_at", LocalDateTime.now());
        
        int result = magicBagMapper.update(null, updateWrapper);
        if (result <= 0) {
            return false;
        }
        
        return true;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchDeleteMagicBags(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        
        // 批量软删除
        UpdateWrapper<MagicBag> updateWrapper = new UpdateWrapper<>();
        updateWrapper.in("id", ids)
                    .set("is_active", false)
                    .set("updated_at", LocalDateTime.now());
        
        int result = magicBagMapper.update(null, updateWrapper);
        return result;
    }
    
    /**
     * 将实体转换为DTO
     * @param magicBag 实体对象
     * @return DTO对象
     */
    private MagicBagDto convertToDto(MagicBag magicBag) {
        MagicBagDto dto = new MagicBagDto();
        BeanUtils.copyProperties(magicBag, dto);
        return dto;
    }
}
