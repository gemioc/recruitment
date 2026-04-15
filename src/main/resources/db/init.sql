-- 创建数据库
CREATE DATABASE IF NOT EXISTS `tv_recruitment`
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_general_ci;

USE `tv_recruitment`;

-- 1. 用户表
CREATE TABLE IF NOT EXISTS `t_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(100) NOT NULL COMMENT '密码(BCrypt加密)',
  `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `role` TINYINT NOT NULL DEFAULT 2 COMMENT '角色:1-管理员 2-运营人员',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态:1-启用 0-禁用',
  `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除:0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_phone` (`phone`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户表';

-- 2. 职位信息表
CREATE TABLE IF NOT EXISTS `t_job` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `job_name` VARCHAR(100) NOT NULL COMMENT '职位名称',
  `company` VARCHAR(100) DEFAULT NULL COMMENT '公司名称',
  `salary_min` DECIMAL(10,2) DEFAULT NULL COMMENT '薪资下限(元/月)',
  `salary_max` DECIMAL(10,2) DEFAULT NULL COMMENT '薪资上限(元/月)',
  `work_address` VARCHAR(200) NOT NULL COMMENT '工作地址',
  `education` VARCHAR(20) DEFAULT '不限' COMMENT '学历要求',
  `experience` VARCHAR(20) DEFAULT '不限' COMMENT '经验要求',
  `job_info` TEXT DEFAULT NULL COMMENT '职位信息',
  `recruit_count` INT DEFAULT 1 COMMENT '招聘人数',
  `welfare` VARCHAR(500) DEFAULT NULL COMMENT '公司福利',
  `contact_name` VARCHAR(50) DEFAULT NULL COMMENT '联系人',
  `contact_phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
  `contact_email` VARCHAR(100) DEFAULT NULL COMMENT '联系邮箱',
  `contact_wechat` VARCHAR(50) DEFAULT NULL COMMENT '联系微信',
  `deadline` DATE DEFAULT NULL COMMENT '截止招聘日期',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态:1-招聘中 0-已暂停',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除:0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_job_name` (`job_name`),
  KEY `idx_work_address` (`work_address`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='职位信息表';

-- 3. 设备分组表
CREATE TABLE IF NOT EXISTS `t_device_group` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `group_name` VARCHAR(100) NOT NULL COMMENT '分组名称',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '分组描述',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除:0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_group_name` (`group_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='设备分组表';

-- 4. 电视终端设备表
CREATE TABLE IF NOT EXISTS `t_device` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `device_code` VARCHAR(50) NOT NULL COMMENT '设备唯一编码',
  `device_name` VARCHAR(100) DEFAULT NULL COMMENT '设备名称',
  `location` VARCHAR(200) NOT NULL COMMENT '设备位置',
  `group_id` BIGINT DEFAULT NULL COMMENT '分组ID',
  `resolution` VARCHAR(20) DEFAULT '1920x1080' COMMENT '分辨率',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '使用状态:1-在用 2-闲置',
  `online_status` TINYINT NOT NULL DEFAULT 0 COMMENT '在线状态:0-离线 1-在线',
  `last_online_time` DATETIME DEFAULT NULL COMMENT '最后上线时间',
  `last_heartbeat` DATETIME DEFAULT NULL COMMENT '最后心跳时间',
  `total_online_duration` BIGINT DEFAULT 0 COMMENT '累计在线时长(秒)',
  `offline_count` INT DEFAULT 0 COMMENT '离线次数',
  `current_content_type` TINYINT DEFAULT NULL COMMENT '当前展示内容类型:1-海报 2-宣传片',
  `current_content_id` BIGINT DEFAULT NULL COMMENT '当前展示内容ID',
  `play_status` TINYINT DEFAULT NULL COMMENT '播放状态:1-播放中 2-暂停',
  `content_start_time` DATETIME DEFAULT NULL COMMENT '当前内容开始播放时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除:0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_device_code` (`device_code`),
  KEY `idx_group_id` (`group_id`),
  KEY `idx_online_status` (`online_status`),
  KEY `idx_last_online_time` (`last_online_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='电视终端设备表';

-- 5. 海报模板表
CREATE TABLE IF NOT EXISTS `t_poster_template` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `template_name` VARCHAR(100) NOT NULL COMMENT '模板名称',
  `template_path` VARCHAR(500) NOT NULL COMMENT '模板文件路径',
  `color_scheme` VARCHAR(50) DEFAULT NULL COMMENT '配色方案代码',
  `preview_path` VARCHAR(500) DEFAULT NULL COMMENT '预览图路径',
  `is_default` TINYINT NOT NULL DEFAULT 0 COMMENT '是否默认模板:0-否 1-是',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态:1-启用 2-禁用',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除:0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_is_default` (`is_default`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='海报模板表';

-- 6. 招聘海报表
CREATE TABLE IF NOT EXISTS `t_poster` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `poster_name` VARCHAR(100) NOT NULL COMMENT '海报名称',
  `job_id` BIGINT NOT NULL COMMENT '关联职位ID',
  `template_id` BIGINT DEFAULT NULL COMMENT '使用的模板ID',
  `file_path` VARCHAR(500) NOT NULL COMMENT '海报文件路径',
  `file_size` BIGINT DEFAULT NULL COMMENT '文件大小(字节)',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除:0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_job_id` (`job_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='招聘海报表';

-- 7. 宣传片表
CREATE TABLE IF NOT EXISTS `t_video` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `video_name` VARCHAR(100) NOT NULL COMMENT '视频名称',
  `file_path` VARCHAR(500) NOT NULL COMMENT '视频文件路径',
  `file_size` BIGINT DEFAULT NULL COMMENT '文件大小(字节)',
  `duration` INT DEFAULT NULL COMMENT '视频时长(秒)',
  `resolution` VARCHAR(20) DEFAULT NULL COMMENT '分辨率',
  `is_top` TINYINT NOT NULL DEFAULT 0 COMMENT '是否置顶:0-否 1-是',
  `create_by` BIGINT DEFAULT NULL COMMENT '上传人ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除:0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_is_top` (`is_top`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='宣传片表';

-- 8. 内容推送记录表
CREATE TABLE IF NOT EXISTS `t_push_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `content_type` TINYINT NOT NULL COMMENT '内容类型:1-海报 2-宣传片',
  `content_id` BIGINT NOT NULL COMMENT '内容ID',
  `content_name` VARCHAR(100) DEFAULT NULL COMMENT '内容名称',
  `push_type` TINYINT NOT NULL COMMENT '推送类型:1-单台 2-多台 3-分组',
  `target_ids` TEXT NOT NULL COMMENT '推送目标ID列表(JSON数组)',
  `device_count` INT DEFAULT 0 COMMENT '设备数量',
  `success_count` INT DEFAULT 0 COMMENT '成功数量',
  `fail_count` INT DEFAULT 0 COMMENT '失败数量',
  `group_id` BIGINT DEFAULT NULL COMMENT '分组ID',
  `push_status` TINYINT NOT NULL DEFAULT 0 COMMENT '推送状态:0-推送中 1-成功 2-失败',
  `fail_reason` VARCHAR(500) DEFAULT NULL COMMENT '失败原因',
  `play_rule` TEXT DEFAULT NULL COMMENT '播放规则(JSON)',
  `push_by` BIGINT DEFAULT NULL COMMENT '推送人ID',
  `push_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '推送时间',
  `complete_time` DATETIME DEFAULT NULL COMMENT '完成时间',
  PRIMARY KEY (`id`),
  KEY `idx_content` (`content_type`, `content_id`),
  KEY `idx_push_status` (`push_status`),
  KEY `idx_push_time` (`push_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='内容推送记录表';

-- 9. 操作日志表
CREATE TABLE IF NOT EXISTS `t_operation_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '操作用户ID',
  `user_name` VARCHAR(50) DEFAULT NULL COMMENT '用户名',
  `operation_type` VARCHAR(50) NOT NULL COMMENT '操作类型',
  `operation_desc` VARCHAR(500) DEFAULT NULL COMMENT '操作描述',
  `request_method` VARCHAR(10) DEFAULT NULL COMMENT '请求方法',
  `request_url` VARCHAR(500) DEFAULT NULL COMMENT '请求URL',
  `request_params` TEXT DEFAULT NULL COMMENT '请求参数',
  `response_result` TEXT DEFAULT NULL COMMENT '响应结果',
  `ip_address` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
  `operation_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `execution_time` BIGINT DEFAULT NULL COMMENT '执行时长(毫秒)',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_operation_type` (`operation_type`),
  KEY `idx_operation_time` (`operation_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='操作日志表';

-- 10. 系统配置表
CREATE TABLE IF NOT EXISTS `t_system_config` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `config_key` VARCHAR(50) NOT NULL COMMENT '配置键',
  `config_value` TEXT DEFAULT NULL COMMENT '配置值',
  `description` VARCHAR(200) DEFAULT NULL COMMENT '配置描述',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='系统配置表';

-- 图片管理表
CREATE TABLE IF NOT EXISTS `t_image` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `image_name` varchar(255) NOT NULL COMMENT '图片名称',
    `file_path` varchar(500) NOT NULL COMMENT '图片文件路径',
    `thumbnail_path` varchar(500) DEFAULT NULL COMMENT '缩略图路径',
    `file_size` bigint DEFAULT NULL COMMENT '文件大小（字节）',
    `width` int DEFAULT NULL COMMENT '图片宽度',
    `height` int DEFAULT NULL COMMENT '图片高度',
    `is_top` tinyint DEFAULT '0' COMMENT '是否置顶（0-否，1-是）',
    `create_by` bigint DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` bigint DEFAULT NULL COMMENT '更新人ID',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` tinyint DEFAULT '0' COMMENT '删除标志（0-未删除，1-已删除）',
    PRIMARY KEY (`id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='图片管理表';

-- 初始化数据

-- 默认管理员账号 (密码: admin123)
INSERT INTO `t_user` (`username`, `password`, `real_name`, `role`, `status`) VALUES
('admin', '$2a$10$naxiXSYlnicrMLDKYOmcmu1CR9IAHcbdVAQZh/yKhDVzxlTQh.Jr2', '系统管理员', 1, 1);

-- 海报模板 (只保留横版)
INSERT INTO tv_recruitment.t_poster_template (id, template_name, template_path, color_scheme, preview_path, is_default, status, create_by, create_time, update_time, deleted) VALUES (26, '蓝色商务横版', '/templates/template-business.svg', 'BLUE', '/templates/template-business.svg', 1, 1, null, '2026-03-26 00:00:42', '2026-03-26 00:50:07', 0);
INSERT INTO tv_recruitment.t_poster_template (id, template_name, template_path, color_scheme, preview_path, is_default, status, create_by, create_time, update_time, deleted) VALUES (33, '蓝白商务模板', '/templates/template-admin.svg', 'BLUE', '/templates/template-admin.svg', 0, 1, null, '2026-04-09 15:15:12', '2026-04-09 15:42:24', 0);

-- 系统配置
INSERT INTO `t_system_config` (`config_key`, `config_value`, `description`) VALUES
('company_name', '某某人力资源有限公司', '公司名称'),
('company_logo', '', '公司Logo路径'),
('company_address', '', '公司地址'),
('company_phone', '', '公司电话'),
('default_poster_duration', '10', '默认海报轮播时长(秒)'),
('default_video_loop', '0', '默认视频循环次数(0为无限循环)');

-- ============================================
-- 数据库升级脚本（针对已存在的数据库执行）
-- ============================================

-- 为t_device表添加设备状态统计相关字段
-- ALTER TABLE `t_device` ADD COLUMN `last_online_time` DATETIME DEFAULT NULL COMMENT '最后上线时间' AFTER `online_status`;
-- ALTER TABLE `t_device` ADD COLUMN `total_online_duration` BIGINT DEFAULT 0 COMMENT '累计在线时长(秒)' AFTER `last_heartbeat`;
-- ALTER TABLE `t_device` ADD COLUMN `offline_count` INT DEFAULT 0 COMMENT '离线次数' AFTER `total_online_duration`;
-- ALTER TABLE `t_device` ADD COLUMN `content_start_time` DATETIME DEFAULT NULL COMMENT '当前内容开始播放时间' AFTER `play_status`;
-- ALTER TABLE `t_device` ADD INDEX `idx_last_online_time` (`last_online_time`);

