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
 * 地方标准详情响应DTO
 *
 * @author pig4cloud
 */
@Data
public class LocalStandardDetailResponseDTO {

	/**
	 * 标准唯一标识
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
	 * 所属城市
	 */
	private String city;

	/**
	 * 标准状态（现行、废止）
	 */
	private String status;

	/**
	 * 标准状态信息
	 */
	private StandardStatusDTO standardStatus;

	/**
	 * 基础信息
	 */
	private BasicInfoDTO basicInfo;

	/**
	 * 备案信息
	 */
	private RecordInfoDTO recordInfo;

	/**
	 * 适用范围
	 */
	private String scope;

	/**
	 * 起草单位列表
	 */
	private List<String> draftingUnits;

	/**
	 * 起草人列表
	 */
	private List<String> draftingPersons;

	/**
	 * 负责部门
	 */
	private String chargeDept;

	/**
	 * 修订标准号
	 */
	private String reviseStdCodes;

	/**
	 * 标准状态DTO
	 */
	@Data
	public static class StandardStatusDTO {
		/**
		 * 发布日期（格式：yyyy-MM-dd）
		 */
		private String issueDate;

		/**
		 * 实施日期（格式：yyyy-MM-dd）
		 */
		private String implementDate;

		/**
		 * 废止日期（格式：yyyy-MM-dd，可能为null）
		 */
		private String abolishDate;
	}

	/**
	 * 基础信息DTO
	 */
	@Data
	public static class BasicInfoDTO {
		/**
		 * 标准号
		 */
		private String code;

		/**
		 * 中国标准分类号
		 */
		private String chinaClassification;

		/**
		 * 国际标准分类号
		 */
		private String internationalClassification;

		/**
		 * 发布日期（格式：yyyy-MM-dd）
		 */
		private String publishDate;

		/**
		 * 实施日期（格式：yyyy-MM-dd）
		 */
		private String implementDate;

		/**
		 * 技术归口
		 */
		private String technicalCommittee;

		/**
		 * 制修订类型（制定、修订）
		 */
		private String revisionType;

		/**
		 * 批准发布部门
		 */
		private String approvalDepartment;

		/**
		 * 代替标准
		 */
		private String replaceStandard;

		/**
		 * 提出部门
		 */
		private String proposingDepartment;

		/**
		 * 行业分类
		 */
		private String industryClassification;

		/**
		 * 标准类别
		 */
		private String standardCategory;
	}

	/**
	 * 备案信息DTO
	 */
	@Data
	public static class RecordInfoDTO {
		/**
		 * 备案号
		 */
		private String recordNumber;

		/**
		 * 备案日期（格式：yyyy-MM-dd）
		 */
		private String recordDate;

		/**
		 * 备案月报
		 */
		private String recordBulletin;
	}

}

