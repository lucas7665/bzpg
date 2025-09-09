<template>
	<div class="evaluation-container">
		<div class="evaluation-header">
			<h1>标准评估系统</h1>
			<p>基于技术成熟度和市场成熟度的标准化评估工具</p>
		</div>
		
		<div class="evaluation-content">
			<!-- 输入区域 -->
			<div class="input-section">
				<el-card class="input-card">
					<template #header>
						<div class="card-header">
							<span>标准名称输入</span>
						</div>
					</template>
					
					<el-form :model="evaluationForm" :rules="rules" ref="evaluationFormRef" label-width="120px">
						<el-form-item label="标准名称" prop="title">
							<el-input
								v-model="evaluationForm.title"
								type="textarea"
								:rows="3"
								placeholder="请输入标准的中文名称，例如：元宇宙技术要求、半导体晶片直径测试方法等"
								maxlength="200"
								show-word-limit
							/>
						</el-form-item>
						
						<el-form-item>
							<el-button 
								type="primary" 
								@click="submitEvaluation"
								:loading="loading"
								:disabled="!evaluationForm.title.trim()"
							>
								{{ loading ? '评估中...' : '开始评估' }}
							</el-button>
							<el-button @click="resetForm">重置</el-button>
						</el-form-item>
					</el-form>
				</el-card>
			</div>
			
			<!-- 结果展示区域 -->
			<div class="result-section" v-if="evaluationResult">
				<el-card class="result-card">
					<template #header>
						<div class="card-header">
							<span>评估结果</span>
							<el-tag :type="resultStatusType" size="small">
								{{ resultStatusText }}
							</el-tag>
						</div>
					</template>
					
					<div v-if="evaluationResult && evaluationResult.status === 'SUCCESS'">
						<!-- 评估表格 -->
						<div class="result-table" v-if="evaluationResult && evaluationResult.resultTable">
							<h3>详细评估分析</h3>
							<div class="markdown-content" v-html="formatMarkdownTable(evaluationResult.resultTable)"></div>
						</div>
						
						<!-- 综合结论 -->
						<div class="result-conclusion" v-if="evaluationResult && evaluationResult.result">
							<h3>综合评估结论</h3>
							<div class="conclusion-content">
								<el-alert
									:title="evaluationResult?.result"
									type="info"
									:closable="false"
									show-icon
								/>
							</div>
						</div>
					</div>
					
					<div v-else class="error-content">
						<el-alert
							:title="evaluationResult?.errorMessage || '评估失败'"
							type="error"
							:closable="false"
							show-icon
						/>
					</div>
				</el-card>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts" name="StandardEvaluation">
import { ref, reactive, computed } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { assessStandard, type StandardEvaluationRequest, type StandardEvaluationResult } from '/@/api/evaluation';

// 表单数据
const evaluationForm = reactive<StandardEvaluationRequest>({
	title: ''
});

// 表单验证规则
const rules = {
	title: [
		{ required: true, message: '请输入标准名称', trigger: 'blur' },
		{ min: 2, max: 200, message: '标准名称长度在 2 到 200 个字符', trigger: 'blur' }
	]
};

// 表单引用
const evaluationFormRef = ref();

// 加载状态
const loading = ref(false);

// 评估结果
const evaluationResult = ref<StandardEvaluationResult | null>(null);

// 结果状态类型
const resultStatusType = computed(() => {
	if (!evaluationResult.value) return '';
	return evaluationResult.value.status === 'SUCCESS' ? 'success' : 'danger';
});

// 结果状态文本
const resultStatusText = computed(() => {
	if (!evaluationResult.value) return '';
	return evaluationResult.value.status === 'SUCCESS' ? '评估成功' : '评估失败';
});

// 提交评估
const submitEvaluation = async () => {
	if (!evaluationFormRef.value) return;
	
	try {
		// 表单验证
		await evaluationFormRef.value.validate();
		
		loading.value = true;
		evaluationResult.value = null;
		
		// 调用API
		const result = await assessStandard(evaluationForm);
		
		if (result && result.code === 0) {
			evaluationResult.value = result.data || {};
			ElMessage.success('评估完成');
		} else {
			ElMessage.error(result?.msg || '评估失败');
		}
		
	} catch (error: any) {
		console.error('评估失败:', error);
		
		// 判断是否是超时错误
		let errorMessage = '评估过程中发生错误，请稍后重试';
		if (error.code === 'ECONNABORTED' || error.message?.includes('timeout')) {
			errorMessage = '评估超时，AI处理时间较长，请稍后重试';
		} else if (error.response?.status === 500) {
			errorMessage = '服务器内部错误，请稍后重试';
		} else if (error.message) {
			errorMessage = error.message;
		}
		
		ElMessage.error(errorMessage);
		
		// 设置错误结果
		evaluationResult.value = {
			status: 'FAILED',
			errorMessage: errorMessage,
			resultTable: '',
			result: ''
		};
	} finally {
		loading.value = false;
	}
};

// 重置表单
const resetForm = () => {
	evaluationForm.title = '';
	evaluationResult.value = null;
	if (evaluationFormRef.value) {
		evaluationFormRef.value.resetFields();
	}
};

// 格式化Markdown表格
const formatMarkdownTable = (markdown: string) => {
	if (!markdown) return '';
	
	// 简单的Markdown表格转HTML
	return markdown
		.replace(/\|/g, '</td><td>')
		.replace(/^<td>/, '<td>')
		.replace(/<td>$/, '</td>')
		.replace(/\n/g, '</tr><tr>')
		.replace(/^/, '<table class="evaluation-table"><tr>')
		.replace(/$/, '</tr></table>')
		.replace(/<td><\/td>/g, '<td>&nbsp;</td>')
		.replace(/<tr><td><\/td><\/tr>/g, '')
		.replace(/<tr><td>([^<]+)<\/td><td>([^<]+)<\/td><td>([^<]+)<\/td><td>([^<]+)<\/td><td>([^<]+)<\/td><\/tr>/g, 
			'<tr><td>$1</td><td>$2</td><td>$3</td><td>$4</td><td>$5</td></tr>');
};
</script>

<style scoped lang="scss">
.evaluation-container {
	min-height: 100vh;
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	padding: 20px;
}

.evaluation-header {
	text-align: center;
	margin-bottom: 30px;
	color: white;
	
	h1 {
		font-size: 2.5rem;
		margin-bottom: 10px;
		font-weight: 600;
	}
	
	p {
		font-size: 1.1rem;
		opacity: 0.9;
	}
}

.evaluation-content {
	max-width: 1200px;
	margin: 0 auto;
}

.input-section {
	margin-bottom: 30px;
}

.input-card {
	border-radius: 12px;
	box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
	
	.card-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		font-weight: 600;
		font-size: 1.1rem;
	}
}

.result-section {
	.result-card {
		border-radius: 12px;
		box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
		
		.card-header {
			display: flex;
			justify-content: space-between;
			align-items: center;
			font-weight: 600;
			font-size: 1.1rem;
		}
	}
}

.result-table {
	margin-bottom: 30px;
	
	h3 {
		color: #409eff;
		margin-bottom: 15px;
		font-size: 1.2rem;
	}
	
	.markdown-content {
		overflow-x: auto;
		
		.evaluation-table {
			width: 100%;
			border-collapse: collapse;
			border: 1px solid #dcdfe6;
			border-radius: 6px;
			overflow: hidden;
			
			tr {
				&:nth-child(even) {
					background-color: #f8f9fa;
				}
				
				&:first-child {
					background-color: #409eff;
					color: white;
					font-weight: 600;
				}
			}
			
			td {
				padding: 12px 15px;
				border: 1px solid #dcdfe6;
				text-align: left;
				vertical-align: top;
				line-height: 1.5;
				
				&:first-child {
					font-weight: 600;
					background-color: #f5f7fa;
				}
			}
		}
	}
}

.result-conclusion {
	h3 {
		color: #409eff;
		margin-bottom: 15px;
		font-size: 1.2rem;
	}
	
	.conclusion-content {
		.el-alert {
			border-radius: 8px;
		}
	}
}

.error-content {
	.el-alert {
		border-radius: 8px;
	}
}

// 响应式设计
@media (max-width: 768px) {
	.evaluation-container {
		padding: 10px;
	}
	
	.evaluation-header h1 {
		font-size: 2rem;
	}
	
	.evaluation-content {
		max-width: 100%;
	}
	
	.result-table .markdown-content .evaluation-table {
		font-size: 0.9rem;
		
		td {
			padding: 8px 10px;
		}
	}
}
</style>
