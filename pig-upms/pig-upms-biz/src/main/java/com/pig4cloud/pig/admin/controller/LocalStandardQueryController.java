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

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.pig.admin.dto.FilterOptionsDTO;
import com.pig4cloud.pig.admin.dto.LocalStandardQueryDTO;
import com.pig4cloud.pig.admin.dto.LocalStandardQueryResponseDTO;
import com.pig4cloud.pig.admin.entity.LocalStandardDetail;
import com.pig4cloud.pig.admin.service.LocalStandardQueryService;
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
import org.springframework.web.bind.annotation.RestController;

/**
 * 地方标准查询控制器
 *
 * @author pig4cloud
 */
@Slf4j
@RestController
@RequestMapping("/admin/local-standard")
@RequiredArgsConstructor
@Tag(name = "地方标准查询", description = "地方标准查询相关接口")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class LocalStandardQueryController {

	private final LocalStandardQueryService localStandardQueryService;

	/**
	 * 分页查询地方标准
	 * @param page 分页参数
	 * @param queryDTO 查询条件
	 * @return 分页查询结果
	 */
	@GetMapping("/query")
	@Operation(summary = "分页查询地方标准", description = "支持关键词、省市区、备案日期、状态等条件筛选")
	public R<LocalStandardQueryResponseDTO> queryLocalStandards(
			@ParameterObject Page<LocalStandardDetail> page,
			@ParameterObject LocalStandardQueryDTO queryDTO) {
		log.info("收到地方标准查询请求，分页参数：{}，查询条件：{}", page, queryDTO);
		try {
			LocalStandardQueryResponseDTO response = localStandardQueryService.queryLocalStandards(page, queryDTO);
			return R.ok(response);
		} catch (Exception e) {
			log.error("查询地方标准失败", e);
			return R.failed("查询失败: " + e.getMessage());
		}
	}

	/**
	 * 获取筛选条件选项
	 * @return 筛选条件选项
	 */
	@GetMapping("/filter-options")
	@Operation(summary = "获取筛选条件选项", description = "获取省份列表、状态选项、备案日期选项等筛选条件")
	public R<FilterOptionsDTO> getFilterOptions() {
		log.info("收到获取筛选条件选项请求");
		try {
			FilterOptionsDTO filterOptions = localStandardQueryService.getFilterOptions();
			return R.ok(filterOptions);
		} catch (Exception e) {
			log.error("获取筛选条件选项失败", e);
			return R.failed("获取筛选条件选项失败: " + e.getMessage());
		}
	}

}

