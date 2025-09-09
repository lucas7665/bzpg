package com.pig4cloud.pig.admin.service;

import com.pig4cloud.pig.admin.dto.StandardEvaluationRequest;
import com.pig4cloud.pig.admin.dto.StandardEvaluationResult;

/**
 * 标准评估服务接口
 *
 * @author pig
 * @date 2025/01/15
 */
public interface StandardEvaluationService {

    /**
     * 评估标准
     *
     * @param request 评估请求
     * @return 评估结果
     */
    StandardEvaluationResult evaluateStandard(StandardEvaluationRequest request);

}
