-- 初始化演示数据（可重复执行：先清空再插入）
-- 演示账号：demo / 123456，tester / 123456（BCrypt 存储）
-- 商品图当前为前端本地图（zgmall-web/public/images），接后端后可换成 OSS 完整 URL

-- ==================== 用户服务 zg_user ====================
USE zg_user;

DELETE FROM tb_address;
DELETE FROM tb_user;

INSERT INTO tb_user (id, username, password, nickname, phone, avatar, balance, status) VALUES
(1, 'demo',   '$2a$10$.AGdGJ2oyKYf0b9Gaka5.e1jOYrDX6m9gMxtx7LGRW7AdujHtAw7S', '张同学', '13800138000', '', 1000000, 1),
(2, 'tester', '$2a$10$JItU47HwvkDYMdPg0jzTE.JA9mUDlXgUdJiL/jG2FLYMlwyeHhgEi', '李同学', '13900139000', '',  500000, 1);

INSERT INTO tb_address (id, user_id, receiver, phone, province, city, district, detail, is_default) VALUES
(1, 1, '张同学', '13800138000', '江西省', '南昌市', '红谷滩区', '学府大道999号 南昌大学前湖校区 3栋302', 1),
(2, 1, '张同学', '13800138000', '江西省', '南昌市', '青山湖区', '南京东路235号 南昌大学青山湖校区', 0);

-- ==================== 商品服务 zg_item ====================
USE zg_item;

DELETE FROM tb_item;
DELETE FROM tb_category;
DELETE FROM tb_brand;

INSERT INTO tb_category (id, name, sort) VALUES
(1, '手机数码', 1),
(2, '电脑办公', 2),
(3, '家用电器', 3),
(4, '服饰鞋包', 4),
(5, '食品生鲜', 5),
(6, '运动户外', 6);

INSERT INTO tb_brand (id, name) VALUES
(1, '小米'), (2, '苹果'), (3, '华为'), (4, '联想'), (5, '罗技'), (6, '米家'), (7, '美的'),
(8, '海尔'), (9, '优衣库'), (10, '耐克'), (11, '三只松鼠'), (12, '大疆'), (13, '迪卡侬'), (14, '京造');

INSERT INTO tb_item (id, name, price, stock, image, category_id, brand_id, spec, sales, status, description) VALUES
(1001, '小米14 Pro 5G手机 骁龙8Gen3 徕卡光学镜头 16GB+512GB 黑色', 499900, 128, '/images/p1001.jpg', 1, 1, '16GB+512GB / 骁龙8Gen3 / 6.73英寸2K屏', 2341, 1, '第二代骁龙8移动平台，徕卡可变光圈主摄，2K全等深微曲屏，120W秒充。'),
(1002, 'Apple iPhone 15 128GB 蓝色 支持移动联通电信5G 双卡双待', 529900, 86, '/images/p1002.webp', 1, 2, '128GB / A16芯片 / 6.1英寸超视网膜屏', 8902, 1, 'A16仿生芯片，4800万像素主摄，灵动岛设计，USB-C接口。'),
(1003, '华为MatePad 11英寸平板电脑 8GB+128GB 深空灰 学习娱乐平板', 199900, 200, '/images/p1003.jpg', 2, 3, '8GB+128GB / 120Hz高刷 / HarmonyOS', 1205, 1, '11英寸120Hz全面屏，多屏协同，平行视界，学习办公好帮手。'),
(1004, '联想拯救者Y7000P 电竞游戏笔记本电脑 RTX4060 2.5K 165Hz', 749900, 45, '/images/p1004.webp', 2, 4, 'i9-14900HX / 16G / 1T固态 / RTX4060', 678, 1, '14代酷睿i9处理器，RTX4060独显，2.5K 165Hz电竞屏，霜刃散热。'),
(1005, '罗技MX Master 3S 无线蓝牙鼠标 人体工学 静音办公', 54900, 320, '/images/p1005.jpg', 2, 5, '8K DPI / 静音按键 / 多设备切换', 3456, 1, '8000DPI任意表面追踪，静音微动，电磁滚轮，跨三台设备无缝切换。'),
(1006, 'Apple AirPods Pro 2 无线蓝牙耳机 主动降噪 运动级防水', 149900, 150, '/images/p1006.webp', 1, 2, 'H2芯片 / 主动降噪 / USB-C充电盒', 5623, 1, 'H2芯片更强主动降噪，自适应通透模式，个性化空间音频。'),
(1007, '米家空气净化器4 Pro 除甲醛除菌 家用卧室智能净化机', 109900, 98, '/images/p1007.jpg', 3, 6, '500m³/h颗粒物CADR / OLED触控屏', 890, 1, '三重滤芯，500大风量，米家APP联动，OLED触控显示屏。'),
(1008, '美的空调 大1.5匹 新一级能效 变频冷暖 自清洁挂机', 229900, 60, '/images/p1008.jpg', 3, 7, '1.5匹 / 新一级能效 / 35㎡适用', 1567, 1, '新一级能效省电，56℃高温自清洁，一晚1度电，静音舒适。'),
(1009, '海尔滚筒洗衣机 10公斤 大容量 除菌螨 智能投放', 189900, 72, '/images/p1009.jpg', 3, 8, '10kg / 1.1洗净比 / BLDC变频', 723, 1, '10公斤大容量，微蒸汽空气洗除菌螨，智能投放洗涤剂。'),
(1010, '优衣库男装 摇粒绒外套 拉链休闲夹克 秋冬保暖 25新款', 19900, 500, '/images/p1010.jpg', 4, 9, 'S-XL / 多色可选 / 100%聚酯纤维', 4321, 1, '蓬松摇粒绒，轻盈保暖不易起静电，多色可选，秋冬百搭。'),
(1011, 'Nike耐克 Air Zoom Pegasus 40 男子缓震跑步鞋', 59900, 210, '/images/p1011.webp', 4, 10, '39-45码 / React泡棉 / 飞线鞋面', 2890, 1, '前后Zoom Air气垫，React中底泡棉，透气飞线鞋面，日常慢跑首选。'),
(1012, '三只松鼠每日坚果 30包750g 混合坚果零食礼包', 6900, 1000, '/images/p1012.jpg', 5, 11, '750g/30包 / 6种坚果+3种果干', 12340, 1, '科学配比每日一包，6种坚果仁搭配3种果干，独立小包装。'),
(1013, '大疆 DJI Osmo Pocket 3 一英寸口袋云台相机 全能套装', 299900, 38, '/images/p1013.jpg', 1, 12, '一英寸CMOS / 2英寸旋转触摸屏 / 4K120fps', 987, 1, '一英寸传感器，三轴机械增稳，2英寸旋转触摸屏，Vlog神器。'),
(1014, '迪卡侬 运动跑步T恤 男速干短袖 透气健身衣服 HUB', 4990, 800, '/images/p1014.webp', 6, 13, 'S-2XL / 速干面料 / 多色', 6543, 1, '速干透气面料，运动剪裁不束缚，跑步健身日常百搭。'),
(1015, '小米手环9 NFC版 运动健康智能手环 表盘商城', 24900, 660, '/images/p1015.webp', 1, 1, '1.62英寸AMOLED / NFC / 16天续航', 9217, 1, '轻薄金属机身，心率血氧监测，NFC门禁公交，16天长续航。'),
(1016, '京东京造 记忆棉枕头 颈椎护颈助眠枕 慢回弹低枕', 12900, 430, '/images/p1016.jpg', 5, 14, '慢回弹记忆棉 / B型曲线 / 可洗枕套', 3210, 1, '太空记忆棉慢回弹，人体工学曲线承托颈椎，抗菌可拆洗枕套。');

-- ==================== 订单服务 zg_trade ====================
USE zg_trade;

DELETE FROM tb_order_detail;
DELETE FROM tb_order;

INSERT INTO tb_order (id, user_id, total_fee, payment_type, status, receiver, phone, address, create_time, pay_time) VALUES
(10001, 1, 499900, 1, 1, '张同学', '13800138000', '江西省南昌市红谷滩区 学府大道999号 南昌大学前湖校区 3栋302', NOW() - INTERVAL 2 HOUR, NULL),
(10002, 1, 174800, 1, 2, '张同学', '13800138000', '江西省南昌市红谷滩区 学府大道999号 南昌大学前湖校区 3栋302', NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 23 HOUR);

INSERT INTO tb_order_detail (id, order_id, item_id, name, price, num, image, spec) VALUES
(11001, 10001, 1001, '小米14 Pro 5G手机 骁龙8Gen3 徕卡光学镜头 16GB+512GB 黑色', 499900, 1, '/images/p1001.jpg', '16GB+512GB / 骁龙8Gen3 / 6.73英寸2K屏'),
(11002, 10002, 1006, 'Apple AirPods Pro 2 无线蓝牙耳机 主动降噪 运动级防水', 149900, 1, '/images/p1006.webp', 'H2芯片 / 主动降噪 / USB-C充电盒'),
(11003, 10002, 1015, '小米手环9 NFC版 运动健康智能手环 表盘商城', 24900, 1, '/images/p1015.webp', '1.62英寸AMOLED / NFC / 16天续航');

-- ==================== 支付服务 zg_pay ====================
USE zg_pay;

DELETE FROM tb_pay_order;

INSERT INTO tb_pay_order (id, biz_order_no, pay_user_id, amount, status, pay_time) VALUES
(90001, 10002, 1, 174800, 2, NOW() - INTERVAL 23 HOUR);
