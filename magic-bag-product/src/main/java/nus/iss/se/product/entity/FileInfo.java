package nus.iss.se.product.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文件信息实体类
 * 对应数据库表 file_info
 */
@Data
@TableName("file_info")
public class FileInfo {
    @TableId
    private Integer id;
    private String fileId;
    private String originalName;
    private String filePath;
    private Long fileSize;
    private String fileType;
    private String mimeType;
    private Integer uploadUserId;
    private String fileCategory;
    private String s3Bucket;
    private String s3Key;
    private String fileUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
