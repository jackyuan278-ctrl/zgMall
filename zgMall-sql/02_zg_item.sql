-- 商品服务数据库
CREATE DATABASE IF NOT EXISTS zg_item DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE zg_item;

-- 商品分类表
CREATE TABLE IF NOT EXISTS tb_category (
    id          BIGINT      NOT NULL COMMENT '分类id',
    name        VARCHAR(50) NOT NULL COMMENT '分类名称',
    sort        INT         NOT NULL DEFAULT 0 COMMENT '排序（越小越靠前）',
    create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE = InnoDB COMMENT = '商品分类表';

-- 品牌表
CREATE TABLE IF NOT EXISTS tb_brand (
    id          BIGINT       NOT NULL COMMENT '品牌id',
    name        VARCHAR(50)  NOT NULL COMMENT '品牌名称',
    logo        VARCHAR(255) NOT NULL DEFAULT '' COMMENT '品牌logo',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE = InnoDB COMMENT = '品牌表';

-- 商品表
CREATE TABLE IF NOT EXISTS tb_item (
    id          BIGINT        NOT NULL COMMENT '商品id',
    name        VARCHAR(255)  NOT NULL COMMENT '商品名称',
    price       INT           NOT NULL COMMENT '价格（分）',
    stock       INT           NOT NULL DEFAULT 0 COMMENT '库存',
    image       VARCHAR(500)  NOT NULL DEFAULT '' COMMENT '商品图片（当前为前端本地图 /images/xxx，上线换 OSS 完整 URL）',
    category_id BIGINT        NOT NULL COMMENT '分类id',
    brand_id    BIGINT        NOT NULL COMMENT '品牌id',
    spec        VARCHAR(500)  NOT NULL DEFAULT '' COMMENT '规格（下单时快照）',
    sales       INT           NOT NULL DEFAULT 0 COMMENT '销量（冗余字段，下单后异步累加）',
    status      TINYINT       NOT NULL DEFAULT 1 COMMENT '状态：1上架 0下架',
    description VARCHAR(1000) NOT NULL DEFAULT '' COMMENT '商品描述（详情展示 / AI 导购 RAG 素材）',
    create_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_category_id (category_id),
    KEY idx_brand_id (brand_id),
    KEY idx_sales (sales),
    KEY idx_status (status)
) ENGINE = InnoDB COMMENT = '商品表';
