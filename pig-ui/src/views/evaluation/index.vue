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
						<!-- Markdown内容展示 -->
						<div class="markdown-result" v-if="evaluationResult && evaluationResult.resultTable">
							<div 
								class="markdown-container" 
								ref="markdownContainer" 
								@scroll="handleScroll"
								:style="{ maxHeight: tableMaxHeight }"
							>
								<div class="markdown-content" v-html="renderMarkdown(evaluationResult.resultTable)"></div>
								<div class="scroll-hint" v-if="showScrollHint">
									<el-icon><ArrowDown /></el-icon>
									<span>向下滚动查看更多内容</span>
								</div>
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
		
		<!-- 回到顶部按钮 -->
		<el-backtop 
			:right="40" 
			:bottom="40"
			:visibility-height="200"
			class="back-to-top"
		>
			<el-icon><ArrowUp /></el-icon>
		</el-backtop>
	</div>
</template>

<script setup lang="ts" name="StandardEvaluation">
import { ref, reactive, computed, nextTick, onMounted, onUnmounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { ArrowDown, ArrowUp } from '@element-plus/icons-vue';
import { assessStandard, type StandardEvaluationRequest, type StandardEvaluationResult } from '/@/api/evaluation';
import { marked } from 'marked';
import Watermark from '/@/utils/wartermark';

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

// Markdown容器引用
const markdownContainer = ref<HTMLElement | null>(null);

// 滚动提示显示状态
const showScrollHint = ref(false);

// 动态计算表格高度
const tableMaxHeight = ref('calc(100vh - 300px)');

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
			
			// 等待DOM更新后检查滚动提示
			await nextTick();
			setTimeout(() => {
				checkScrollHint();
			}, 100);
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
	showScrollHint.value = false;
	if (evaluationFormRef.value) {
		evaluationFormRef.value.resetFields();
	}
};

// 动态计算表格高度
const calculateTableHeight = () => {
	const windowHeight = window.innerHeight;
	const windowWidth = window.innerWidth;
	
	// 根据屏幕尺寸调整参数
	let taskbarHeight = 60; // 默认任务栏高度
	let headerHeight = 120; // 页面头部高度
	let padding = 40; // 额外边距
	
	// 移动端调整
	if (windowWidth <= 768) {
		taskbarHeight = 40;
		headerHeight = 100;
		padding = 30;
	}
	
	// 超小屏幕调整
	if (windowWidth <= 480) {
		taskbarHeight = 30;
		headerHeight = 80;
		padding = 20;
	}
	
	const availableHeight = windowHeight - taskbarHeight - headerHeight - padding;
	const minHeight = windowWidth <= 480 ? 250 : windowWidth <= 768 ? 300 : 400;
	
	tableMaxHeight.value = `${Math.max(minHeight, availableHeight)}px`;
};

// 检查是否需要显示滚动提示
const checkScrollHint = () => {
	if (!markdownContainer.value) return;
	
	const container = markdownContainer.value;
	const hasVerticalScroll = container.scrollHeight > container.clientHeight;
	const hasHorizontalScroll = container.scrollWidth > container.clientWidth;
	
	showScrollHint.value = hasVerticalScroll || hasHorizontalScroll;
};

// 滚动事件处理
const handleScroll = () => {
	if (!markdownContainer.value) return;
	
	const container = markdownContainer.value;
	const scrollTop = container.scrollTop;
	const scrollLeft = container.scrollLeft;
	
	// 如果已经滚动了一定距离，隐藏提示
	if (scrollTop > 50 || scrollLeft > 50) {
		showScrollHint.value = false;
	}
};

// 渲染Markdown内容
const renderMarkdown = (markdown: string) => {
	if (!markdown) return '';
	
	// 配置marked选项
	marked.setOptions({
		breaks: true, // 支持换行
		gfm: true, // 支持GitHub风格的Markdown
	});
	
	// 解析markdown为HTML
	return marked(markdown);
};

// 生命周期钩子
onMounted(() => {
	// 强制删除水印
	Watermark.del();
	
	// 计算初始表格高度
	calculateTableHeight();
	
	// 监听窗口大小变化
	window.addEventListener('resize', () => {
		calculateTableHeight();
		checkScrollHint();
	});
});

onUnmounted(() => {
	// 清理事件监听
	window.removeEventListener('resize', calculateTableHeight);
	window.removeEventListener('resize', checkScrollHint);
});
</script>

<style scoped lang="scss">
.evaluation-container {
	min-height: 100vh;
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	padding: 20px;
	padding-bottom: 80px; /* 添加底部安全区域 */
	overflow-y: auto;
	scroll-behavior: smooth;
	height: 100vh; /* 设置固定高度确保滚动条生效 */
	
	/* 自定义页面滚动条样式 */
	&::-webkit-scrollbar {
		width: 12px;
	}
	
	&::-webkit-scrollbar-track {
		background: rgba(255, 255, 255, 0.2);
		border-radius: 6px;
		margin: 4px;
	}
	
	&::-webkit-scrollbar-thumb {
		background: rgba(255, 255, 255, 0.6);
		border-radius: 6px;
		border: 2px solid transparent;
		background-clip: content-box;
		
		&:hover {
			background: rgba(255, 255, 255, 0.8);
			background-clip: content-box;
		}
		
		&:active {
			background: rgba(255, 255, 255, 0.9);
			background-clip: content-box;
		}
	}
	
	&::-webkit-scrollbar-corner {
		background: rgba(255, 255, 255, 0.1);
	}
	
	/* Firefox 滚动条样式 */
	scrollbar-width: thin;
	scrollbar-color: rgba(255, 255, 255, 0.6) rgba(255, 255, 255, 0.2);
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
	max-width: 1600px; /* 扩大页面内容显示宽度 */
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
	
	.table-container {
		overflow-y: auto;
		border: 1px solid #dcdfe6;
		border-radius: 8px;
		background: #fff;
		position: relative;
		
		/* 自定义滚动条样式 */
		&::-webkit-scrollbar {
			width: 8px;
			height: 8px;
		}
		
		&::-webkit-scrollbar-track {
			background: #f1f1f1;
			border-radius: 4px;
		}
		
		&::-webkit-scrollbar-thumb {
			background: #c1c1c1;
			border-radius: 4px;
			
			&:hover {
				background: #a8a8a8;
			}
		}
		
		&::-webkit-scrollbar-corner {
			background: #f1f1f1;
		}
	}
	
	.scroll-hint {
		position: absolute;
		bottom: 10px;
		right: 10px;
		background: rgba(64, 158, 255, 0.9);
		color: white;
		padding: 8px 12px;
		border-radius: 20px;
		font-size: 12px;
		display: flex;
		align-items: center;
		gap: 4px;
		animation: bounce 2s infinite;
		z-index: 20;
		box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
		
		.el-icon {
			font-size: 14px;
		}
	}
	
	@keyframes bounce {
		0%, 20%, 50%, 80%, 100% {
			transform: translateY(0);
		}
		40% {
			transform: translateY(-5px);
		}
		60% {
			transform: translateY(-3px);
		}
	}
	
	.markdown-content {
		overflow-x: auto;
		min-width: 100%;
		
		.evaluation-table {
			width: 100%;
			min-width: 800px; /* 确保表格有最小宽度 */
			border-collapse: collapse;
			border: none;
			margin: 0;
			
			tr {
				&:nth-child(even) {
					background-color: #f8f9fa;
				}
				
				&:first-child {
					background-color: #409eff;
					color: white;
					font-weight: 600;
					position: sticky;
					top: 0;
					z-index: 10;
				}
			}
			
			td {
				padding: 12px 15px;
				border: 1px solid #dcdfe6;
				text-align: left;
				vertical-align: top;
				line-height: 1.5;
				white-space: nowrap;
				
				&:first-child {
					font-weight: 600;
					background-color: #f5f7fa;
					position: sticky;
					left: 0;
					z-index: 5;
					min-width: 80px;
				}
				
				&:nth-child(2) {
					min-width: 120px;
				}
				
				&:nth-child(3) {
					min-width: 200px;
					white-space: normal;
					word-break: break-word;
				}
				
				&:nth-child(4) {
					min-width: 100px;
				}
				
				&:nth-child(5) {
					min-width: 150px;
					white-space: normal;
					word-break: break-word;
				}
			}
		}
	}
}

.markdown-result {
	margin-bottom: 30px;
	
	.markdown-container {
		overflow-y: auto;
		border: 1px solid #dcdfe6;
		border-radius: 8px;
		background: #fff;
		position: relative;
		
		/* 自定义滚动条样式 */
		&::-webkit-scrollbar {
			width: 8px;
			height: 8px;
		}
		
		&::-webkit-scrollbar-track {
			background: #f1f1f1;
			border-radius: 4px;
		}
		
		&::-webkit-scrollbar-thumb {
			background: #c1c1c1;
			border-radius: 4px;
			
			&:hover {
				background: #a8a8a8;
			}
		}
		
		&::-webkit-scrollbar-corner {
			background: #f1f1f1;
		}
	}
	
	.scroll-hint {
		position: absolute;
		bottom: 10px;
		right: 10px;
		background: rgba(64, 158, 255, 0.9);
		color: white;
		padding: 8px 12px;
		border-radius: 20px;
		font-size: 12px;
		display: flex;
		align-items: center;
		gap: 4px;
		animation: bounce 2s infinite;
		z-index: 20;
		box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
		
		.el-icon {
			font-size: 14px;
		}
	}
	
	.markdown-content {
		padding: 20px;
		
		// 表格样式
		table {
			width: 100%;
			min-width: 800px;
			border-collapse: collapse;
			margin: 20px 0;
			border: 2px solid #409eff; // 表格外边框
			
			thead tr {
				background-color: #409eff;
				color: white;
				font-weight: 600;
				position: sticky;
				top: 0;
				z-index: 10;
			}
			
			tbody tr:first-child td:first-child {
				position: sticky;
				left: 0;
				background: #f5f7fa;
				z-index: 5;
			}
			
			th, td {
				padding: 12px 8px;
				border: 1px solid #d9d9d9; // 更明显的单元格边框
				text-align: left;
				vertical-align: top;
				line-height: 1.5;
				font-size: 14px;
			}
			
			// 表头边框
			th {
				border-bottom: 2px solid #409eff; // 表头底部加粗边框
				border-right: 1px solid #d9d9d9;
				
				&:last-child {
					border-right: none;
				}
			}
			
			// 表格行边框
			tr {
				border-bottom: 1px solid #d9d9d9; // 更明显的行分隔线
				
				&:last-child {
					border-bottom: 2px solid #409eff; // 最后一行底部加粗边框
				}
			}
			
			td {
				border-right: 1px solid #d9d9d9;
				
				&:last-child {
					border-right: none;
				}
				
				&:nth-child(1) {
					min-width: 80px;
					font-weight: 500;
					border-right: 2px solid #409eff; // 第一列右边加粗边框
				}
				
				&:nth-child(2) {
					min-width: 120px;
				}
				
				&:nth-child(3) {
					min-width: 200px;
					white-space: normal;
				}
				
				&:nth-child(4) {
					min-width: 150px;
					font-weight: 500;
				}
				
				&:nth-child(5) {
					min-width: 200px;
					white-space: normal;
				}
			}
			
			tr:nth-child(even) {
				background-color: #f8f9fa;
			}
			
			tr:hover {
				background-color: #f0f9ff;
			}
			
			/* 行分隔线加强：为每一行底部增加更清晰的分割线 */
			tbody tr:not(:last-child) td {
				border-bottom: 1.5px solid #cfd8dc;
			}
		}

		/* 保障边框样式对 v-html 渲染内容强制生效（避免被全局样式覆盖） */
		:deep(table) {
			border-collapse: collapse !important;
			border: 2px solid #409eff !important;
		}
		:deep(th),
		:deep(td) {
			border: 1px solid #d9d9d9 !important;
		}
		
		// 标题样式
		h3 {
			color: #409eff;
			margin: 30px 0 15px 0;
			font-size: 18px;
			font-weight: 600;
			border-bottom: 2px solid #409eff;
			padding-bottom: 8px;
		}
		
		// 列表样式
		ul {
			margin: 15px 0;
			padding-left: 20px;
			
			li {
				margin: 8px 0;
				line-height: 1.6;
			}
		}
		
		// 段落样式
		p {
			margin: 10px 0;
			line-height: 1.6;
			color: #333;
		}
		
		// 强调样式
		strong {
			color: #409eff;
			font-weight: 600;
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
		padding-bottom: 60px; /* 移动端底部安全区域 */
		
		/* 移动端滚动条样式 */
		&::-webkit-scrollbar {
			width: 8px;
		}
		
		&::-webkit-scrollbar-track {
			background: rgba(255, 255, 255, 0.3);
			border-radius: 4px;
		}
		
		&::-webkit-scrollbar-thumb {
			background: rgba(255, 255, 255, 0.7);
			border-radius: 4px;
			
			&:hover {
				background: rgba(255, 255, 255, 0.9);
			}
		}
		
		/* Firefox 移动端滚动条 */
		scrollbar-width: thin;
		scrollbar-color: rgba(255, 255, 255, 0.7) rgba(255, 255, 255, 0.3);
	}
	
	.evaluation-header h1 {
		font-size: 2rem;
	}
	
	.evaluation-content {
		max-width: 100%;
	}
	
	.markdown-result {
		.markdown-content table {
			font-size: 0.9rem;
			min-width: 600px; /* 移动端最小宽度 */
			border: 2px solid #409eff; // 保持表格外边框
			
			th, td {
				padding: 8px 10px;
				border: 1px solid #d9d9d9; // 保持单元格边框
			}
			
			th {
				border-bottom: 2px solid #409eff; // 保持表头底部加粗边框
			}
			
			tr {
				border-bottom: 1px solid #d9d9d9; // 保持行分隔线
			}
			/* 行分隔线加强（移动端） */
			tbody tr:not(:last-child) td {
				border-bottom: 1.5px solid #cfd8dc;
			}
			
			td {
				&:first-child {
					min-width: 60px; /* 移动端减少第一列宽度 */
					border-right: 2px solid #409eff; // 保持第一列右边加粗边框
				}
				
				&:nth-child(2) {
					min-width: 80px;
				}
				
				&:nth-child(3) {
					min-width: 150px;
				}
				
				&:nth-child(4) {
					min-width: 80px;
				}
				
				&:nth-child(5) {
					min-width: 120px;
				}
			}
		}
	}
}

// 超小屏幕优化
@media (max-width: 480px) {
	.evaluation-container {
		padding-bottom: 50px; /* 超小屏幕底部安全区域 */
		
		/* 超小屏幕滚动条样式 */
		&::-webkit-scrollbar {
			width: 6px;
		}
		
		&::-webkit-scrollbar-track {
			background: rgba(255, 255, 255, 0.4);
			border-radius: 3px;
		}
		
		&::-webkit-scrollbar-thumb {
			background: rgba(255, 255, 255, 0.8);
			border-radius: 3px;
			
			&:hover {
				background: rgba(255, 255, 255, 1);
			}
		}
		
		/* Firefox 超小屏幕滚动条 */
		scrollbar-width: thin;
		scrollbar-color: rgba(255, 255, 255, 0.8) rgba(255, 255, 255, 0.4);
	}
	
	.markdown-result {
		.markdown-content table {
			font-size: 0.85rem;
			min-width: 500px;
			border: 2px solid #409eff; // 保持表格外边框
			
			th, td {
				padding: 6px 8px;
				border: 1px solid #d9d9d9; // 保持单元格边框
			}
			
			th {
				border-bottom: 2px solid #409eff; // 保持表头底部加粗边框
			}
			
			tr {
				border-bottom: 1px solid #d9d9d9; // 保持行分隔线
			}
			/* 行分隔线加强（超小屏幕） */
			tbody tr:not(:last-child) td {
				border-bottom: 1.5px solid #cfd8dc;
			}
			
			td:first-child {
				border-right: 2px solid #409eff; // 保持第一列右边加粗边框
			}
		}
	}
}

// 回到顶部按钮样式
:deep(.back-to-top) {
	.el-backtop {
		background: rgba(64, 158, 255, 0.9);
		border: 2px solid rgba(255, 255, 255, 0.3);
		box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
		transition: all 0.3s ease;
		
		&:hover {
			background: rgba(64, 158, 255, 1);
			transform: translateY(-2px);
			box-shadow: 0 6px 16px rgba(0, 0, 0, 0.3);
		}
		
		.el-icon {
			color: white;
			font-size: 18px;
		}
	}
}
</style>
