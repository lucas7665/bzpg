import request from '/@/utils/request';

/**
 * 标准评估请求参数
 */
export interface StandardEvaluationRequest {
	title: string;
}

/**
 * 标准评估结果
 */
export interface StandardEvaluationResult {
	resultTable: string;
	result: string;
	status: string;
	errorMessage?: string;
}

/**
 * 标准评估
 * @param data 评估请求参数
 * @returns 评估结果
 */
export function assessStandard(data: StandardEvaluationRequest) {
	return request({
		url: '/admin/evaluation/assess',
		method: 'post',
		data: data,
		timeout: 120000, // 2分钟超时
		headers: {
			skipToken: true, // 跳过token验证
		},
	});
}
