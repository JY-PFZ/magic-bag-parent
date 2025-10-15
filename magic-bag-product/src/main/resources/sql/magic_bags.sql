-- Magic Bags 表创建脚本
CREATE TABLE `magic_bags` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `merchant_id` bigint NOT NULL COMMENT '所属商家ID',
    `title` varchar(100) NOT NULL COMMENT '标题，如"今日面包盲盒"',
    `description` text COMMENT '描述（可选）',
    `price` decimal(8,2) NOT NULL COMMENT '价格（元）',
    `quantity` int NOT NULL COMMENT '库存数量',
    `pickup_start_time` time NOT NULL COMMENT '可自提开始时间（如 18:00）',
    `pickup_end_time` time NOT NULL COMMENT '可自提结束时间（如 20:00）',
    `available_date` date NOT NULL COMMENT '有效日期（即当天）',
    `category` varchar(50) DEFAULT NULL COMMENT '食物类型标签，如 面包、咖啡、便当',
    `image_url` varchar(255) DEFAULT NULL COMMENT '图片URL',
    `is_active` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否上架',
    `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_merchant_date_title` (`merchant_id`,`available_date`,`title`),
    KEY `magic_bags_merchant_id_IDX` (`merchant_id`,`available_date`) USING BTREE,
    KEY `magic_bags_available_date_IDX` (`available_date`,`is_active`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='魔法袋表（临期食物盲盒）';


