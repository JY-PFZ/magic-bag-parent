-- 1. 在 orders 表中添加 order_type 字段
--    这个字段用于区分是单商品订单还是购物车生成的订单
ALTER TABLE `orders`
ADD COLUMN `order_type` ENUM('single','cart') NOT NULL DEFAULT 'single' COMMENT '订单类型：single-单商品订单，cart-购物车订单';

-- 2. 创建 order_items 表
--    这个表用于存储购物车生成的订单的商品明细，支持一个订单包含多个商品
CREATE TABLE `order_items` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `magic_bag_id` BIGINT NOT NULL COMMENT '魔法袋ID',
    `quantity` INT NOT NULL COMMENT '购买数量',
    `unit_price` DECIMAL(8,2) NOT NULL COMMENT '单价',
    `subtotal` DECIMAL(8,2) NOT NULL COMMENT '小计',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `order_items_order_id_IDX` (`order_id`),
    KEY `order_items_magic_bag_id_IDX` (`magic_bag_id`),
    CONSTRAINT `fk_order_items_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_order_items_magic_bag` FOREIGN KEY (`magic_bag_id`) REFERENCES `magic_bags` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单明细表';

-- 3. （可选）为现有单商品订单设置 order_type
--    如果 orders 表中已有数据，可以运行此语句将其标记为 'single' 类型
UPDATE `orders`
SET `order_type` = 'single'
WHERE `order_type` IS NULL;

