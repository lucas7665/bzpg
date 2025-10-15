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
 * 标准记录模型
 *
 * @author pig4cloud
 */
@Data
public class StandardRecord {

	/**
	 * 实施日期（时间戳）
	 */
	private Long actDate;

	/**
	 * 标准名称
	 */
	private String chName;

	/**
	 * 负责部门
	 */
	private String chargeDept;

	/**
	 * 标准号
	 */
	private String code;

	/**
	 * 是否为空
	 */
	private Boolean empty;

	/**
	 * 行业领域
	 */
	private String industry;

	/**
	 * 批准日期（时间戳）
	 */
	private Long issueDate;

	/**
	 * 其他结果列
	 */
	private Object otherResultColumns;

	/**
	 * 唯一标识
	 */
	private String pk;

	/**
	 * 备案日期（时间戳）
	 */
	private Long recordDate;

	/**
	 * 备案号
	 */
	private String recordNo;

	/**
	 * 修订标准号
	 */
	private String reviseStdCodes;

	/**
	 * 状态
	 */
	private String status;

	/**
	 * 废止日期（时间戳）
	 */
	private Long fzDate;

}
