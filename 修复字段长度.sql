-- ========================================
-- 修复 local_standard_detail_info 表字段长度
-- 解决 Data too long 错误
-- ========================================

USE pig;

-- 1. 扩展分类号字段长度（可能包含多个分类号）
ALTER TABLE `local_standard_detail_info` 
  MODIFY COLUMN `china_classification` VARCHAR(500) DEFAULT NULL COMMENT '中国标准分类号（可能包含多个，用分号分隔）';

ALTER TABLE `local_standard_detail_info` 
  MODIFY COLUMN `international_classification` VARCHAR(500) DEFAULT NULL COMMENT '国际标准分类号（可能包含多个，用分号分隔）';

-- 2. 扩展其他可能较长的字段（预防性）
ALTER TABLE `local_standard_detail_info` 
  MODIFY COLUMN `technical_committee` VARCHAR(500) DEFAULT NULL COMMENT '技术归口';

ALTER TABLE `local_standard_detail_info` 
  MODIFY COLUMN `approval_department` VARCHAR(500) DEFAULT NULL COMMENT '批准发布部门';

ALTER TABLE `local_standard_detail_info` 
  MODIFY COLUMN `replace_standard` VARCHAR(500) DEFAULT NULL COMMENT '代替标准（可能包含多个）';

ALTER TABLE `local_standard_detail_info` 
  MODIFY COLUMN `record_number` VARCHAR(100) DEFAULT NULL COMMENT '备案号';

ALTER TABLE `local_standard_detail_info` 
  MODIFY COLUMN `record_bulletin` VARCHAR(200) DEFAULT NULL COMMENT '备案月报';

-- 3. 验证修改结果
SELECT 
  COLUMN_NAME, 
  COLUMN_TYPE, 
  CHARACTER_MAXIMUM_LENGTH,
  COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'pig' 
  AND TABLE_NAME = 'local_standard_detail_info'
  AND COLUMN_NAME IN (
    'china_classification', 
    'international_classification',
    'technical_committee',
    'approval_department',
    'replace_standard',
    'record_number',
    'record_bulletin'
  )
ORDER BY ORDINAL_POSITION;

-- ========================================
-- 执行完成后，继续运行爬虫任务即可
-- ========================================

