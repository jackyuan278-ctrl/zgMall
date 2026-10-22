-- 本地/测试环境商品压测数据脚本：将 tb_item 扩充到至少 100 万条。
-- 不删除、不覆盖现有商品；使用 9000000000000000000 起始的 ID 段，避免与演示商品冲突。
-- 运行前确认连接的是 zg_item 测试库。生成 1,000,000 行时会占用磁盘并执行较久。

USE zg_item;

DROP PROCEDURE IF EXISTS generate_million_test_items;
DELIMITER $$
CREATE PROCEDURE generate_million_test_items()
BEGIN
    DECLARE v_batch INT DEFAULT 0;
    DECLARE v_base BIGINT DEFAULT 9000000000000000000;
    DECLARE v_inserted BIGINT DEFAULT 0;

    WHILE v_batch < 100 DO
        INSERT INTO tb_item
            (id, name, price, stock, image, category_id, brand_id, spec, sales, status, description)
        SELECT
            v_base + (v_batch * 10000 + seq.n),
            CONCAT('压测测试商品-', LPAD(v_batch * 10000 + seq.n, 7, '0')),
            990 + MOD(v_batch * 10000 + seq.n, 500000),
            MOD(v_batch * 10000 + seq.n, 1000),
            '/images/ai/zg_phone_blue.jpg',
            1 + MOD(v_batch * 10000 + seq.n, 6),
            1 + MOD(v_batch * 10000 + seq.n, 94),
            CONCAT('测试规格-', MOD(v_batch * 10000 + seq.n, 20)),
            MOD(v_batch * 10000 + seq.n, 100000),
            1,
            '仅供本地或测试环境压测使用的合成商品数据'
        FROM (
            SELECT d0.n + d1.n * 10 + d2.n * 100 + d3.n * 1000 AS n
            FROM (SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
                  UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) d0
            CROSS JOIN (SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
                  UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) d1
            CROSS JOIN (SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
                  UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) d2
            CROSS JOIN (SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
                  UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) d3
        ) seq
        WHERE NOT EXISTS (
            SELECT 1 FROM tb_item existing
            WHERE existing.id = v_base + (v_batch * 10000 + seq.n)
        );

        SET v_inserted = v_inserted + ROW_COUNT();
        SET v_batch = v_batch + 1;
    END WHILE;

    SELECT COUNT(*) AS total_item_count,
           v_inserted AS inserted_test_item_count
    FROM tb_item;
END$$
DELIMITER ;

CALL generate_million_test_items();
DROP PROCEDURE generate_million_test_items;

