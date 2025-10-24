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

import com.baomidou.mybatisplus.extension.service.IService;
import com.pig4cloud.pig.admin.entity.IndustryStandardDetailInfo;

import java.util.List;

/**
 * 行业标准详细信息Service
 *
 * @author pig4cloud
 */
public interface IndustryStandardDetailInfoService extends IService<IndustryStandardDetailInfo> {

	/**
	 * 获取需要爬取详细信息的pk列表
	 * @return pk列表
	 */
	List<String> getPksNeedingDetailInfo();

	/**
	 * 分页获取需要爬取详细信息的pk列表
	 * @param offset 偏移量
	 * @param limit 限制数量
	 * @return pk列表
	 */
	List<String> getPksNeedingDetailInfo(int offset, int limit);

	/**
	 * 获取需要爬取详细信息的记录总数
	 * @return 总数
	 */
	long getPksNeedingDetailInfoCount();

	/**
	 * 批量保存详细信息
	 * @param detailInfos 详细信息列表
	 * @return 保存成功数量
	 */
	int saveBatchDetailInfo(List<IndustryStandardDetailInfo> detailInfos);

	/**
	 * 根据行业代码分页获取需要爬取详细信息的pk列表
	 * @param industryCode 行业代码
	 * @param offset 偏移量
	 * @param limit 限制数量
	 * @return pk列表
	 */
	List<String> getPksNeedingDetailInfoByIndustryCode(String industryCode, int offset, int limit);

	/**
	 * 根据行业代码获取需要爬取详细信息的记录总数
	 * @param industryCode 行业代码
	 * @return 总数
	 */
	long getPksNeedingDetailInfoCountByIndustryCode(String industryCode);

	/**
	 * 获取所有需要爬取详细信息的行业代码列表
	 * @return 行业代码列表
	 */
	List<String> getIndustryCodesNeedingDetailInfo();

}
