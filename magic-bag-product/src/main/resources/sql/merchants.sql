-- Merchants 表创建脚本
CREATE TABLE `merchants` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` varchar(100) NOT NULL COMMENT '商家名称',
    `phone` varchar(15) NOT NULL COMMENT '联系手机号',
    `password_hash` varchar(255) NOT NULL COMMENT '密码哈希',
    `business_license` varchar(255) NOT NULL COMMENT '营业执照图片URL',
    `address` varchar(255) NOT NULL COMMENT '详细地址',
    `latitude` decimal(10,8) DEFAULT NULL COMMENT '纬度（预留）',
    `longitude` decimal(11,8) DEFAULT NULL COMMENT '经度（预留）',
    `status` enum('pending','approved','rejected') NOT NULL DEFAULT 'pending' COMMENT '审核状态：待审核/通过/拒绝',
    `score` double DEFAULT 0.0 COMMENT '商家评分',
    `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `approved_at` datetime DEFAULT NULL COMMENT '审核通过时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `phone` (`phone`),
    KEY `merchants_status_IDX` (`status`) USING BTREE,
    KEY `merchants_longitude_IDX` (`longitude`,`latitude`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商家表';
