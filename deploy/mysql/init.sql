-- 生产库初始化：只建库和业务账号，表结构由 JPA 首次启动自动创建
-- 建议首次用 ddl-auto=update，稳定后改为 validate，后续变更走 migrations
-- 执行：mysql -uroot -p < deploy/mysql/init.sql

CREATE DATABASE IF NOT EXISTS qingguoji_mall
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

-- 业务账号（把 change-me 换成强口令）
CREATE USER IF NOT EXISTS 'qingguoji'@'%' IDENTIFIED BY 'change-me';
GRANT ALL PRIVILEGES ON qingguoji_mall.* TO 'qingguoji'@'%';
FLUSH PRIVILEGES;

-- 校验
-- SHOW CREATE DATABASE qingguoji_mall;
