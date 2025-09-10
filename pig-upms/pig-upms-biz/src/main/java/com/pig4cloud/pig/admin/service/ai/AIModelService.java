package com.pig4cloud.pig.admin.service.ai;

import com.pig4cloud.pig.admin.dto.StandardEvaluationRequest;

/**
 * AI模型服务接口
 *
 * @author pig
 * @date 2025/01/15
 */
public interface AIModelService {
    
    /**
     * 获取模型名称
     *
     * @return 模型名称
     */
    String getModelName();
    
    /**
     * 评估标准
     *
     * @param request 评估请求
     * @return 评估结果
     */
    String evaluate(StandardEvaluationRequest request);
    
    /**
     * 是否支持该模型
     *
     * @param modelName 模型名称
     * @return 是否支持
     */
    boolean supports(String modelName);
}
