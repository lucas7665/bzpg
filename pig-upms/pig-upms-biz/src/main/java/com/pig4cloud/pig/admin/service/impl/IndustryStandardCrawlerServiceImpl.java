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

package com.pig4cloud.pig.admin.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pig4cloud.pig.admin.dto.StandardQueryResponse;
import com.pig4cloud.pig.admin.dto.StandardRecord;
import com.pig4cloud.pig.admin.entity.IndustryCategory;
import com.pig4cloud.pig.admin.entity.IndustryStandardDetail;
import com.pig4cloud.pig.admin.service.IndustryCategoryService;
import com.pig4cloud.pig.admin.service.IndustryStandardCrawlerService;
import com.pig4cloud.pig.admin.service.IndustryStandardDetailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 行业标准爬取服务实现类
 *
 * @author pig4cloud
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IndustryStandardCrawlerServiceImpl implements IndustryStandardCrawlerService {

	private final IndustryCategoryService categoryService;
	private final IndustryStandardDetailService detailService;
	private final ObjectMapper objectMapper = new ObjectMapper();

	private static final String TARGET_URL = "https://hbba.sacinfo.org.cn/stdList";
	private static final String QUERY_URL = "https://hbba.sacinfo.org.cn/stdQueryList";

	@Override
	public List<IndustryCategory> crawlIndustryCategories() {
		List<IndustryCategory> categories = new ArrayList<>();
		
		try {
			log.info("开始爬取行业分类数据...");
			
			Document doc = Jsoup.connect(TARGET_URL)
				.userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
				.timeout(10000)
				.get();
				
			Elements tradeDivs = doc.select("#codes-area .trade-div");
			log.info("找到 {} 个行业分类", tradeDivs.size());
			
			for (Element div : tradeDivs) {
				try {
					String industryCode = div.select(".trade-no").text();
					String industryName = div.attr("data-trade");
					String title = div.attr("title");
					
					// 提取数量
					Pattern pattern = Pattern.compile("\\((\\d+)\\)");
					Matcher matcher = pattern.matcher(div.text());
					int count = matcher.find() ? Integer.parseInt(matcher.group(1)) : 0;
					
					IndustryCategory category = IndustryCategory.builder()
						.industryCode(industryCode)
						.industryName(industryName)
						.standardCount(count)
						.title(title)
						.dataTrade(industryName)
						.createBy("system")
						.updateBy("system")
						.build();
					
					categories.add(category);
					
				} catch (Exception e) {
					log.warn("解析行业分类数据失败: {}", div.text(), e);
				}
			}
			
			log.info("成功解析 {} 个行业分类", categories.size());
			
		} catch (Exception e) {
			log.error("爬取行业分类数据失败", e);
			throw new RuntimeException("爬取行业分类数据失败", e);
		}
		
		return categories;
	}

	@Override
	public List<IndustryStandardDetail> crawlStandardDetailsByIndustry(String industryName) {
		List<IndustryStandardDetail> allStandards = new ArrayList<>();
		
		try {
			log.info("开始爬取行业 {} 的标准数据...", industryName);
			
			// 先获取第一页，了解分页信息
			StandardQueryResponse firstPageResponse = getStandardDetailsByPage(industryName, 1, 100);
			
			if (firstPageResponse == null || firstPageResponse.getRecords() == null) {
				log.warn("行业 {} 没有标准数据", industryName);
				return allStandards;
			}
			
			// 添加第一页数据
			allStandards.addAll(parseStandardDetails(firstPageResponse.getRecords()));
			
			int totalPages = firstPageResponse.getPages();
			int totalRecords = firstPageResponse.getTotal();
			
			log.info("行业 {} 共有 {} 页数据，总计 {} 条记录", industryName, totalPages, totalRecords);
			
			// 如果有多页，继续爬取剩余页面
			if (totalPages > 1) {
				for (int currentPage = 2; currentPage <= totalPages; currentPage++) {
					try {
						StandardQueryResponse pageResponse = getStandardDetailsByPage(industryName, currentPage, 100);
						if (pageResponse != null && pageResponse.getRecords() != null) {
							allStandards.addAll(parseStandardDetails(pageResponse.getRecords()));
							log.info("已爬取行业 {} 第 {}/{} 页数据", industryName, currentPage, totalPages);
						}
						
						// 添加延迟，避免请求过于频繁
						Thread.sleep(500);
					} catch (Exception e) {
						log.error("爬取行业 {} 第 {} 页数据失败", industryName, currentPage, e);
						// 单页失败不影响其他页面
					}
				}
			}
			
			log.info("行业 {} 爬取完成，共获取 {} 条记录", industryName, allStandards.size());
			
		} catch (Exception e) {
			log.error("爬取行业 {} 的标准详情失败", industryName, e);
		}
		
		return allStandards;
	}

	@Override
	public String crawlAllIndustryStandards() {
		try {
			log.info("开始爬取行业标准数据...");
			
			// 第一阶段：爬取行业分类数据
			List<IndustryCategory> categories = crawlIndustryCategories();
			if (categories.isEmpty()) {
				log.warn("未获取到任何行业分类数据");
				return "1";
			}
			
			categoryService.saveOrUpdateBatch(categories);
			log.info("第一阶段完成：获取到 {} 个行业分类", categories.size());
			
			// 第二阶段：遍历每个行业，爬取详细标准数据
			int totalStandards = 0;
			for (IndustryCategory category : categories) {
				try {
					log.info("开始爬取行业 {} 的标准数据...", category.getIndustryName());
					
					List<IndustryStandardDetail> standards = crawlStandardDetailsByIndustry(category.getIndustryName());
					
					if (!standards.isEmpty()) {
						// 设置行业分类关联
						standards.forEach(standard -> 
							standard.setIndustryCategoryId(category.getId())
						);
						
						detailService.saveOrUpdateBatch(standards);
						totalStandards += standards.size();
						
						log.info("行业 {} 爬取完成，获取 {} 条标准数据", 
							category.getIndustryName(), standards.size());
					} else {
						log.warn("行业 {} 没有获取到标准数据", category.getIndustryName());
					}
					
					// 行业间添加延迟，避免请求过于频繁
					Thread.sleep(2000);
					
				} catch (Exception e) {
					log.error("爬取行业 {} 的标准数据失败", category.getIndustryName(), e);
					// 单个行业失败不影响其他行业
				}
			}
			
			log.info("爬取任务完成：共处理 {} 个行业，获取 {} 条标准数据", 
				categories.size(), totalStandards);
			
			return "0"; // 成功
		} catch (Exception e) {
			log.error("爬取行业标准数据失败", e);
			return "1"; // 失败
		}
	}

	/**
	 * 获取指定页的标准数据
	 */
	private StandardQueryResponse getStandardDetailsByPage(String industryName, int currentPage, int pageSize) {
		try {
			String postData = buildPostData(industryName, currentPage, pageSize);
			
			Document doc = Jsoup.connect(QUERY_URL)
				.userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
				.header("Content-Type", "application/x-www-form-urlencoded")
				.requestBody(postData)
				.timeout(30000)
				.post();
				
			String jsonResponse = doc.text();
			return objectMapper.readValue(jsonResponse, StandardQueryResponse.class);
			
		} catch (Exception e) {
			log.error("获取行业 {} 第 {} 页数据失败", industryName, currentPage, e);
			return null;
		}
	}

	/**
	 * 构建POST请求参数（支持分页）
	 */
	private String buildPostData(String industryName, int currentPage, int pageSize) {
		return String.format(
			"current=%d&size=%d&industry=%s",
			currentPage, pageSize, industryName
		);
	}

	/**
	 * 解析标准记录为实体对象
	 */
	private List<IndustryStandardDetail> parseStandardDetails(List<StandardRecord> records) {
		List<IndustryStandardDetail> standards = new ArrayList<>();
		
		for (StandardRecord record : records) {
			try {
				IndustryStandardDetail standard = IndustryStandardDetail.builder()
					.pk(record.getPk())
					.code(record.getCode())
					.chName(record.getChName())
					.industry(record.getIndustry())
					.chargeDept(record.getChargeDept())
					.status(record.getStatus())
					.issueDate(record.getIssueDate())
					.actDate(record.getActDate())
					.recordDate(record.getRecordDate())
					.recordNo(record.getRecordNo())
					.reviseStdCodes(record.getReviseStdCodes())
					.empty(record.getEmpty())
					.fzDate(record.getFzDate())
					.createBy("system")
					.updateBy("system")
					.build();
				
				// 将otherResultColumns转换为JSON字符串存储
				if (record.getOtherResultColumns() != null) {
					try {
						standard.setOtherResultColumns(objectMapper.writeValueAsString(record.getOtherResultColumns()));
					} catch (Exception e) {
						log.warn("转换otherResultColumns失败", e);
						standard.setOtherResultColumns("{}");
					}
				}
				
				standards.add(standard);
			} catch (Exception e) {
				log.warn("解析标准记录失败: {}", record, e);
			}
		}
		
		return standards;
	}

}
