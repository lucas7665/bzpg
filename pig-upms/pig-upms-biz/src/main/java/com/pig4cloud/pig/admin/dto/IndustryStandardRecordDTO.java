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
 * 行业标准记录DTO
 *
 * @author pig4cloud
 */
@Data
public class IndustryStandardRecordDTO {

	/**
	 * 唯一标识
	 */
	private String pk;

	/**
	 * 标准号
	 */
	private String code;

	/**
	 * 标准名称
	 */
	private String chName;

	/**
	 * 行业领域
	 */
	private String industry;

	/**
	 * 负责部门
	 */
	private String chargeDept;

	/**
	 * 状态（现行、废止）
	 */
	private String status;

	/**
	 * 批准日期（时间戳）
	 */
	private Long issueDate;

	/**
	 * 实施日期（时间戳）
	 */
	private Long actDate;

	/**
	 * 备案号
	 */
	private String recordNo;

	/**
	 * 备案日期（时间戳）
	 */
	private Long recordDate;

}

