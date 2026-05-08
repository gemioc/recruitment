-- ============================================
-- 海报模板：新增多岗招聘模板2 (multi_02)
-- 执行日期: 2026-05-07
-- ============================================

SET @new_id = (SELECT IFNULL(MAX(`id`), 0) + 1 FROM `t_poster_template`);
INSERT IGNORE INTO `t_poster_template`
(`id`, `template_name`, `template_path`, `template_id`, `color_scheme`, `preview_path`, `is_default`, `status`, `create_by`, `create_time`, `update_time`, `deleted`)
VALUES
(@new_id, '多岗招聘2', NULL, 'multi_02', 'BLUE', NULL, 0, 1, NULL, NOW(), NOW(), 0);

-- 验证结果
SELECT
    `id`,
    `template_name`,
    `template_id` AS '模板ID',
    `color_scheme` AS '配色',
    CASE WHEN `is_default` = 1 THEN '是' ELSE '否' END AS '默认',
    CASE WHEN `status` = 1 THEN '启用' ELSE '禁用' END AS '状态'
FROM `t_poster_template`
WHERE `deleted` = 0 AND `template_id` IN ('multi_01', 'multi_02')
ORDER BY `id`;