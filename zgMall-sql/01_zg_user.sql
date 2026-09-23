-- 用户服务数据库
CREATE DATABASE IF NOT EXISTS zg_user DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE zg_user;

-- 用户表
CREATE TABLE IF NOT EXISTS tb_user (
    id          BIGINT       NOT NULL COMMENT '用户id（雪花ID）',
    username    VARCHAR(50)  NOT NULL COMMENT '用户名（登录账号）',
    password    VARCHAR(100) NOT NULL COMMENT '密码（BCrypt 加密）',
    nickname    VARCHAR(50)  NOT NULL DEFAULT '' COMMENT '昵称',
    phone       VARCHAR(20)  NOT NULL DEFAULT '' COMMENT '手机号',
    avatar      VARCHAR(255) NOT NULL DEFAULT '' COMMENT '头像url',
    balance     INT          NOT NULL DEFAULT 0 COMMENT '账户余额（分）',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1正常 0禁用',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    KEY idx_phone (phone)
) ENGINE = InnoDB COMMENT = '用户表';

-- 收货地址表
CREATE TABLE IF NOT EXISTS tb_address (
    id          BIGINT       NOT NULL COMMENT '地址id（雪花ID）',
    user_id     BIGINT       NOT NULL COMMENT '用户id',
    receiver    VARCHAR(50)  NOT NULL COMMENT '收货人',
    phone       VARCHAR(20)  NOT NULL COMMENT '联系电话',
    province    VARCHAR(50)  NOT NULL DEFAULT '' COMMENT '省',
    city        VARCHAR(50)  NOT NULL DEFAULT '' COMMENT '市',
    district    VARCHAR(50)  NOT NULL DEFAULT '' COMMENT '区/县',
    detail      VARCHAR(255) NOT NULL COMMENT '详细地址',
    is_default  TINYINT      NOT NULL DEFAULT 0 COMMENT '是否默认：1是 0否',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id)
) ENGINE = InnoDB COMMENT = '收货地址表';
