package nus.iss.se.merchant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import nus.iss.se.merchant.entity.Merchant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MerchantMapper extends BaseMapper<Merchant> {

    @Select("SELECT * FROM merchants WHERE status = #{status}")
    List<Merchant> findByStatus(@Param("status") String status);

    @Select("SELECT * FROM merchants WHERE phone = #{phone}")
    Merchant findByPhone(@Param("phone") String phone);

    @Select("SELECT * FROM merchants WHERE user_id = #{userId}")
    Merchant findByUserId(@Param("userId") Integer userId);

    @Select("SELECT * FROM merchants WHERE status = 'approved' AND score >= #{minScore} ORDER BY score DESC")
    IPage<Merchant> findMerchantsByScore(Page<Merchant> page, @Param("minScore") Double minScore);

    @Select("SELECT * FROM merchants WHERE status = 'approved'")
    List<Merchant> findAllApprovedMerchants();
}



