-- ============================================
-- 海报模板表结构优化
-- 执行日期: 2026-05-06
-- 说明: 将前端 JS 模板引擎的 templateId 落地到数据库
-- ============================================

-- 0. 先将现有 NOT NULL 的 template_path 改为允许 NULL
ALTER TABLE `t_poster_template`
MODIFY COLUMN `template_path` VARCHAR(500) DEFAULT NULL COMMENT '模板文件路径(已废弃,改用template_id)';

-- 1. 添加 template_id 字段
ALTER TABLE `t_poster_template`
ADD COLUMN `template_id` VARCHAR(100) DEFAULT NULL COMMENT '前端模板引擎注册ID' AFTER `template_path`;

-- 2. 从 template_path 提取并更新 template_id
-- 规则: /templates/template-admin.svg -> admin_01
--       /templates/template-business.svg -> tech_01
UPDATE `t_poster_template`
SET `template_id` = CASE
    WHEN `template_path` LIKE '%admin%' THEN 'admin_01'
    WHEN `template_path` LIKE '%business%' THEN 'tech_01'
    WHEN `template_path` LIKE '%tech_02%' THEN 'tech_02'
    ELSE NULL
END
WHERE `deleted` = 0;

-- 3. 插入新的多岗位招聘模板 (multi_01)
-- 使用 INSERT IGNORE 避免主键冲突
SET @new_id = (SELECT IFNULL(MAX(`id`), 0) + 1 FROM `t_poster_template`);
INSERT IGNORE INTO `t_poster_template`
(`id`, `template_name`, `template_path`, `template_id`, `color_scheme`, `preview_path`, `is_default`, `status`, `create_by`, `create_time`, `update_time`, `deleted`)
VALUES
(@new_id, '多岗招聘', NULL, 'multi_01', 'BLUE', NULL, 0, 1, NULL, NOW(), NOW(), 0);

-- 4. 验证结果
SELECT
    `id`,
    `template_name`,
    `template_path` AS '旧字段(废弃)',
    `template_id` AS '模板ID',
    `color_scheme` AS '配色',
    CASE WHEN `is_default` = 1 THEN '是' ELSE '否' END AS '默认',
    CASE WHEN `status` = 1 THEN '启用' ELSE '禁用' END AS '状态'
FROM `t_poster_template`
WHERE `deleted` = 0
ORDER BY `id`;
