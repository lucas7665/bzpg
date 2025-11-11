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

import java.util.List;

/**
 * 筛选条件选项DTO
 *
 * @author pig4cloud
 */
@Data
public class FilterOptionsDTO {

	/**
	 * 省份列表（带数量统计）
	 */
	private List<ProvinceOptionDTO> provinces;

	/**
	 * 标准状态选项
	 */
	private List<StatusOptionDTO> statusOptions;

	/**
	 * 备案日期选项
	 */
	private List<RecordDateOptionDTO> recordDateOptions;

	/**
	 * 省份选项DTO
	 */
	@Data
	public static class ProvinceOptionDTO {
		/**
		 * 城市代码
		 */
		private String cityCode;

		/**
		 * 城市名称（如：北京市）
		 */
		private String cityName;

		/**
		 * 该省份下的标准数量
		 */
		private Integer standardCount;
	}

	/**
	 * 状态选项DTO
	 */
	@Data
	public static class StatusOptionDTO {
		/**
		 * 状态值
		 */
		private String value;

		/**
		 * 状态标签
		 */
		private String label;
	}

	/**
	 * 备案日期选项DTO
	 */
	@Data
	public static class RecordDateOptionDTO {
		/**
		 * 日期类型值
		 */
		private String value;

		/**
		 * 日期类型标签
		 */
		private String label;
	}

}

