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
import com.pig4cloud.pig.admin.mapper.IndustryStandardDetailMapper;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
	private final IndustryStandardDetailMapper detailMapper;
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

				// 第一步：数据去重（基于 pk）
				Map<String, IndustryStandardDetail> uniqueStandardsMap = new LinkedHashMap<>();
				for (IndustryStandardDetail standard : allStandards) {
					String pk = standard.getPk();
					if (pk != null && !pk.isEmpty()) {
						// 如果已存在，保留最新的（后爬取的覆盖先爬取的）
						uniqueStandardsMap.put(pk, standard);
					}
				}
				List<IndustryStandardDetail> uniqueStandards = new ArrayList<>(uniqueStandardsMap.values());
				log.info("去重后剩余 {} 条记录（去重前 {} 条）", uniqueStandards.size(), allStandards.size());

				if (uniqueStandards.isEmpty()) {
					log.info("去重后没有有效数据");
					return "0"; // 成功，但没有有效数据
				}

				// 获取所有行业分类用于关联
				List<IndustryCategory> categories = categoryService.list();
				Map<String, IndustryCategory> categoryMap = categories.stream()
					.collect(Collectors.toMap(IndustryCategory::getIndustryCode, Function.identity()));

				// 设置每条数据的行业分类关联
				for (IndustryStandardDetail standard : uniqueStandards) {
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

				// 第二步：查询数据库中已存在的记录，分离插入和更新
				log.info("开始保存标准数据，共 {} 条记录", uniqueStandards.size());

				// 收集所有 pk
				List<String> allPks = uniqueStandards.stream()
					.map(IndustryStandardDetail::getPk)
					.filter(pk -> pk != null && !pk.isEmpty())
					.collect(Collectors.toList());

				// 查询数据库中已存在的记录（获取 pk -> id 映射）
				Map<String, Long> existingPkToIdMap = new HashMap<>();
				if (!allPks.isEmpty()) {
					// 分批查询，避免 IN 子句过长
					int queryBatchSize = 1000;
					for (int i = 0; i < allPks.size(); i += queryBatchSize) {
						int end = Math.min(i + queryBatchSize, allPks.size());
						List<String> batchPks = allPks.subList(i, end);
						List<Map<String, Object>> mappings = detailMapper.getPkToIdMapping(batchPks);
						for (Map<String, Object> mapping : mappings) {
							String pk = (String) mapping.get("pk");
							Object idObj = mapping.get("id");
							if (pk != null && idObj != null) {
								Long id = idObj instanceof Long ? (Long) idObj : ((Number) idObj).longValue();
								existingPkToIdMap.put(pk, id);
							}
						}
					}
					log.info("查询到数据库中已存在 {} 条记录", existingPkToIdMap.size());
				}

				// 分离需要插入和更新的数据
				List<IndustryStandardDetail> toInsert = new ArrayList<>();
				List<IndustryStandardDetail> toUpdate = new ArrayList<>();

				// 当前时间，用于插入和更新操作
				LocalDateTime now = LocalDateTime.now();

				for (IndustryStandardDetail standard : uniqueStandards) {
					String pk = standard.getPk();
					if (pk != null && !pk.isEmpty()) {
						Long existingId = existingPkToIdMap.get(pk);
						if (existingId != null) {
							// 已存在，设置 id 用于更新
							standard.setId(existingId);
							// 手动设置更新时间和更新人，确保 update_time 字段会被更新
							standard.setUpdateTime(now);
							standard.setUpdateBy("sys_incremental_update");
							toUpdate.add(standard);
						} else {
							// 不存在，需要插入
							// 手动设置更新时间和更新人，确保插入时也有 update_time
							standard.setUpdateTime(now);
							standard.setUpdateBy("sys_incremental_insert");
							toInsert.add(standard);
						}
					}
				}

				log.info("需要插入 {} 条，更新 {} 条", toInsert.size(), toUpdate.size());

				// 分批插入新数据
				int totalInserted = 0;
				int totalUpdated = 0;
				int batchSize = 500;

				if (!toInsert.isEmpty()) {
					for (int i = 0; i < toInsert.size(); i += batchSize) {
						int end = Math.min(i + batchSize, toInsert.size());
						List<IndustryStandardDetail> batch = toInsert.subList(i, end);
						try {
							detailService.saveBatch(batch);
							totalInserted += batch.size();
							log.info("批次 {} 插入成功，共 {} 条", (i / batchSize + 1), batch.size());
						} catch (Exception e) {
							log.error("批次 {} 批量插入失败，降级为逐条插入", (i / batchSize + 1), e);
							int inserted = 0;
							for (IndustryStandardDetail standard : batch) {
								try {
									detailService.save(standard);
									inserted++;
								} catch (Exception ex) {
									log.error("插入标准 {} 失败", standard.getPk(), ex);
								}
							}
							totalInserted += inserted;
						}
					}
				}

				// 分批更新已存在的数据
				if (!toUpdate.isEmpty()) {
					for (int i = 0; i < toUpdate.size(); i += batchSize) {
						int end = Math.min(i + batchSize, toUpdate.size());
						List<IndustryStandardDetail> batch = toUpdate.subList(i, end);
						try {
							detailService.updateBatchById(batch);
							totalUpdated += batch.size();
							log.debug("批次 {} 更新成功，共 {} 条", (i / batchSize + 1), batch.size());
						} catch (Exception e) {
							log.error("批次 {} 批量更新失败，降级为逐条更新", (i / batchSize + 1), e);
							int updated = 0;
							for (IndustryStandardDetail standard : batch) {
								try {
									detailService.updateById(standard);
									updated++;
								} catch (Exception ex) {
									log.error("更新标准 {} 失败", standard.getPk(), ex);
								}
							}
							totalUpdated += updated;
						}
					}
				}

				log.info("标准数据保存完成：插入 {} 条，更新 {} 条，总计 {} 条",
					totalInserted, totalUpdated, totalInserted + totalUpdated);
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
