-- 地方标准表重建脚本
-- 删除现有表并重新创建

-- 1. 删除现有表（按依赖关系顺序删除）
DROP TABLE IF EXISTS `local_standard_document`;
DROP TABLE IF EXISTS `local_standard_detail_info`;
DROP TABLE IF EXISTS `local_standard_detail`;
DROP TABLE IF EXISTS `local_standard_city_category`;

-- 2. 创建地方标准城市分类表
CREATE TABLE `local_standard_city_category` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `city_code` varchar(50) NOT NULL COMMENT '城市代码',
  `city_name` varchar(100) NOT NULL COMMENT '城市名称',
  `standard_count` int(11) DEFAULT 0 COMMENT '标准数量',
  `title` varchar(200) DEFAULT NULL COMMENT '完整标题，如北京市(2,374)',
  `data_trade` varchar(100) DEFAULT NULL COMMENT 'data-trade属性值',
  `create_by` varchar(50) DEFAULT 'system' COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT 'system' COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_city_code` (`city_code`),
  KEY `idx_city_name` (`city_name`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='地方标准城市分类表';

-- 3. 创建地方标准详情表
CREATE TABLE `local_standard_detail` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `pk` varchar(64) NOT NULL COMMENT '标准唯一标识',
  `code` varchar(100) DEFAULT NULL COMMENT '标准号',
  `ch_name` varchar(500) DEFAULT NULL COMMENT '标准名称',
  `city_code` varchar(50) DEFAULT NULL COMMENT '城市代码，如bjzjj',
  `city` varchar(100) DEFAULT NULL COMMENT '所属城市',
  `charge_dept` varchar(200) DEFAULT NULL COMMENT '负责部门',
  `status` varchar(50) DEFAULT NULL COMMENT '标准状态',
  `issue_date` bigint(20) DEFAULT NULL COMMENT '批准日期（时间戳）',
  `act_date` bigint(20) DEFAULT NULL COMMENT '实施日期（时间戳）',
  `record_date` bigint(20) DEFAULT NULL COMMENT '备案日期（时间戳）',
  `record_no` varchar(100) DEFAULT NULL COMMENT '备案号',
  `revise_std_codes` varchar(500) DEFAULT NULL COMMENT '修订标准号',
  `empty` tinyint(1) DEFAULT 0 COMMENT '是否为空',
  `other_result_columns` text COMMENT '其他结果列（JSON格式）',
  `fz_date` bigint(20) DEFAULT NULL COMMENT '废止日期（时间戳）',
  `city_category_id` bigint(20) DEFAULT NULL COMMENT '关联的城市分类ID',
  `create_by` varchar(50) DEFAULT 'system' COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT 'system' COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pk` (`pk`),
  KEY `idx_code` (`code`),
  KEY `idx_city` (`city`),
  KEY `idx_city_code` (`city_code`),
  KEY `idx_status` (`status`),
  KEY `idx_city_category_id` (`city_category_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='地方标准详情表';

-- 4. 创建地方标准详细信息表
CREATE TABLE `local_standard_detail_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `pk` varchar(64) NOT NULL COMMENT '标准唯一标识',
  `standard_code` varchar(100) DEFAULT NULL COMMENT '标准号',
  `publish_date` varchar(20) DEFAULT NULL COMMENT '发布日期',
  `implement_date` varchar(20) DEFAULT NULL COMMENT '实施日期',
  `abolish_status` varchar(10) DEFAULT NULL COMMENT '废止状态',
  `revision_type` varchar(20) DEFAULT NULL COMMENT '制修订类型',
  `replace_standard` varchar(200) DEFAULT NULL COMMENT '代替标准',
  `china_classification` varchar(20) DEFAULT NULL COMMENT '中国标准分类号',
  `international_classification` varchar(20) DEFAULT NULL COMMENT '国际标准分类号',
  `technical_committee` varchar(200) DEFAULT NULL COMMENT '技术归口',
  `approval_department` varchar(200) DEFAULT NULL COMMENT '批准发布部门',
  `industry_classification` varchar(100) DEFAULT NULL COMMENT '行业分类',
  `standard_category` varchar(50) DEFAULT NULL COMMENT '标准类别',
  `record_number` varchar(50) DEFAULT NULL COMMENT '备案号',
  `record_date` varchar(20) DEFAULT NULL COMMENT '备案日期',
  `record_bulletin` varchar(100) DEFAULT NULL COMMENT '备案月报',
  `scope` varchar(2000) DEFAULT NULL COMMENT '适用范围',
  `drafting_units` text COMMENT '起草单位',
  `drafting_persons` text COMMENT '起草人',
  `create_by` varchar(50) DEFAULT 'system' COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT 'system' COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pk` (`pk`),
  KEY `idx_standard_code` (`standard_code`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='地方标准详细信息表';

-- 5. 创建地方标准文档下载记录表
CREATE TABLE `local_standard_document` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `pk` varchar(64) NOT NULL COMMENT '标准PK',
  `standard_code` varchar(100) DEFAULT NULL COMMENT '标准号',
  `file_path` varchar(500) DEFAULT NULL COMMENT '文件路径',
  `file_size` bigint(20) DEFAULT NULL COMMENT '文件大小(字节)',
  `download_status` varchar(20) DEFAULT 'PENDING' COMMENT '下载状态：PENDING,SUCCESS,FAILED',
  `captcha_text` varchar(20) DEFAULT NULL COMMENT '识别的验证码',
  `download_token` varchar(100) DEFAULT NULL COMMENT '下载token',
  `retry_count` int(11) DEFAULT 0 COMMENT '重试次数',
  `error_message` text COMMENT '错误信息',
  `download_time` datetime DEFAULT NULL COMMENT '下载时间',
  `create_by` varchar(50) DEFAULT 'system' COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT 'system' COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pk` (`pk`),
  KEY `idx_download_status` (`download_status`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_update_time` (`update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='地方标准文档下载记录表';

-- 6. 显示创建结果
SHOW TABLES LIKE 'local_standard%';
