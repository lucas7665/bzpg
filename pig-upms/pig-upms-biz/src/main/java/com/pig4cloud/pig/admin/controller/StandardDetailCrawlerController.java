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

import com.pig4cloud.pig.admin.service.IndustryStandardDetailCrawlerService;
import com.pig4cloud.pig.common.core.util.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 标准详细信息爬取控制器
 *
 * @author pig4cloud
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/standard-detail")
@Tag(description = "standard-detail", name = "标准详细信息爬取管理")
public class StandardDetailCrawlerController {

	private final IndustryStandardDetailCrawlerService detailCrawlerService;

	@PostMapping("/crawl")
	@Operation(summary = "手动触发详细信息爬取任务", description = "手动触发标准详细信息爬取任务")
	public R<String> crawlStandardDetails() {
		try {
			log.info("手动触发标准详细信息爬取任务");
			String result = detailCrawlerService.crawlAllStandardDetails();
			
			if ("0".equals(result)) {
				return R.ok("详细信息爬取任务执行成功");
			} else {
				return R.failed("详细信息爬取任务执行失败");
			}
		} catch (Exception e) {
			log.error("手动触发详细信息爬取任务失败", e);
			return R.failed("详细信息爬取任务执行异常: " + e.getMessage());
		}
	}

}
