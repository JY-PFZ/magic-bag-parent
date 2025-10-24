-- 数据修复脚本：修复购物车订单的 bag_id 字段
-- 执行前请备份数据库

-- 1. 查看需要修复的订单数量
SELECT COUNT(*) as cart_orders_without_bag_id
FROM orders 
WHERE order_type = 'cart' 
AND bag_id IS NULL;

-- 2. 修复购物车订单的 bag_id 字段
-- 使用第一个订单项的 magic_bag_id 作为 bag_id
UPDATE orders o 
SET bag_id = (
    SELECT oi.magic_bag_id 
    FROM order_items oi 
    WHERE oi.order_id = o.id 
    ORDER BY oi.id 
    LIMIT 1
)
WHERE o.order_type = 'cart' 
AND o.bag_id IS NULL;

-- 3. 验证修复结果
SELECT COUNT(*) as remaining_cart_orders_without_bag_id
FROM orders 
WHERE order_type = 'cart' 
AND bag_id IS NULL;

-- 4. 查看修复后的订单示例
SELECT o.id, o.order_no, o.order_type, o.bag_id, oi.magic_bag_id
FROM orders o
LEFT JOIN order_items oi ON o.id = oi.order_id
WHERE o.order_type = 'cart'
ORDER BY o.id
LIMIT 10;

-- 5. 检查数据一致性
-- 确保所有购物车订单都有对应的订单项
SELECT o.id, o.order_no, o.order_type, o.bag_id
FROM orders o
WHERE o.order_type = 'cart'
AND NOT EXISTS (
    SELECT 1 FROM order_items oi WHERE oi.order_id = o.id
);

-- 6. 检查订单项和商品的一致性
-- 确保订单项中的商品都存在
SELECT oi.order_id, oi.magic_bag_id
FROM order_items oi
WHERE NOT EXISTS (
    SELECT 1 FROM magic_bags mb WHERE mb.id = oi.magic_bag_id
);
