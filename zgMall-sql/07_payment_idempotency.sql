-- 已有环境执行一次：扩展支付状态，并创建余额扣款幂等流水。
USE zg_pay;
ALTER TABLE tb_pay_order
    MODIFY COLUMN status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1待支付 2已支付 3处理中';

USE zg_user;
CREATE TABLE IF NOT EXISTS tb_balance_deduction (
    id            BIGINT NOT NULL AUTO_INCREMENT,
    biz_order_no  BIGINT NOT NULL COMMENT '业务订单号（唯一幂等键）',
    user_id       BIGINT NOT NULL,
    amount        INT NOT NULL COMMENT '扣款金额（分）',
    create_time   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_biz_order_no (biz_order_no),
    KEY idx_user_id (user_id)
) ENGINE = InnoDB COMMENT = '余额扣款幂等流水';

-- 支付成功事件 Outbox：支付状态与待投递事件在同一事务落库。
CREATE TABLE IF NOT EXISTS tb_pay_event_outbox (
    id            BIGINT NOT NULL AUTO_INCREMENT,
    biz_order_no  BIGINT NOT NULL COMMENT '交易订单号',
    published     TINYINT NOT NULL DEFAULT 0 COMMENT '投递状态：0待投递 1已投递',
    create_time   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_time DATETIME NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_biz_order_no (biz_order_no),
    KEY idx_published_id (published, id)
) ENGINE = InnoDB COMMENT = '支付成功事件 Outbox';
