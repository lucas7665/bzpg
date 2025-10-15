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

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 行业标准详情表
 *
 * @author pig4cloud
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "行业标准详情")
@EqualsAndHashCode(callSuper = false)
@TableName("industry_standard_detail")
public class IndustryStandardDetail extends Model<IndustryStandardDetail> {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 主键ID
	 */
	@TableId(value = "id", type = IdType.ASSIGN_ID)
	private Long id;

	/**
	 * 标准唯一标识
	 */
	@Schema(description = "标准唯一标识")
	private String pk;

	/**
	 * 标准号
	 */
	@Schema(description = "标准号")
	private String code;

	/**
	 * 标准名称
	 */
	@Schema(description = "标准名称")
	private String chName;

	/**
	 * 行业领域
	 */
	@Schema(description = "行业领域")
	private String industry;

	/**
	 * 负责部门
	 */
	@Schema(description = "负责部门")
	private String chargeDept;

	/**
	 * 标准状态
	 */
	@Schema(description = "标准状态")
	private String status;

	/**
	 * 批准日期（时间戳）
	 */
	@Schema(description = "批准日期")
	private Long issueDate;

	/**
	 * 实施日期（时间戳）
	 */
	@Schema(description = "实施日期")
	private Long actDate;

	/**
	 * 备案日期（时间戳）
	 */
	@Schema(description = "备案日期")
	private Long recordDate;

	/**
	 * 备案号
	 */
	@Schema(description = "备案号")
	private String recordNo;

	/**
	 * 修订标准号
	 */
	@Schema(description = "修订标准号")
	private String reviseStdCodes;

	/**
	 * 是否为空
	 */
	@TableField("`empty`")
	@Schema(description = "是否为空")
	private Boolean empty;

	/**
	 * 其他结果列（JSON格式）
	 */
	@Schema(description = "其他结果列")
	private String otherResultColumns;

	/**
	 * 关联的行业分类ID
	 */
	@Schema(description = "行业分类ID")
	private Long industryCategoryId;

	/**
	 * 创建者
	 */
	@TableField(fill = FieldFill.INSERT)
	@Schema(description = "创建者")
	private String createBy;

	/**
	 * 创建时间
	 */
	@TableField(fill = FieldFill.INSERT)
	@Schema(description = "创建时间")
	private LocalDateTime createTime;

	/**
	 * 更新者
	 */
	@TableField(fill = FieldFill.UPDATE)
	@Schema(description = "更新者")
	private String updateBy;

	/**
	 * 更新时间
	 */
	@TableField(fill = FieldFill.UPDATE)
	@Schema(description = "更新时间")
	private LocalDateTime updateTime;

	/**
	 * 废止日期（时间戳）
	 */
	@Schema(description = "废止日期")
	private Long fzDate;

	/**
	 * 备注信息
	 */
	@Schema(description = "备注信息")
	private String remark;

}
