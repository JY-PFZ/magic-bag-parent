package nus.iss.se.product.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import nus.iss.se.product.dto.MerchantDto;
import nus.iss.se.product.dto.MerchantUpdateDto;

import java.util.List;

/**
 * Merchant Service 接口
 * 定义商户相关的业务方法
 */
public interface IMerchantService {
    
    /**
     * 获取所有已审核的商户列表
     * @return 商户列表
     */
    List<MerchantDto> getAllMerchants();
    
    /**
     * 根据ID获取商户详情
     * @param id 商户ID
     * @return 商户详情
     */
    MerchantDto getMerchantById(Integer id);
    
    /**
     * 根据用户ID获取对应的商户ID
     * @param userId 用户ID
     * @return 商户ID，如果不存在则返回null
     */
    Integer getMerchantIdByUserId(Integer userId);
    
    /**
     * 更新商户信息
     * @param merchantDto 商户更新信息
     * @param currentUserId 当前登录用户ID
     */
    void updateMerchantProfile(MerchantUpdateDto merchantDto, Integer currentUserId);
    
    /**
     * 根据评分排序商户（分页）
     * @param current 当前页
     * @param size 每页大小
     * @param minScore 最低评分
     * @return 分页结果
     */
    IPage<MerchantDto> sortedMerchantsByScore(Integer current, Integer size, Integer minScore);
}
