package nus.iss.se.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import nus.iss.se.product.dto.*;
import nus.iss.se.product.entity.MagicBag;

import java.util.List;

/**
 * MagicBag 服务接口
 * 定义盲盒相关的业务方法
 */
public interface IMagicBagService extends IService<MagicBag> {

    /**
     * 获取所有盲盒列表（分页）
     */
    MagicBagListResponse getAllMagicBags(Integer page, Integer size);

    /**
     * 根据ID获取盲盒详情
     */
    MagicBagDto getMagicBagById(Integer id);

    /**
     * 根据分类获取盲盒
     */
    List<MagicBagDto> getMagicBagsByCategory(String category);

    /**
     * 根据商家ID获取盲盒
     */
    List<MagicBagDto> getMagicBagsByMerchantId(Integer merchantId);

    /**
     * 创建新的盲盒商品
     */
    MagicBagDto createMagicBag(MagicBagCreateDto createDto);

    /**
     * 更新盲盒商品信息
     */
    MagicBagDto updateMagicBag(Integer id, MagicBagUpdateDto updateDto);

    /**
     * 删除盲盒商品
     */
    boolean deleteMagicBag(Integer id);

    /**
     * 批量获取盲盒商品
     */
    List<MagicBagDto> getBatchMagicBags(List<Integer> ids);
}