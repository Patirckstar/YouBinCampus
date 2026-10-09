-- ============================================================
-- 校园二手交易平台（YouBinCampus）数据库初始化脚本
-- 数据库：MySQL 8.x   字符集：utf8mb4
-- 说明：不建物理外键，关联关系通过索引 + 应用层保证
-- ============================================================

-- 1. 建库
CREATE DATABASE IF NOT EXISTS `youbin_campus` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE `youbin_campus`;

-- ============================================================
-- 2. 分类表 category
-- ============================================================
DROP TABLE IF EXISTS `category`;

CREATE TABLE `category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `name` VARCHAR(32) NOT NULL COMMENT '分类名称',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '排序值，越小越靠前',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '商品分类表';

-- ============================================================
-- 3. 用户表 user
-- ============================================================
DROP TABLE IF EXISTS `user`;

CREATE TABLE `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `openid` VARCHAR(64) NOT NULL COMMENT '微信 openid（登录标识）',
    `nickname` VARCHAR(64) DEFAULT NULL COMMENT '昵称',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像 URL',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_openid` (`openid`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户表';

-- ============================================================
-- 4. 商品表 goods
-- ============================================================
DROP TABLE IF EXISTS `goods`;

CREATE TABLE `goods` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '商品ID',
    `user_id` BIGINT NOT NULL COMMENT '发布者用户ID',
    `category_id` BIGINT NOT NULL COMMENT '分类ID',
    `title` VARCHAR(100) NOT NULL COMMENT '标题',
    `description` TEXT COMMENT '描述',
    `price` DECIMAL(10, 2) NOT NULL COMMENT '价格',
    `images` JSON COMMENT '图片 URL 数组，如 ["a.jpg","b.jpg"]',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0下架 1在售 2已售',
    `view_count` INT NOT NULL DEFAULT 0 COMMENT '浏览量',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_status` (`status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '商品表';

-- ============================================================
-- 5. 收藏表 favorite
-- ============================================================
DROP TABLE IF EXISTS `favorite`;

CREATE TABLE `favorite` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `goods_id` BIGINT NOT NULL COMMENT '商品ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_goods` (`user_id`, `goods_id`),
    KEY `idx_goods_id` (`goods_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '收藏表';

-- ============================================================
-- 6. 站内消息表 message（可选扩展：买家联系卖家）
-- ============================================================
DROP TABLE IF EXISTS `message`;

CREATE TABLE `message` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '消息ID',
    `from_user` BIGINT NOT NULL COMMENT '发送方用户ID',
    `to_user` BIGINT NOT NULL COMMENT '接收方用户ID',
    `goods_id` BIGINT DEFAULT NULL COMMENT '关联商品ID（可空）',
    `content` VARCHAR(500) NOT NULL COMMENT '消息内容',
    `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读：0未读 1已读',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_from_user` (`from_user`),
    KEY `idx_to_user` (`to_user`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '站内消息表';

-- ============================================================
-- 7. 初始数据：分类
--    注：分类名称与 Agent「智能发布」的分类映射保持一致
-- ============================================================
INSERT INTO
    `category` (`name`, `sort`)
VALUES ('教材书籍', 1),
    ('数码电子', 2),
    ('生活用品', 3),
    ('体育器材', 4),
    ('其他', 5);