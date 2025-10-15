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

import com.pig4cloud.pig.admin.entity.IndustryStandardDetailInfo;
import com.pig4cloud.pig.admin.service.IndustryStandardDetailCrawlerService;
import com.pig4cloud.pig.admin.service.IndustryStandardDetailInfoService;
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
 * 行业标准详细信息爬取服务实现
 *
 * @author pig4cloud
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IndustryStandardDetailCrawlerServiceImpl implements IndustryStandardDetailCrawlerService {

	private static final String DETAIL_BASE_URL = "https://hbba.sacinfo.org.cn/stdDetail/";
	private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";
	private static final int TIMEOUT = 30000; // 30 seconds

	private final IndustryStandardDetailInfoService detailInfoService;

	@Override
	public String crawlAllStandardDetails() {
		try {
			log.info("开始爬取所有标准的详细信息");

			// 获取总记录数
			long totalCount = detailInfoService.getPksNeedingDetailInfoCount();
			log.info("共需要爬取 {} 条标准详细信息", totalCount);

			if (totalCount == 0) {
				log.info("没有需要爬取的标准详细信息");
				return "0";
			}

			// 分页处理，避免内存问题
			int totalProcessed = 0;
			int pageSize = 1000; // 每页1000条，减少数据库查询次数
			int currentPage = 0;

			while (totalProcessed < totalCount) {
				// 分页获取pk列表
				List<String> pks = detailInfoService.getPksNeedingDetailInfo(
					currentPage * pageSize, pageSize);

				if (pks.isEmpty()) {
					break;
				}

				// 分批处理当前页的数据
				int batchSize = 10; // 每批处理100条
				for (int i = 0; i < pks.size(); i += batchSize) {
					int end = Math.min(i + batchSize, pks.size());
					List<String> batch = pks.subList(i, end);

					int batchProcessed = crawlBatchStandardDetails(batch);
					totalProcessed += batchProcessed;

					log.info("已处理 {}/{} 条记录，当前批次处理 {} 条",
						totalProcessed, totalCount, batchProcessed);

					// 批次间延迟，避免请求过于频繁
					if (i + batchSize < pks.size()) {
						Thread.sleep(2000);
					}
				}

				currentPage++;

				// 页间延迟
				if (totalProcessed < totalCount) {
					Thread.sleep(2000);
				}
			}

			log.info("标准详细信息爬取完成，共处理 {} 条记录", totalProcessed);
			return "0"; // 成功

		} catch (Exception e) {
			log.error("爬取标准详细信息失败", e);
			return "1"; // 失败
		}
	}

	@Override
	public IndustryStandardDetailInfo crawlStandardDetail(String pk) {
		try {
			String detailUrl = DETAIL_BASE_URL + pk;
			log.debug("爬取标准详细信息: {}", detailUrl);

			Document doc = Jsoup.connect(detailUrl)
				.userAgent(USER_AGENT)
				.timeout(TIMEOUT)
				.get();

			return parseDetailPage(doc, pk);

		} catch (Exception e) {
			log.error("爬取标准 {} 详细信息失败", pk, e);
			return null;
		}
	}

	@Override
	public int crawlBatchStandardDetails(List<String> pks) {
		List<IndustryStandardDetailInfo> detailInfos = new ArrayList<>();

		for (String pk : pks) {
			try {
				IndustryStandardDetailInfo detailInfo = crawlStandardDetail(pk);
				if (detailInfo != null) {
					detailInfos.add(detailInfo);
				}

				// 单条记录间添加延迟
				Thread.sleep(1000);

			} catch (Exception e) {
				log.error("处理标准 {} 详细信息失败", pk, e);
				// 单条失败不影响其他记录
			}
		}

		// 批量保存
		if (!detailInfos.isEmpty()) {
			int saved = detailInfoService.saveBatchDetailInfo(detailInfos);
			log.info("批次保存完成，成功保存 {} 条记录", saved);
			return saved;
		}

		return 0;
	}

	/**
	 * 解析详细页面内容
	 */
	private IndustryStandardDetailInfo parseDetailPage(Document doc, String pk) {
		IndustryStandardDetailInfo info = IndustryStandardDetailInfo.builder()
			.pk(pk)
			.createBy("system")
			.updateBy("system")
			.build();

		try {
			log.debug("开始解析标准 {} 的详细信息", pk);

			// 解析标准状态
			parseStandardStatus(doc, info);
			log.debug("标准状态解析完成: 发布日期={}, 实施日期={}, 废止状态={}",
				info.getPublishDate(), info.getImplementDate(), info.getAbolishStatus());

			// 解析基础信息
			parseBasicInfo(doc, info);
			log.debug("基础信息解析完成: 标准号={}, 制修订={}, 代替标准={}",
				info.getStandardCode(), info.getRevisionType(), info.getReplaceStandard());

			// 解析备案信息
			parseRecordInfo(doc, info);
			log.debug("备案信息解析完成: 备案号={}, 备案日期={}, 备案月报={}",
				info.getRecordNumber(), info.getRecordDate(), info.getRecordBulletin());

			// 解析适用范围
			parseScope(doc, info);
			log.debug("适用范围解析完成: {}", info.getScope());

			// 解析起草信息
			parseDraftingInfo(doc, info);
			log.debug("起草信息解析完成: 起草单位={}, 起草人={}",
				info.getDraftingUnits(), info.getDraftingPersons());

		} catch (Exception e) {
			log.error("解析标准 {} 详细信息失败", pk, e);
		}

		return info;
	}

	/**
	 * 解析标准状态
	 */
	private void parseStandardStatus(Document doc, IndustryStandardDetailInfo info) {
		// 解析时间轴中的状态信息
		Elements timelineElements = doc.select(".timeline .events li a");
		for (Element element : timelineElements) {
			String text = element.text();
			if (text.contains("发布")) {
				info.setPublishDate(extractDate(text));
			} else if (text.contains("实施")) {
				info.setImplementDate(extractDate(text));
			} else if (text.contains("废止")) {
				info.setAbolishStatus("已废止");
			}
		}
	}

	/**
	 * 解析基础信息
	 */
	private void parseBasicInfo(Document doc, IndustryStandardDetailInfo info) {
		// 解析左侧基础信息
		Elements leftInfoElements = doc.select(".basicInfo-left dt, .basicInfo-left dd");
		parseBasicInfoBlock(leftInfoElements, info);

		// 解析右侧基础信息
		Elements rightInfoElements = doc.select(".basicInfo-right dt, .basicInfo-right dd");
		parseBasicInfoBlock(rightInfoElements, info);
	}

	/**
	 * 解析基础信息块
	 */
	private void parseBasicInfoBlock(Elements elements, IndustryStandardDetailInfo info) {
		for (int i = 0; i < elements.size(); i += 2) {
			if (i + 1 < elements.size()) {
				Element labelElement = elements.get(i);
				Element valueElement = elements.get(i + 1);

				if (labelElement.hasClass("name") && valueElement.hasClass("value")) {
					String label = labelElement.text().trim();
					String value = valueElement.text().trim();

					switch (label) {
						case "标准号":
							info.setStandardCode(value);
							break;
						case "发布日期":
							info.setPublishDate(value);
							break;
						case "实施日期":
							info.setImplementDate(value);
							break;
						case "制修订":
							info.setRevisionType(value);
							break;
						case "代替标准":
							info.setReplaceStandard(value);
							break;
						case "中国标准分类号":
							info.setChinaClassification(value);
							break;
						case "国际标准分类号":
							info.setInternationalClassification(value);
							break;
						case "技术归口":
							info.setTechnicalCommittee(value);
							break;
						case "批准发布部门":
							info.setApprovalDepartment(value);
							break;
						case "行业分类":
							info.setIndustryClassification(value);
							break;
						case "标准类别":
							info.setStandardCategory(value);
							break;
					}
				}
			}
		}
	}

	/**
	 * 解析备案信息
	 */
	private void parseRecordInfo(Document doc, IndustryStandardDetailInfo info) {
		// 查找备案信息段落
		Element recordSection = doc.select("div.para-title:contains(备案信息)").first();
		if (recordSection != null) {
			// 备案信息段落后面直接跟着p标签，不是div
			Element current = recordSection.nextElementSibling();
			while (current != null) {
				if (current.tagName().equals("p")) {
					String text = current.text().trim();
					log.debug("解析备案信息段落: {}", text);
					
					if (text.startsWith("备案号：")) {
						info.setRecordNumber(text.substring(4));
						log.debug("提取备案号: {}", info.getRecordNumber());
					} else if (text.startsWith("备案日期：")) {
						info.setRecordDate(text.substring(5));
						log.debug("提取备案日期: {}", info.getRecordDate());
					} else if (text.startsWith("备案月报：")) {
						// 提取链接文本
						Element link = current.select("a").first();
						if (link != null) {
							info.setRecordBulletin(link.text().trim());
							log.debug("提取备案月报(链接): {}", info.getRecordBulletin());
						} else {
							info.setRecordBulletin(text.substring(5));
							log.debug("提取备案月报(文本): {}", info.getRecordBulletin());
						}
					}
				} else if (current.hasClass("para-title")) {
					// 遇到下一个段落，停止解析
					break;
				}
				current = current.nextElementSibling();
			}
		} else {
			log.debug("未找到备案信息段落");
		}
	}

	/**
	 * 解析适用范围
	 */
	private void parseScope(Document doc, IndustryStandardDetailInfo info) {
		// 查找适用范围段落
		Element scopeSection = doc.select("div.para-title:contains(适用范围)").first();
		if (scopeSection != null) {
			Element current = scopeSection.nextElementSibling();
			while (current != null) {
				if (current.tagName().equals("p")) {
					String scopeText = current.text().trim();
					info.setScope(scopeText);
					log.debug("提取适用范围: {}", scopeText);
					break;
				} else if (current.hasClass("para-title")) {
					break;
				}
				current = current.nextElementSibling();
			}
		} else {
			log.debug("未找到适用范围段落");
		}
	}

	/**
	 * 解析起草信息
	 */
	private void parseDraftingInfo(Document doc, IndustryStandardDetailInfo info) {
		// 解析起草单位
		Element unitsSection = doc.select("div.para-title:contains(起草单位)").first();
		if (unitsSection != null) {
			Element current = unitsSection.nextElementSibling();
			while (current != null) {
				if (current.tagName().equals("p")) {
					String unitsText = current.text().trim();
					if (!unitsText.isEmpty() && !unitsText.equals(" ")) {
						info.setDraftingUnits(unitsText);
						log.debug("提取起草单位: {}", unitsText);
					}
					break;
				} else if (current.hasClass("para-title")) {
					break;
				}
				current = current.nextElementSibling();
			}
		} else {
			log.debug("未找到起草单位段落");
		}

		// 解析起草人
		Element personsSection = doc.select("div.para-title:contains(起草人)").first();
		if (personsSection != null) {
			Element current = personsSection.nextElementSibling();
			while (current != null) {
				if (current.tagName().equals("p")) {
					String personsText = current.text().trim();
					if (!personsText.isEmpty() && !personsText.equals(" ")) {
						info.setDraftingPersons(personsText);
						log.debug("提取起草人: {}", personsText);
					}
					break;
				} else if (current.hasClass("para-title")) {
					break;
				}
				current = current.nextElementSibling();
			}
		} else {
			log.debug("未找到起草人段落");
		}
	}

	/**
	 * 提取日期信息
	 */
	private String extractDate(String text) {
		Pattern pattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
		Matcher matcher = pattern.matcher(text);
		if (matcher.find()) {
			return matcher.group();
		}
		return text;
	}

}
