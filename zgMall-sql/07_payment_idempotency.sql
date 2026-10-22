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
