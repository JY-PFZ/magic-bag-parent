package nus.iss.se.product.service;

import nus.iss.se.product.dto.CreateMagicBagRequest;
import nus.iss.se.product.dto.MagicBagDto;
import nus.iss.se.product.dto.MagicBagListResponse;
import nus.iss.se.product.dto.UpdateMagicBagRequest;

import java.util.List;

/**
 * MagicBag 服务接口
 * 定义盲盒相关的业务方法
 */
public interface IMagicBagService {
    
    /**
     * 获取所有盲盒列表（分页）
     * @param page 页码
     * @param size 每页大小
     * @return 分页响应结果
     */
    MagicBagListResponse getAllMagicBags(Integer page, Integer size);
    
    /**
     * 根据ID获取盲盒详情
     * @param id 盲盒ID
     * @return 盲盒详情
     */
    MagicBagDto getMagicBagById(Integer id);
    
    /**
     * 根据分类获取盲盒
     * @param category 分类名称
     * @return 盲盒列表
     */
    List<MagicBagDto> getMagicBagsByCategory(String category);
    
    /**
     * 根据商户ID获取盲盒
     * @param merchantId 商户ID
     * @return 盲盒列表
     */
    List<MagicBagDto> getMagicBagsByMerchantId(Integer merchantId);
    
    /**
     * 创建新产品
     * @param request 创建产品请求
     * @return 创建的产品信息
     */
    MagicBagDto createMagicBag(CreateMagicBagRequest request);
    
    /**
     * 更新产品信息
     * @param id 产品ID
     * @param request 更新产品请求
     * @return 更新后的产品信息
     */
    MagicBagDto updateMagicBag(Integer id, UpdateMagicBagRequest request);
    
    /**
     * 删除产品（软删除）
     * @param id 产品ID
     * @return 是否删除成功
     */
    boolean deleteMagicBag(Integer id);
    
    /**
     * 批量删除产品（软删除）
     * @param ids 产品ID列表
     * @return 删除成功的数量
     */
    int batchDeleteMagicBags(List<Integer> ids);

	List<MagicBagDto> getBatchByIds(List<Integer> ids);
}
