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

package com.pig4cloud.pig.admin.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.pig.admin.dto.IndustryStandardDetailResponseDTO;
import com.pig4cloud.pig.admin.dto.IndustryStandardFilterOptionsDTO;
import com.pig4cloud.pig.admin.dto.IndustryStandardQueryDTO;
import com.pig4cloud.pig.admin.dto.IndustryStandardQueryResponseDTO;
import com.pig4cloud.pig.admin.entity.IndustryStandardDetail;
import com.pig4cloud.pig.admin.service.IndustryStandardQueryService;
import com.pig4cloud.pig.common.core.util.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 行业标准查询控制器
 *
 * @author pig4cloud
 */
@Slf4j
@RestController
@RequestMapping("/admin/industry-standard")
@RequiredArgsConstructor
@Tag(name = "行业标准查询", description = "行业标准查询相关接口")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class IndustryStandardQueryController {

	private final IndustryStandardQueryService industryStandardQueryService;

	/**
	 * 分页查询行业标准
	 * @param page 分页参数
	 * @param queryDTO 查询条件
	 * @return 分页查询结果
	 */
	@GetMapping("/query")
	@Operation(summary = "分页查询行业标准", description = "支持关键词、行业代码、部委、备案日期、状态等条件筛选")
	public R<IndustryStandardQueryResponseDTO> queryIndustryStandards(
			@ParameterObject Page<IndustryStandardDetail> page,
			@ParameterObject IndustryStandardQueryDTO queryDTO) {
		log.info("收到行业标准查询请求，分页参数：{}，查询条件：{}", page, queryDTO);
		try {
			IndustryStandardQueryResponseDTO response = industryStandardQueryService.queryIndustryStandards(page, queryDTO);
			return R.ok(response);
		} catch (Exception e) {
			log.error("查询行业标准失败", e);
			return R.failed("查询失败: " + e.getMessage());
		}
	}

	/**
	 * 获取筛选条件选项
	 * @return 筛选条件选项
	 */
	@GetMapping("/filter-options")
	@Operation(summary = "获取筛选条件选项", description = "获取行业代码列表、部委列表、状态选项、备案日期选项等筛选条件")
	public R<IndustryStandardFilterOptionsDTO> getFilterOptions() {
		log.info("收到获取筛选条件选项请求");
		try {
			IndustryStandardFilterOptionsDTO filterOptions = industryStandardQueryService.getFilterOptions();
			return R.ok(filterOptions);
		} catch (Exception e) {
			log.error("获取筛选条件选项失败", e);
			return R.failed("获取筛选条件选项失败: " + e.getMessage());
		}
	}

	/**
	 * 获取标准详情
	 * @param pk 标准唯一标识（可选）
	 * @param code 标准号（可选）
	 * @return 标准详情
	 */
	@GetMapping("/detail")
	@Operation(summary = "获取标准详情", description = "根据pk或code获取标准的详细信息")
	public R<IndustryStandardDetailResponseDTO> getStandardDetail(
			@RequestParam(required = false) String pk,
			@RequestParam(required = false) String code) {
		log.info("收到标准详情查询请求，pk: {}, code: {}", pk, code);
		try {
			// 参数校验
			if (StrUtil.isBlank(pk) && StrUtil.isBlank(code)) {
				return R.failed("pk和code至少提供一个");
			}

			IndustryStandardDetailResponseDTO detail = industryStandardQueryService.getStandardDetail(pk, code);
			if (detail == null) {
				return R.failed("标准不存在");
			}
			return R.ok(detail);
		} catch (Exception e) {
			log.error("查询标准详情失败", e);
			return R.failed("查询失败: " + e.getMessage());
		}
	}

}

