-- 订单服务数据库
CREATE DATABASE IF NOT EXISTS zg_trade DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE zg_trade;

-- 订单表
CREATE TABLE IF NOT EXISTS tb_order (
    id           BIGINT       NOT NULL COMMENT '订单id（雪花ID）',
    user_id      BIGINT       NOT NULL COMMENT '用户id',
    total_fee    INT          NOT NULL COMMENT '订单总金额（分）',
    payment_type TINYINT      NOT NULL DEFAULT 1 COMMENT '支付方式：1余额 2模拟支付',
    status       TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1未付款 2已付款 3已发货 4已完成 5已关闭',
    receiver     VARCHAR(50)  NOT NULL DEFAULT '' COMMENT '收货人（下单时地址快照）',
    phone        VARCHAR(20)  NOT NULL DEFAULT '' COMMENT '联系电话（快照）',
    address      VARCHAR(500) NOT NULL DEFAULT '' COMMENT '收货地址（快照）',
    remark       VARCHAR(200) NULL COMMENT '买家备注',
    create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
    pay_time     DATETIME     NULL COMMENT '支付时间',
    consign_time DATETIME     NULL COMMENT '发货时间',
    end_time     DATETIME     NULL COMMENT '完成时间',
    close_time   DATETIME     NULL COMMENT '关闭时间',
    update_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_status (status),
    KEY idx_create_time (create_time)
) ENGINE = InnoDB COMMENT = '订单表';

-- 订单明细表
CREATE TABLE IF NOT EXISTS tb_order_detail (
    id          BIGINT       NOT NULL COMMENT '明细id（雪花ID）',
    order_id    BIGINT       NOT NULL COMMENT '订单id',
    item_id     BIGINT       NOT NULL COMMENT '商品id',
    name        VARCHAR(255) NOT NULL COMMENT '商品名称（下单时快照）',
    price       INT          NOT NULL COMMENT '商品单价（分，快照）',
    num         INT          NOT NULL COMMENT '购买数量',
    image       VARCHAR(255) NOT NULL DEFAULT '' COMMENT '商品图片（快照）',
    spec        VARCHAR(500) NOT NULL DEFAULT '' COMMENT '规格（快照）',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_order_id (order_id)
) ENGINE = InnoDB COMMENT = '订单明细表';
