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

import com.pig4cloud.pig.admin.entity.IndustryStandardDetailInfo;

import java.util.List;

/**
 * 行业标准详细信息爬取服务
 *
 * @author pig4cloud
 */
public interface IndustryStandardDetailCrawlerService {

	/**
	 * 爬取所有标准的详细信息
	 * @return 执行结果
	 */
	String crawlAllStandardDetails();

	/**
	 * 爬取指定pk的详细信息
	 * @param pk 标准唯一标识
	 * @return 详细信息
	 */
	IndustryStandardDetailInfo crawlStandardDetail(String pk);

	/**
	 * 批量爬取详细信息
	 * @param pks pk列表
	 * @return 爬取成功数量
	 */
	int crawlBatchStandardDetails(List<String> pks);

}
