package nus.iss.se.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import nus.iss.se.product.entity.FileInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 文件信息 Mapper
 */
@Mapper
public interface FileInfoMapper extends BaseMapper<FileInfo> {
    
    /**
     * 根据文件ID获取文件信息
     */
    @Select("SELECT * FROM file_info WHERE file_id = #{fileId}")
    FileInfo findByFileId(@Param("fileId") String fileId);
    
    /**
     * 根据上传用户ID获取文件列表
     */
    @Select("SELECT * FROM file_info WHERE upload_user_id = #{userId}")
    List<FileInfo> findByUploadUserId(@Param("userId") Integer userId);
    
    /**
     * 根据文件分类获取文件列表
     */
    @Select("SELECT * FROM file_info WHERE file_category = #{category}")
    List<FileInfo> findByFileCategory(@Param("category") String category);
}
