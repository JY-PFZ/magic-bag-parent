-- 文件信息表创建脚本
CREATE TABLE `file_info` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `file_id` varchar(64) NOT NULL COMMENT '文件唯一ID',
    `original_name` varchar(255) NOT NULL COMMENT '原始文件名',
    `file_path` varchar(500) NOT NULL COMMENT 'S3文件路径',
    `file_size` bigint NOT NULL COMMENT '文件大小',
    `file_type` varchar(50) NOT NULL COMMENT '文件类型',
    `mime_type` varchar(100) NOT NULL COMMENT 'MIME类型',
    `upload_user_id` bigint NOT NULL COMMENT '上传用户ID',
    `file_category` varchar(50) NOT NULL COMMENT '文件分类',
    `s3_bucket` varchar(100) NOT NULL COMMENT 'S3存储桶',
    `s3_key` varchar(500) NOT NULL COMMENT 'S3对象键',
    `file_url` varchar(500) NOT NULL COMMENT '文件访问URL',
    `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_file_id` (`file_id`),
    KEY `idx_upload_user` (`upload_user_id`),
    KEY `idx_file_category` (`file_category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文件信息表';
