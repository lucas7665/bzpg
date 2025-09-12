<template>
  <div class="evaluation-container">
    <div class="container">
  <div class="banner">
    <h1>标准化评估系统</h1>
    <div class="version-badge">
      <i class="fas fa-flask"></i> STL大模型
    </div>
  </div>
  
  
  <div class="disclaimer">
    <p><i class="fas fa-exclamation-triangle"></i> <strong>演示说明：</strong> 此页面为标准化评估系统的概念验证版本，仅用于展示和初步测试目的，非最终正式系统。</p>
  </div>

    <div class="card">
      <div class="card-header">
        <i class="fas fa-search"></i>
        标准评估输入
      </div>
      <div class="input-group">
        <input 
          type="text" 
          v-model="evaluationForm.title"
          placeholder="请输入标准名称（例如：元宇宙技术）"
          @keyup.enter="submitEvaluation"
        />
        <button 
          @click="submitEvaluation"
          :disabled="loading"
          class="btn-primary"
        >
          {{ loading ? '评估中...' : '开始评估' }}
        </button>
      </div>
      <p class="feedback-note">
        <i class="fas fa-info-circle"></i> 请输入要评估的标准名称，然后点击"开始评估"按钮查看分析结果。
      </p>
    </div>

    <div class="card" v-if="evaluationResult && evaluationResult.status === 'SUCCESS'">
      <div class="card-header">
        <i class="fas fa-chart-pie"></i>
        综合结论
      </div>
      <div class="conclusion-box">
        <div class="conclusion-title">评估结论</div>
        <div class="conclusion-content">
          <p v-if="conclusionData.suggestion"><strong>建议:</strong> {{ conclusionData.suggestion }}</p>
          <p v-if="conclusionData.reason"><strong>理由:</strong> {{ conclusionData.reason }}</p>
        </div>
      </div>
    </div>

    <div class="card" v-if="evaluationResult && evaluationResult.status === 'SUCCESS'">
      <div class="card-header">
        <i class="fas fa-table"></i>
        评估详情
      </div>
      <div class="table-container">
        <table>
          <thead>
            <tr>
              <th>维度</th>
              <th>子维度</th>
              <th>维度具体情况</th>
              <th>结论</th>
              <th>相关说明</th>
            </tr>
          </thead>
          <tbody>
            <template v-for="(row, index) in tableData" :key="index">
              <tr>
                <td v-if="row.dimension" :rowspan="row.dimensionRowspan" class="dimension-cell">{{ row.dimension }}</td>
                <td :class="{'sub-dimension': row.isSubDimension}">{{ row.subDimension }}</td>
                <td>{{ row.specificSituation }}</td>
                <td :class="{'conclusion-cell': true}">{{ row.conclusion }}</td>
                <td>{{ row.explanation }}</td>
              </tr>
            </template>
          </tbody>
        </table>
      </div>
    </div>

    <footer>
        <p>© 2025 新一代技术标准化研究院 | 标准化评估系统 - 概念验证版</p>
        <p>反馈请联系: 15105417909</p>
      </footer>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { assessStandard, type StandardEvaluationRequest, type StandardEvaluationResult } from '/@/api/evaluation';
import Watermark from '/@/utils/wartermark';

interface TableRow {
  dimension: string;
  subDimension: string;
  specificSituation: string;
  conclusion: string;
  explanation: string;
  isDimensionHeader: boolean; // 保留接口兼容性，但不再使用
  isSubDimension: boolean;
  dimensionRowspan: number;
}

interface ConclusionData {
  suggestion: string;
  reason: string;
}

// 表单数据
const evaluationForm = reactive<StandardEvaluationRequest>({
  title: ''
});

// 加载状态
const loading = ref(false);

// 评估结果
const evaluationResult = ref<StandardEvaluationResult | null>(null);

// 解析后的表格数据
const tableData = ref<TableRow[]>([]);

// 解析后的结论数据
const conclusionData = ref<ConclusionData>({
  suggestion: '',
  reason: ''
});

// 提交评估
const submitEvaluation = async () => {
  if (!evaluationForm.title.trim()) {
    ElMessage.warning('请输入标准名称');
    return;
  }
  
  try {
    loading.value = true;
    evaluationResult.value = null;
    tableData.value = [];
    conclusionData.value = { suggestion: '', reason: '' };
    
    // 调用API
    const result = await assessStandard(evaluationForm);
    
    if (result && result.code === 0) {
      evaluationResult.value = result.data || null;
      
      // 解析markdown数据
      if (result.data?.resultTable) {
        parseEvaluationResult(result.data.resultTable);
      }
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
  } finally {
    loading.value = false;
  }
};

// 解析评估结果
const parseEvaluationResult = (markdownContent: string): void => {
  if (!markdownContent) return;
  
  // 分离表格和结论部分
  const parts = markdownContent.split('### 综合评估结论');
  const tablePart = parts[0] || '';
  const conclusionPart = parts[1] || '';
  
  // 解析表格数据
  parseTableData(tablePart);
  
  // 解析结论数据
  parseConclusionData(conclusionPart);
};

// 解析表格数据
const parseTableData = (tableContent: string): void => {
  const lines = tableContent.split('\n').filter((line: string) => line.trim());
  
  // 过滤掉表头行和分隔行
  const tableLines = lines.filter((line: string) => {
    return line.includes('|') && 
           !line.includes('---') && 
           !line.includes('维度 | 子维度 | 维度具体情况 | 结论 | 相关说明');
  });
  
  tableData.value = [];
  let currentDimension = '';
  let dimensionRowspan = 0;
  let dimensionStartIndex = 0;
  
  tableLines.forEach((line: string, index: number) => {
    // 分割行并清理空白
    const parts = line.split('|');
    // 过滤掉首尾空元素
    const cells = parts.filter((part, i) => i > 0 && i < parts.length - 1).map(cell => cell.trim());
    
    if (cells.length >= 5) {
      const dimension = cells[0];
      const subDimension = cells[1];
      const specificSituation = cells[2];
      const conclusion = cells[3];
      const explanation = cells[4];
      
      // 检查是否是新维度
      const isNewDimension = dimension && dimension !== currentDimension && dimension !== '';
      
      if (isNewDimension) {
        // 结束上一个维度的rowspan计算
        if (currentDimension && dimensionRowspan > 0) {
          for (let i = dimensionStartIndex; i < tableData.value.length; i++) {
            if (tableData.value[i].dimension === currentDimension) {
              tableData.value[i].dimensionRowspan = dimensionRowspan;
              break;
            }
          }
        }
        
        // 开始新维度
        currentDimension = dimension;
        dimensionRowspan = 1;
        dimensionStartIndex = tableData.value.length;
      } else if (currentDimension) {
        dimensionRowspan++;
      }
      
      // 检查是否是子维度行（维度列为空）
      const isSubDimension = dimension === '';
      
      const newRow: TableRow = {
        dimension: isNewDimension ? dimension : '',
        subDimension: subDimension,
        specificSituation: specificSituation,
        conclusion: conclusion,
        explanation: explanation,
        isDimensionHeader: false,
        isSubDimension: Boolean(isSubDimension),
        dimensionRowspan: 0
      };
      
      tableData.value.push(newRow);
    }
  });
  
  // 设置最后一个维度的rowspan
  if (currentDimension && dimensionRowspan > 0) {
    for (let i = dimensionStartIndex; i < tableData.value.length; i++) {
      if (tableData.value[i].dimension === currentDimension) {
        tableData.value[i].dimensionRowspan = dimensionRowspan;
        break;
      }
    }
  }
  
  console.log('解析后的表格数据:', tableData.value);
};

// 解析结论数据
const parseConclusionData = (conclusionContent: string): void => {
  const lines = conclusionContent.split('\n').filter((line: string) => line.trim());
  
  // 初始化结论数据
  conclusionData.value = {
    suggestion: '',
    reason: ''
  };
  
  // 提取建议和理由
  for (let i = 0; i < lines.length; i++) {
    const line = lines[i];
    
    if (line.includes('建议：') || line.includes('建议:')) {
      conclusionData.value.suggestion = line.replace(/^-?\s*建议[：:]\s*/, '').trim();
    } else if (line.includes('理由：') || line.includes('理由:')) {
      conclusionData.value.reason = line.replace(/^-?\s*理由[：:]\s*/, '').trim();
      
      // 如果理由后面还有内容，可能是长文本被分成多行，尝试合并
      let nextIndex = i + 1;
      while (nextIndex < lines.length) {
        const nextLine = lines[nextIndex];
        // 如果下一行不是新的标题（不包含"建议:"等）
        if (!nextLine.includes('建议:') && !nextLine.includes('建议：')) {
          conclusionData.value.reason += ' ' + nextLine.trim();
        } else {
          break;
        }
        nextIndex++;
      }
    }
  }
  
  console.log('解析后的结论数据:', conclusionData.value);
};

// 生命周期钩子
onMounted(() => {
  // 直接设置页面标题，只显示一个标题
  document.title = '标准评估系统';
  
  // 强制删除水印
  Watermark.del();
});
</script>

<style scoped lang="scss">
.evaluation-container {
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8f0 100%);
  min-height: 100vh;
  padding: 20px;
  padding-bottom: 80px; /* 添加底部安全区域 */
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  height: 100vh; /* 设置固定高度 */
  overflow-y: auto; /* 添加垂直滚动条 */
  scroll-behavior: smooth; /* 平滑滚动 */
}

.container {
  max-width: 1200px;
  margin: 0 auto;
}

/* 自定义滚动条样式 */
.evaluation-container::-webkit-scrollbar {
  width: 12px;
}

.evaluation-container::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.05);
  border-radius: 6px;
}

.evaluation-container::-webkit-scrollbar-thumb {
  background: rgba(44, 111, 187, 0.5);
  border-radius: 6px;
  border: 2px solid transparent;
  background-clip: content-box;
}

.evaluation-container::-webkit-scrollbar-thumb:hover {
  background: rgba(44, 111, 187, 0.7);
  background-clip: content-box;
}

/* Firefox 滚动条样式 */
.evaluation-container {
  scrollbar-width: thin;
  scrollbar-color: rgba(44, 111, 187, 0.5) rgba(0, 0, 0, 0.05);
}

.banner {
  background: linear-gradient(90deg, #2c6fbb 0%, #4a8fd2 100%);
  color: white;
  padding: 15px 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.banner h1 {
  color: white;
  font-size: 24px;
  font-weight: 700;
  margin: 0;
}

.subtitle {
  color: #666;
  font-size: 16px;
  font-weight: 500;
  text-align: center;
  margin-bottom: 20px;
}

.version-badge {
  background: #ffc107;
  color: #333;
  padding: 4px 10px;
  border-radius: 20px;
  font-weight: 600;
  font-size: 14px;
  display: inline-flex;
  align-items: center;
}

.version-badge i {
  margin-right: 5px;
}

.disclaimer {
  background-color: #fff8e1;
  border-left: 5px solid #ffc107;
  padding: 12px 15px;
  border-radius: 4px;
  margin-bottom: 25px;
  font-size: 14px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
  height: 50px;
}

.disclaimer i {
  color: #ffc107;
  margin-right: 8px;
  
}

.feedback-note {
	background: #f0f7ff;
    padding: 10px 15px;
    border-radius: 4px;
    margin-top: 5px;
    font-size: 14px;
    color: #666;
    display: flex;
    align-items: center;
    margin-bottom: 45px;
    margin-left: 20px;
    margin-right: 20px;
    height: 45px;
}

.feedback-note i {
  color: #2c6fbb;
  margin-right: 8px;
}

.card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.06);
  margin-bottom: 25px;
  overflow: hidden;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.card:hover {
  transform: translateY(-3px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.card-header {
  padding: 15px 20px;
  border-bottom: 2px solid #f0f7ff;
  color: #2c6fbb;
  font-weight: 800;
  font-size: 22px;
  display: flex;
  align-items: center;
  margin-bottom: 5px;
}

.card-header i {
  margin-right: 10px;
  font-size: 1.4rem;
  color: #2c6fbb;
  display: inline-block;
}

.input-group {
  display: flex;
  padding: 20px;
}

.input-group input {
  flex: 1;
  padding: 10px 15px;
  border: 1px solid #ddd;
  border-radius: 4px 0 0 4px;
  font-size: 16px;
  outline: none;
}

.input-group input:focus {
  border-color: #2c6fbb;
}

.btn-primary {
  background-color: #2c6fbb;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 0 4px 4px 0;
  cursor: pointer;
  font-size: 16px;
  font-weight: 500;
}

.btn-primary:hover {
  background-color: #245a9d;
}

.btn-primary:disabled {
  background-color: #a0b7d8;
  cursor: not-allowed;
}

.conclusion-box {
  padding: 20px;
}

.conclusion-title {
  color: #2c6fbb;
  padding: 10px 15px;
  border-left: 5px solid #2c6fbb;
  font-weight: 800;
  margin-bottom: 15px;
  font-size: 18px;
  background-color: #f0f7ff;
}

.conclusion-content {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  /* 移除高度限制和垂直滚动 */
}

.conclusion-content p {
  margin: 10px 0;
  line-height: 1.6;
  word-break: break-word;
  text-align: justify;
}

.conclusion-content strong {
  color: #2c6fbb;
  font-weight: 700;
}

.table-container {
  padding: 20px;
  overflow-x: auto; /* 保留水平滚动 */
  /* 移除垂直滚动和高度限制 */
}

table {
  width: 100%;
  border-collapse: collapse;
  border: 1px solid #ddd;
  table-layout: fixed; /* 固定表格布局 */
  margin: 20px 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

th, td {
  padding: 15px;
  text-align: left;
  border: 1px solid #e1e4e8;
  word-break: break-word; /* 允许单词内换行 */
  vertical-align: top; /* 顶部对齐 */
}

th {
  background-color: #2c6fbb;
  color: white;
  font-weight: 700;
  position: sticky; /* 表头固定 */
  top: 0;
  z-index: 10;
  text-align: center; /* 文字居中 */
}

/* 移除了dimension-row样式 */

.dimension-cell {
  background-color: #4a8fd2;
  color: white;
  font-weight: 700;
  text-align: center;
  vertical-align: middle;
  position: sticky; /* 维度列固定 */
  left: 0;
  z-index: 5;
}

.sub-dimension {
  padding-left: 15px !important;
  /* 统一所有子维度的内边距 */
}

.conclusion-cell {
  font-weight: 700;
  color: #28a745;
}

tr:nth-child(even) {
  background-color: #f5f7fa;
}

tr:hover {
  background-color: #f0f7ff;
}

/* 设置列宽 */
table th:nth-child(1), table td:nth-child(1) {
  width: 10%;
  min-width: 80px;
}

table th:nth-child(2), table td:nth-child(2) {
  width: 15%;
  min-width: 100px;
}

table th:nth-child(3), table td:nth-child(3) {
  width: 25%;
  min-width: 150px;
}

table th:nth-child(4), table td:nth-child(4) {
  width: 15%;
  min-width: 100px;
}

table th:nth-child(5), table td:nth-child(5) {
  width: 35%;
  min-width: 200px;
}

footer {
  text-align: center;
  margin-top: 40px;
  color: #666;
  font-size: 14px;
  font-weight: 400;
  padding: 20px 0;
  position: relative;
  z-index: 1;
  border-top: 1px solid #e1e4e8;
}

@media (max-width: 768px) {
  .evaluation-container {
    padding-bottom: 60px; /* 移动端底部安全区域 */
    
    /* 移动端滚动条样式 */
    &::-webkit-scrollbar {
      width: 8px;
    }
    
    &::-webkit-scrollbar-track {
      background: rgba(0, 0, 0, 0.03);
    }
    
    &::-webkit-scrollbar-thumb {
      background: rgba(44, 111, 187, 0.4);
    }
  }

  .input-group {
    flex-direction: column;
  }
  
  .input-group input {
    border-radius: 4px;
    margin-bottom: 10px;
  }
  
  .btn-primary {
    border-radius: 4px;
    width: 100%;
  }
  
  .table-container {
    padding: 10px;
  }
  
  th, td {
    padding: 8px;
    font-size: 14px;
  }
}

/* 超小屏幕优化 */
@media (max-width: 480px) {
  .evaluation-container {
    padding-bottom: 50px; /* 超小屏幕底部安全区域 */
    padding-left: 10px;
    padding-right: 10px;
  }
  
  .card {
    border-radius: 6px;
  }
  
  .card-header {
    padding: 12px 15px;
    font-size: 15px;
  }
  
  .table-container {
    padding: 10px 5px;
  }
}
</style>
// 测试git跟踪
// 测试修改
