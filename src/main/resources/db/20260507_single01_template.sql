-- ============================================
-- 海报模板：单岗位模板 single_01 更新
-- 执行日期: 2026-05-07
-- 说明：删除原有单岗位模板(id=26蓝色商务横版, id=33蓝白商务模板)，
--       添加新的单岗位模板 single_01 (HTML+CSS渲染)
-- ============================================

SET @new_id = (SELECT IFNULL(MAX(`id`), 0) + 1 FROM `t_poster_template`);

-- 1. 删除原有单岗位模板（蓝色商务横版、蓝白商务模板）
DELETE FROM `t_poster_template` WHERE `id` IN (26, 33) AND `deleted` = 0;

-- 2. 添加新的单岗位模板 single_01
INSERT IGNORE INTO `t_poster_template`
(`id`, `template_name`, `template_path`, `template_id`, `color_scheme`, `preview_path`, `is_default`, `status`, `create_by`, `create_time`, `update_time`, `deleted`)
VALUES
(@new_id, '单岗位招聘', NULL, 'single_01', 'BLUE', NULL, 0, 1, NULL, NOW(), NOW(), 0);

-- 3. 验证结果
SELECT
    `id`,
    `template_name`,
    `template_id` AS '模板ID',
    `color_scheme` AS '配色',
    CASE WHEN `is_default` = 1 THEN '是' ELSE '否' END AS '默认',
    CASE WHEN `status` = 1 THEN '启用' ELSE '禁用' END AS '状态'
FROM `t_poster_template`
WHERE `deleted` = 0 AND `template_id` IN ('single_01', 'multi_01', 'multi_02')
ORDER BY `id`;
