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
 * 行业分类表
 *
 * @author pig4cloud
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "行业分类")
@EqualsAndHashCode(callSuper = false)
@TableName("industry_category")
public class IndustryCategory extends Model<IndustryCategory> {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 主键ID
	 */
	@TableId(value = "id", type = IdType.ASSIGN_ID)
	private Long id;

	/**
	 * 行业代码，如AQ、BB等
	 */
	@Schema(description = "行业代码")
	private String industryCode;

	/**
	 * 行业名称，如安全生产、包装等
	 */
	@Schema(description = "行业名称")
	private String industryName;

	/**
	 * 该行业下的标准数量
	 */
	@Schema(description = "标准数量")
	private Integer standardCount;

	/**
	 * 完整标题，如AQ 安全生产(387)
	 */
	@Schema(description = "完整标题")
	private String title;

	/**
	 * data-trade属性值
	 */
	@Schema(description = "data-trade属性值")
	private String dataTrade;

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
	 * 备注信息
	 */
	@Schema(description = "备注信息")
	private String remark;

}
