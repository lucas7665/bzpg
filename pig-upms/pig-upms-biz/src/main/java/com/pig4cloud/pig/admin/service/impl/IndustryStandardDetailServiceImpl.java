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

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pig4cloud.pig.admin.entity.IndustryStandardDetail;
import com.pig4cloud.pig.admin.mapper.IndustryStandardDetailMapper;
import com.pig4cloud.pig.admin.service.IndustryStandardDetailService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 行业标准详情服务实现类
 *
 * @author pig4cloud
 */
@Service
@AllArgsConstructor
public class IndustryStandardDetailServiceImpl extends ServiceImpl<IndustryStandardDetailMapper, IndustryStandardDetail> implements IndustryStandardDetailService {

}
