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

package com.pig4cloud.pig.admin.dto;

import lombok.Data;

/**
 * 行业标准查询DTO
 *
 * @author pig4cloud
 */
@Data
public class IndustryStandardQueryDTO {

	/**
	 * 关键词（标准号或标准名称，模糊查询）
	 */
	private String keyword;

	/**
	 * 行业代码（如AQ、BB、CB等，单选）
	 */
	private String industryCode;

	/**
	 * 部委（负责部门，单选）
	 */
	private String chargeDept;

	/**
	 * 备案日期类型
	 * LAST_MONTH: 近一月
	 * LAST_THREE_MONTHS: 近三月
	 * LAST_HALF_YEAR: 近半年
	 * LAST_YEAR: 近一年
	 * LAST_TWO_YEARS: 近两年
	 * LAST_THREE_YEARS: 近三年
	 */
	private String recordDateType;

	/**
	 * 标准状态
	 * 现行、废止
	 */
	private String status;

}

