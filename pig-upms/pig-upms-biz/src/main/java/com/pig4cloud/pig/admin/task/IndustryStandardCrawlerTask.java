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

import com.pig4cloud.pig.admin.service.IndustryStandardCrawlerService;
// import com.pig4cloud.pig.daemon.quartz.constants.PigQuartzEnum;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 行业标准爬取定时任务
 *
 * @author pig4cloud
 */
@Slf4j
@Component("IndustryStandardCrawler")
@RequiredArgsConstructor
public class IndustryStandardCrawlerTask {

	private final IndustryStandardCrawlerService crawlerService;

	/**
	 * 爬取行业标准数据（无参数版本）
	 * @return 执行结果
	 */
	@SneakyThrows
	public String crawlIndustryStandards() {
		log.info("开始执行行业标准数据爬取任务");
		
		try {
			String result = crawlerService.crawlAllIndustryStandards();
			
			if ("0".equals(result)) {
				log.info("行业标准数据爬取任务执行成功");
				return "0"; // 成功
			} else {
				log.error("行业标准数据爬取任务执行失败");
				return "1"; // 失败
			}
		} catch (Exception e) {
			log.error("行业标准数据爬取任务执行异常", e);
			return "1"; // 失败
		}
	}

	/**
	 * 爬取行业标准数据（带参数版本）
	 * @param params 参数（可为空）
	 * @return 执行结果
	 */
	@SneakyThrows
	public String crawlIndustryStandards(String params) {
		log.info("开始执行行业标准数据爬取任务，参数：{}", params);
		
		try {
			String result = crawlerService.crawlAllIndustryStandards();
			
			if ("0".equals(result)) {
				log.info("行业标准数据爬取任务执行成功");
				return "0"; // 成功
			} else {
				log.error("行业标准数据爬取任务执行失败");
				return "1"; // 失败
			}
		} catch (Exception e) {
			log.error("行业标准数据爬取任务执行异常", e);
			return "1"; // 失败
		}
	}

}
