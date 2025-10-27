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

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pig4cloud.pig.admin.entity.IndustryStandardDetail;
import com.pig4cloud.pig.admin.entity.IndustryStandardDetailInfo;
import com.pig4cloud.pig.admin.mapper.IndustryStandardDetailInfoMapper;
import com.pig4cloud.pig.admin.service.IndustryStandardDetailInfoService;
import com.pig4cloud.pig.admin.service.IndustryStandardDetailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 行业标准详细信息Service实现
 *
 * @author pig4cloud
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IndustryStandardDetailInfoServiceImpl extends ServiceImpl<IndustryStandardDetailInfoMapper, IndustryStandardDetailInfo> implements IndustryStandardDetailInfoService {

	private final IndustryStandardDetailService detailService;

	@Override
	public List<String> getPksNeedingDetailInfo() {
		// 使用自定义SQL查询需要爬取详细信息的pk，避免内存问题
		return baseMapper.selectPksNeedingDetailInfo(0, Integer.MAX_VALUE);
	}

	/**
	 * 分页获取需要爬取详细信息的pk列表
	 * @param offset 偏移量
	 * @param limit 限制数量
	 * @return pk列表
	 */
	public List<String> getPksNeedingDetailInfo(int offset, int limit) {
		return baseMapper.selectPksNeedingDetailInfo(offset, limit);
	}

	/**
	 * 获取需要爬取详细信息的记录总数
	 * @return 总数
	 */
	public long getPksNeedingDetailInfoCount() {
		return baseMapper.selectPksNeedingDetailInfoCount();
	}

	@Override
	public int saveBatchDetailInfo(List<IndustryStandardDetailInfo> detailInfos) {
		if (detailInfos == null || detailInfos.isEmpty()) {
			return 0;
		}

		try {
			// 使用自定义的 UPSERT 方法，基于 pk 字段的唯一约束
			// 如果 pk 已存在则更新，不存在则插入
			int affectedRows = baseMapper.insertOrUpdateBatch(detailInfos);
			
			// UPSERT 返回值说明：
			// - 插入新记录：返回 1
			// - 更新已存在记录：返回 2
			// 我们需要返回实际处理的记录数，不是受影响的行数
			int actualProcessed = detailInfos.size();
			
			log.info("批量保存完成，实际处理 {} 条记录，受影响行数 {} 条", actualProcessed, affectedRows);
			return actualProcessed;
		} catch (Exception e) {
			log.error("批量保存标准详细信息失败", e);
			return 0;
		}
	}

	@Override
	public List<String> getPksNeedingDetailInfoByIndustryCode(String industryCode, int offset, int limit) {
		return baseMapper.selectPksNeedingDetailInfoByIndustryCode(industryCode, offset, limit);
	}

	@Override
	public long getPksNeedingDetailInfoCountByIndustryCode(String industryCode) {
		return baseMapper.selectPksNeedingDetailInfoCountByIndustryCode(industryCode);
	}

	@Override
	public List<String> getIndustryCodesNeedingDetailInfo() {
		return baseMapper.getIndustryCodesNeedingDetailInfo();
	}

}
