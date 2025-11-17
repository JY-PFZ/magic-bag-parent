package nus.iss.se.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import nus.iss.se.product.dto.*;
import nus.iss.se.product.entity.MagicBag;

import java.util.List;

public interface IMagicBagService extends IService<MagicBag> {

    MagicBagListResponse getAllMagicBags(Integer page, Integer size);

    MagicBagDto getMagicBagById(Integer id);

    List<MagicBagDto> getMagicBagsByCategory(String category);

    List<MagicBagDto> getMagicBagsByMerchantId(Integer merchantId);

    MagicBagDto createMagicBag(MagicBagCreateDto createDto);

    MagicBagDto updateMagicBag(Integer id, MagicBagUpdateDto updateDto);

    boolean deleteMagicBag(Integer id);

    List<MagicBagDto> getBatchMagicBags(List<Integer> ids);
}
