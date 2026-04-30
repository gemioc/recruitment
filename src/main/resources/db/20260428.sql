-- ============================================
-- 数据库结构变更脚本（ALTER）
-- ============================================
-- 使用说明：此文件用于存储后续新增功能带来的表结构变更
-- 对于已存在的数据库，需要手动执行此脚本进行升级

-- --------------------------------------------
-- Job表新增字段
-- --------------------------------------------

-- 工种类别
ALTER TABLE `t_job` ADD COLUMN `job_type` varchar(100) comment '工种类别';
-- 工作性质
ALTER TABLE `t_job` ADD COLUMN `work_nature` varchar(100) comment '工作性质';
-- 展位编号
ALTER TABLE `t_job` ADD COLUMN `number` varchar(100) comment '展位编号';
-- 工作地可以为空
ALTER TABLE `t_job` MODIFY `work_address` varchar(200) NULL;