import request from '/@/utils/request';

export interface AssessStandardRequest {
  content: string;
  language?: 'zh-cn' | 'en';
}

export interface AssessStandardResponse {
  topic: string;
  summary: string;
  score: number;
  recommendations: string[];
}

/**
 * 标准评估（MVP）
 */
export async function postAssessStandard(payload: AssessStandardRequest): Promise<AssessStandardResponse> {
  const res = await request.post('/assess/standard', payload);
  return res as AssessStandardResponse;
}


