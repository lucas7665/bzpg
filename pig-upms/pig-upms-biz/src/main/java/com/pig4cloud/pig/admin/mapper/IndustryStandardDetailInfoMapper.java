/*
 * Copyright (c) 2020 pig4cloud Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pig4cloud.pig.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pig4cloud.pig.admin.entity.IndustryStandardDetailInfo;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 行业标准详细信息Mapper
 *
 * @author pig4cloud
 */
@Mapper
public interface IndustryStandardDetailInfoMapper extends BaseMapper<IndustryStandardDetailInfo> {

	/**
	 * 查询需要爬取详细信息的pk列表
	 * @param offset 偏移量
	 * @param limit 限制数量
	 * @return pk列表
	 */
	@Select("SELECT pk FROM industry_standard_detail d " +
			"WHERE NOT EXISTS (SELECT 1 FROM industry_standard_detail_info i WHERE i.pk = d.pk) " +
			"LIMIT #{limit} OFFSET #{offset}")
	List<String> selectPksNeedingDetailInfo(@Param("offset") int offset, @Param("limit") int limit);

	/**
	 * 查询需要爬取详细信息的记录总数
	 * @return 总数
	 */
	@Select("SELECT COUNT(*) FROM industry_standard_detail d " +
			"WHERE NOT EXISTS (SELECT 1 FROM industry_standard_detail_info i WHERE i.pk = d.pk)")
	long selectPksNeedingDetailInfoCount();

	/**
	 * 根据行业代码查询需要爬取详细信息的PK列表
	 * @param industryCode 行业代码
	 * @param offset 偏移量
	 * @param limit 限制数量
	 * @return pk列表
	 */
	@Select("SELECT d.pk FROM industry_standard_detail d " +
			"LEFT JOIN industry_standard_detail_info di ON d.pk = di.pk " +
			"WHERE di.pk IS NULL AND d.industry_code = #{industryCode} " +
			"ORDER BY d.create_time " +
			"LIMIT #{limit} OFFSET #{offset}")
	List<String> selectPksNeedingDetailInfoByIndustryCode(@Param("industryCode") String industryCode, 
	                                                      @Param("offset") int offset, 
	                                                      @Param("limit") int limit);

	/**
	 * 根据行业代码查询需要爬取详细信息的记录总数
	 * @param industryCode 行业代码
	 * @return 总数
	 */
	@Select("SELECT COUNT(*) FROM industry_standard_detail d " +
			"LEFT JOIN industry_standard_detail_info di ON d.pk = di.pk " +
			"WHERE di.pk IS NULL AND d.industry_code = #{industryCode}")
	long selectPksNeedingDetailInfoCountByIndustryCode(@Param("industryCode") String industryCode);

	/**
	 * 获取所有需要爬取详细信息的行业代码列表
	 * @return 行业代码列表
	 */
	@Select("SELECT DISTINCT d.industry_code FROM industry_standard_detail d " +
			"LEFT JOIN industry_standard_detail_info di ON d.pk = di.pk " +
			"WHERE di.pk IS NULL AND d.industry_code IS NOT NULL " +
			"ORDER BY d.industry_code")
	List<String> getIndustryCodesNeedingDetailInfo();

	/**
	 * 批量插入或更新标准详细信息（基于 pk 字段的唯一约束）
	 * 如果 pk 已存在则更新，不存在则插入
	 */
	@Insert({
		"<script>",
		"INSERT INTO industry_standard_detail_info ",
		"(pk, publish_date, implement_date, abolish_status, standard_code, revision_type, ",
		"replace_standard, china_classification, international_classification, technical_committee, ",
		"approval_department, industry_classification, standard_category, record_number, ",
		"record_date, record_bulletin, scope, drafting_units, drafting_persons, create_by, update_by, ",
		"create_time, update_time, remark) VALUES ",
		"<foreach collection='list' item='item' separator=','>",
		"(#{item.pk}, #{item.publishDate}, #{item.implementDate}, #{item.abolishStatus}, ",
		"#{item.standardCode}, #{item.revisionType}, #{item.replaceStandard}, #{item.chinaClassification}, ",
		"#{item.internationalClassification}, #{item.technicalCommittee}, #{item.approvalDepartment}, ",
		"#{item.industryClassification}, #{item.standardCategory}, #{item.recordNumber}, #{item.recordDate}, ",
		"#{item.recordBulletin}, #{item.scope}, #{item.draftingUnits}, #{item.draftingPersons}, ",
		"#{item.createBy}, #{item.updateBy}, #{item.createTime}, #{item.updateTime}, #{item.remark})",
		"</foreach>",
		"ON DUPLICATE KEY UPDATE ",
		"publish_date = VALUES(publish_date), ",
		"implement_date = VALUES(implement_date), ",
		"abolish_status = VALUES(abolish_status), ",
		"standard_code = VALUES(standard_code), ",
		"revision_type = VALUES(revision_type), ",
		"replace_standard = VALUES(replace_standard), ",
		"china_classification = VALUES(china_classification), ",
		"international_classification = VALUES(international_classification), ",
		"technical_committee = VALUES(technical_committee), ",
		"approval_department = VALUES(approval_department), ",
		"industry_classification = VALUES(industry_classification), ",
		"standard_category = VALUES(standard_category), ",
		"record_number = VALUES(record_number), ",
		"record_date = VALUES(record_date), ",
		"record_bulletin = VALUES(record_bulletin), ",
		"scope = VALUES(scope), ",
		"drafting_units = VALUES(drafting_units), ",
		"drafting_persons = VALUES(drafting_persons), ",
		"update_by = VALUES(update_by), ",
		"update_time = VALUES(update_time), ",
		"remark = VALUES(remark)",
		"</script>"
	})
	int insertOrUpdateBatch(@Param("list") List<IndustryStandardDetailInfo> list);

}
