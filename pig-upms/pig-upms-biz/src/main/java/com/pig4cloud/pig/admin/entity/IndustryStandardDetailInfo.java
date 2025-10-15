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

package com.pig4cloud.pig.admin.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 行业标准详细信息表
 *
 * @author pig4cloud
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("industry_standard_detail_info")
@Schema(description = "行业标准详细信息表")
public class IndustryStandardDetailInfo extends Model<IndustryStandardDetailInfo> {

	private static final long serialVersionUID = 1L;

	@TableId(type = IdType.AUTO)
	@Schema(description = "主键ID")
	private Long id;

	@Schema(description = "标准唯一标识，关联industry_standard_detail.pk")
	private String pk;

	// 标准状态
	@Schema(description = "发布日期")
	private String publishDate;

	@Schema(description = "实施日期")
	private String implementDate;

	@Schema(description = "废止状态")
	private String abolishStatus;

	// 基础信息
	@Schema(description = "标准号")
	private String standardCode;

	@Schema(description = "制修订类型")
	private String revisionType;

	@Schema(description = "代替标准")
	private String replaceStandard;

	@Schema(description = "中国标准分类号")
	private String chinaClassification;

	@Schema(description = "国际标准分类号")
	private String internationalClassification;

	@Schema(description = "技术归口")
	private String technicalCommittee;

	@Schema(description = "批准发布部门")
	private String approvalDepartment;

	@Schema(description = "行业分类")
	private String industryClassification;

	@Schema(description = "标准类别")
	private String standardCategory;

	// 备案信息
	@Schema(description = "备案号")
	private String recordNumber;

	@Schema(description = "备案日期")
	private String recordDate;

	@Schema(description = "备案月报")
	private String recordBulletin;

	// 适用范围
	@Schema(description = "适用范围")
	private String scope;

	// 起草信息
	@Schema(description = "起草单位")
	private String draftingUnits;

	@Schema(description = "起草人")
	private String draftingPersons;

	// 系统字段
	@TableField(fill = FieldFill.INSERT)
	@Schema(description = "创建者")
	private String createBy;

	@TableField(fill = FieldFill.INSERT)
	@Schema(description = "创建时间")
	private LocalDateTime createTime;

	@TableField(fill = FieldFill.UPDATE)
	@Schema(description = "更新者")
	private String updateBy;

	@TableField(fill = FieldFill.UPDATE)
	@Schema(description = "更新时间")
	private LocalDateTime updateTime;

	@Schema(description = "备注信息")
	private String remark;

}
