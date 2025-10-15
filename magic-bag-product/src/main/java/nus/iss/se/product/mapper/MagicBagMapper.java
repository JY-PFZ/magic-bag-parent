package nus.iss.se.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import nus.iss.se.product.entity.MagicBag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * MagicBag Mapper接口
 * 继承MyBatis-Plus的BaseMapper，提供基础CRUD操作
 */
@Mapper
public interface MagicBagMapper extends BaseMapper<MagicBag> {
    
    /**
     * 根据分类查询活跃的盲盒
     * @param category 分类名称
     * @return 盲盒列表
     */
    @Select("SELECT * FROM magic_bags WHERE category = #{category} AND is_active = 1")
    List<MagicBag> findByCategory(String category);
    
    /**
     * 根据商家ID查询活跃的盲盒
     * @param merchantId 商家ID
     * @return 盲盒列表
     */
    @Select("SELECT * FROM magic_bags WHERE merchant_id = #{merchantId} AND is_active = 1")
    List<MagicBag> findByMerchantId(Integer merchantId);
}


