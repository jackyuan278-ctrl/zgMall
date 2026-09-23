-- 支付服务数据库
CREATE DATABASE IF NOT EXISTS zg_pay DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE zg_pay;

-- 支付单表
CREATE TABLE IF NOT EXISTS tb_pay_order (
    id            BIGINT   NOT NULL COMMENT '支付单id（雪花ID）',
    biz_order_no  BIGINT   NOT NULL COMMENT '业务订单号（trade服务的订单id）',
    pay_user_id   BIGINT   NOT NULL COMMENT '支付用户id',
    amount        INT      NOT NULL COMMENT '支付金额（分）',
    status        TINYINT  NOT NULL DEFAULT 1 COMMENT '状态：1未支付 2已支付',
    create_time   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    pay_time      DATETIME NULL COMMENT '支付时间',
    update_time   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_biz_order_no (biz_order_no)
) ENGINE = InnoDB COMMENT = '支付单表';
