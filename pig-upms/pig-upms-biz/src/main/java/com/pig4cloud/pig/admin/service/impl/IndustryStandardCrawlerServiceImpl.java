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
import com.pig4cloud.pig.admin.service.IndustryStandardDetailCrawlerService;
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
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
	private final IndustryStandardDetailCrawlerService detailCrawlerService;
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
		return crawlStandardDetailsByIndustry(industryName, false);
	}

	/**
	 * 根据行业名称爬取详细标准数据（支持增量爬取）
	 * @param industryName 行业名称
	 * @param incremental 是否为增量爬取
	 * @return 标准详情列表
	 */
	public List<IndustryStandardDetail> crawlStandardDetailsByIndustry(String industryName, boolean incremental) {
		List<IndustryStandardDetail> allStandards = new ArrayList<>();

		try {
			log.info("开始爬取行业 {} 的标准数据{}...",
				industryName, incremental ? "（增量，最近一个月）" : "（全量）");

			// 先获取第一页，了解分页信息
			StandardQueryResponse firstPageResponse = getStandardDetailsByPage(industryName, 1, 100, incremental);

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
						StandardQueryResponse pageResponse = getStandardDetailsByPage(industryName, currentPage, 100, incremental);
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

					List<IndustryStandardDetail> standards = crawlStandardDetailsByIndustry(category.getIndustryName(), false);

					if (!standards.isEmpty()) {
						// 设置行业分类关联（同时设置 industry_code 和 industry_category_id）
						standards.forEach(standard -> {
							standard.setIndustryCategoryId(category.getId());  // 保留原有关联
							standard.setIndustryCode(category.getIndustryCode()); // 新增：设置行业代码
						});

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

	@Override
	public String crawlIncrementalIndustryStandards() {
		try {
			log.info("开始增量爬取行业标准数据（最近一个月）...");

			// pubdate=-1 获取所有行业最近一个月的数据，不需要按行业遍历
			log.info("开始爬取所有行业最近一个月的标准数据...");

			List<IndustryStandardDetail> allStandards = new ArrayList<>();

			// 先获取第一页，了解分页信息
			StandardQueryResponse firstPageResponse = getStandardDetailsByPage("", 1, 100, true);

			if (firstPageResponse == null || firstPageResponse.getRecords() == null) {
				log.warn("最近一个月没有标准数据");
				return "1";
			}

			// 添加第一页数据
			allStandards.addAll(parseStandardDetails(firstPageResponse.getRecords()));

			int totalPages = firstPageResponse.getPages();
			int totalRecords = firstPageResponse.getTotal();

			log.info("最近一个月共有 {} 页数据，总计 {} 条记录", totalPages, totalRecords);

			// 如果有多页，继续爬取剩余页面
			if (totalPages > 1) {
				for (int currentPage = 2; currentPage <= totalPages; currentPage++) {
					try {
						StandardQueryResponse pageResponse = getStandardDetailsByPage("", currentPage, 100, true);
						if (pageResponse != null && pageResponse.getRecords() != null) {
							allStandards.addAll(parseStandardDetails(pageResponse.getRecords()));
							log.info("已爬取第 {}/{} 页数据", currentPage, totalPages);
						}

						// 添加延迟，避免请求过于频繁
						Thread.sleep(500);
					} catch (Exception e) {
						log.error("爬取第 {} 页数据失败", currentPage, e);
						// 单页失败不影响其他页面
					}
				}
			}

			// 处理爬取到的数据：设置行业分类关联
			if (!allStandards.isEmpty()) {
				log.info("开始处理 {} 条数据的行业分类关联...", allStandards.size());

				// 获取所有行业分类用于关联
				List<IndustryCategory> categories = categoryService.list();
				Map<String, IndustryCategory> categoryMap = categories.stream()
					.collect(Collectors.toMap(IndustryCategory::getIndustryCode, Function.identity()));

				// 设置每条数据的行业分类关联
				for (IndustryStandardDetail standard : allStandards) {
					try {
						String industryCode = extractIndustryCode(standard);

						// 根据行业代码查找对应的分类
						IndustryCategory category = categoryMap.get(industryCode);
						if (category != null) {
							standard.setIndustryCategoryId(category.getId());
							standard.setIndustryCode(category.getIndustryCode());
						} else {
							log.warn("未找到行业代码 {} 对应的分类", industryCode);
						}

						standard.setCreateBy("system");
						standard.setUpdateBy("system");

					} catch (Exception e) {
						log.warn("处理标准 {} 的行业分类失败", standard.getPk(), e);
					}
				}

				// 批量保存（MyBatis-Plus 会自动判断插入或更新）
				detailService.saveOrUpdateBatch(allStandards);
				log.info("标准数据保存完成，共处理 {} 条记录", allStandards.size());
			} else {
				log.info("最近一个月没有新数据");
			}

			// 第二阶段：爬取这批数据的详细信息（增量更新 detail_info）
			if (!allStandards.isEmpty()) {
				log.info("开始爬取增量数据的详细信息...");

				// 收集所有pk
				List<String> pks = allStandards.stream()
					.map(IndustryStandardDetail::getPk)
					.filter(pk -> pk != null && !pk.isEmpty())
					.collect(Collectors.toList());

				log.info("需要爬取详细信息的标准数量：{}", pks.size());

				// 批量爬取详细信息（每次处理100条）
				int batchSize = 100;
				int totalProcessed = 0;

				for (int i = 0; i < pks.size(); i += batchSize) {
					int end = Math.min(i + batchSize, pks.size());
					List<String> batch = pks.subList(i, end);

					try {
						// 爬取这批详细数据
						int processed = detailCrawlerService.crawlBatchStandardDetails(batch);
						totalProcessed += processed;

						log.info("已处理详细信息 {}/{} 条记录", totalProcessed, pks.size());

						// 批次间延迟
						if (end < pks.size()) {
							Thread.sleep(100);
						}

					} catch (Exception e) {
						log.error("处理详细信息批次失败", e);
					}
				}

				log.info("详细信息爬取完成，共处理 {} 条记录", totalProcessed);
			}

			log.info("增量爬取任务完成：共处理 {} 条标准数据", allStandards.size());

			return "0"; // 成功
		} catch (Exception e) {
			log.error("增量爬取行业标准数据失败", e);
			return "1"; // 失败
		}
	}

	/**
	 * 从标准数据中提取行业代码
	 */
	private String extractIndustryCode(IndustryStandardDetail standard) {
		// 优先使用 industry_code 字段（如果已设置）
		if (standard.getIndustryCode() != null && !standard.getIndustryCode().isEmpty()) {
			return standard.getIndustryCode();
		}

		// 如果 industry_code 为空，尝试从 industry 字段匹配
		// industry 字段存储的是行业名称，需要通过名称匹配到行业代码
		if (standard.getIndustry() != null && !standard.getIndustry().isEmpty()) {
			// 通过行业名称匹配查找行业代码
			List<IndustryCategory> categories = categoryService.list();
			for (IndustryCategory category : categories) {
				if (category.getIndustryName() != null &&
				    category.getIndustryName().equals(standard.getIndustry())) {
					return category.getIndustryCode();
				}
			}
		}

		return null;
	}

	/**
	 * 获取指定页的标准数据（支持增量爬取）
	 */
	private StandardQueryResponse getStandardDetailsByPage(String industryName, int currentPage, int pageSize) {
		return getStandardDetailsByPage(industryName, currentPage, pageSize, false);
	}

	/**
	 * 获取指定页的标准数据（支持增量爬取）
	 */
	private StandardQueryResponse getStandardDetailsByPage(String industryName, int currentPage, int pageSize, boolean incremental) {
		try {
			String postData = buildPostData(industryName, currentPage, pageSize, incremental);

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
	 * 构建POST请求参数（支持增量爬取）
	 */
	private String buildPostData(String industryName, int currentPage, int pageSize) {
		return buildPostData(industryName, currentPage, pageSize, false);
	}

	/**
	 * 构建POST请求参数（支持增量爬取）
	 */
	private String buildPostData(String industryName, int currentPage, int pageSize, boolean incremental) {
		if (incremental) {
			// 增量爬取：添加 pubdate=-1 参数（最近一个月）
			return String.format(
				"current=%d&size=%d&industry=%s&pubdate=-1",
				currentPage, pageSize, industryName
			);
		} else {
			// 全量爬取：原有逻辑
			return String.format(
				"current=%d&size=%d&industry=%s",
				currentPage, pageSize, industryName
			);
		}
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
