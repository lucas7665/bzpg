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

package com.pig4cloud.pig.admin.task;

import com.pig4cloud.pig.admin.service.IndustryStandardDetailCrawlerService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 标准详细信息爬取定时任务
 *
 * @author pig4cloud
 */
@Slf4j
@Component("StandardDetailCrawler")
@RequiredArgsConstructor
public class StandardDetailCrawlerTask {

	private final IndustryStandardDetailCrawlerService detailCrawlerService;

	/**
	 * 爬取标准详细信息（无参数版本）
	 * @return 执行结果
	 */
	@SneakyThrows
	public String crawlStandardDetails() {
		log.info("开始执行标准详细信息爬取任务");
		
		try {
			String result = detailCrawlerService.crawlAllStandardDetails();
			
			if ("0".equals(result)) {
				log.info("标准详细信息爬取任务执行成功");
				return "0"; // 成功
			} else {
				log.error("标准详细信息爬取任务执行失败");
				return "1"; // 失败
			}
		} catch (Exception e) {
			log.error("标准详细信息爬取任务执行异常", e);
			return "1"; // 失败
		}
	}

	/**
	 * 爬取标准详细信息（带参数版本）
	 * @param params 参数（可为空）
	 * @return 执行结果
	 */
	@SneakyThrows
	public String crawlStandardDetails(String params) {
		log.info("开始执行标准详细信息爬取任务，参数：{}", params);
		
		try {
			String result = detailCrawlerService.crawlAllStandardDetails();
			
			if ("0".equals(result)) {
				log.info("标准详细信息爬取任务执行成功");
				return "0"; // 成功
			} else {
				log.error("标准详细信息爬取任务执行失败");
				return "1"; // 失败
			}
		} catch (Exception e) {
			log.error("标准详细信息爬取任务执行异常", e);
			return "1"; // 失败
		}
	}

}
