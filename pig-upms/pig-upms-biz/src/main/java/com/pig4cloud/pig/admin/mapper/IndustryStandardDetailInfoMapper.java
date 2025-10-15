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

package com.pig4cloud.pig.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pig4cloud.pig.admin.entity.IndustryStandardDetailInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 行业标准详细信息Mapper
 *
 * @author pig4cloud
 */
@Mapper
public interface IndustryStandardDetailInfoMapper extends BaseMapper<IndustryStandardDetailInfo> {

	/**
	 * 查询需要爬取详细信息的pk列表
	 * @param offset 偏移量
	 * @param limit 限制数量
	 * @return pk列表
	 */
	@Select("SELECT pk FROM industry_standard_detail d " +
			"WHERE NOT EXISTS (SELECT 1 FROM industry_standard_detail_info i WHERE i.pk = d.pk) " +
			"LIMIT #{limit} OFFSET #{offset}")
	List<String> selectPksNeedingDetailInfo(@Param("offset") int offset, @Param("limit") int limit);

	/**
	 * 查询需要爬取详细信息的记录总数
	 * @return 总数
	 */
	@Select("SELECT COUNT(*) FROM industry_standard_detail d " +
			"WHERE NOT EXISTS (SELECT 1 FROM industry_standard_detail_info i WHERE i.pk = d.pk)")
	long selectPksNeedingDetailInfoCount();

}
