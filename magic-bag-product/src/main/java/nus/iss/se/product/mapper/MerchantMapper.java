package nus.iss.se.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import nus.iss.se.product.entity.Merchant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Merchant Mapper
 * 商户数据访问层
 */
@Mapper
public interface MerchantMapper extends BaseMapper<Merchant> {
    
    /**
     * 根据状态获取商户列表
     */
    @Select("SELECT * FROM merchants WHERE status = #{status}")
    List<Merchant> findByStatus(@Param("status") String status);
    
    /**
     * 根据手机号获取商户
     */
    @Select("SELECT * FROM merchants WHERE phone = #{phone}")
    Merchant findByPhone(@Param("phone") String phone);
    
    /**
     * 根据评分排序获取商户（分页）
     */
    @Select("SELECT * FROM merchants WHERE status = 'approved' AND score >= #{minScore} ORDER BY score DESC")
    IPage<Merchant> findMerchantsByScore(Page<Merchant> page, @Param("minScore") Double minScore);
    
    /**
     * 获取所有已审核的商户
     */
    @Select("SELECT * FROM merchants WHERE status = 'approved'")
    List<Merchant> findAllApprovedMerchants();
}
