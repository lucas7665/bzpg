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

package com.pig4cloud.pig.admin.service;

import com.pig4cloud.pig.admin.entity.IndustryCategory;
import com.pig4cloud.pig.admin.entity.IndustryStandardDetail;

import java.util.List;

/**
 * 行业标准爬取服务
 *
 * @author pig4cloud
 */
public interface IndustryStandardCrawlerService {

	/**
	 * 爬取行业分类数据
	 * @return 行业分类列表
	 */
	List<IndustryCategory> crawlIndustryCategories();

	/**
	 * 根据行业名称爬取详细标准数据
	 * @param industryName 行业名称
	 * @return 标准详情列表
	 */
	List<IndustryStandardDetail> crawlStandardDetailsByIndustry(String industryName);

	/**
	 * 爬取所有行业标准数据
	 * @return 执行结果
	 */
	String crawlAllIndustryStandards();

}
