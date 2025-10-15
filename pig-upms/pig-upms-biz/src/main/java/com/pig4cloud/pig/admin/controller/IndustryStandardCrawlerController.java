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

import com.pig4cloud.pig.admin.service.IndustryStandardCrawlerService;
import com.pig4cloud.pig.common.core.util.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 行业标准爬取控制器
 *
 * @author pig4cloud
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/industry-standard")
@Tag(description = "industry-standard", name = "行业标准爬取管理")
public class IndustryStandardCrawlerController {

	private final IndustryStandardCrawlerService crawlerService;

	@PostMapping("/crawl")
	@Operation(summary = "手动触发爬取任务", description = "手动触发行业标准数据爬取任务")
	public R<String> crawlIndustryStandards() {
		try {
			log.info("手动触发行业标准数据爬取任务");
			String result = crawlerService.crawlAllIndustryStandards();
			
			if ("0".equals(result)) {
				return R.ok("爬取任务执行成功");
			} else {
				return R.failed("爬取任务执行失败");
			}
		} catch (Exception e) {
			log.error("手动触发爬取任务失败", e);
			return R.failed("爬取任务执行异常: " + e.getMessage());
		}
	}

	@PostMapping("/crawl-categories")
	@Operation(summary = "爬取行业分类", description = "仅爬取行业分类数据")
	public R<String> crawlIndustryCategories() {
		try {
			log.info("开始爬取行业分类数据");
			var categories = crawlerService.crawlIndustryCategories();
			log.info("成功爬取 {} 个行业分类", categories.size());
			return R.ok("成功爬取 " + categories.size() + " 个行业分类");
		} catch (Exception e) {
			log.error("爬取行业分类失败", e);
			return R.failed("爬取行业分类失败: " + e.getMessage());
		}
	}

}
