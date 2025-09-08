<template>
  <div class="p-4">
    <el-card>
      <template #header>
        <div class="flex items-center justify-between">
          <span>标准评估</span>
          <div>
            <el-select v-model="language" size="small" style="width: 140px">
              <el-option label="中文(zh-cn)" value="zh-cn" />
              <el-option label="English(en)" value="en" />
            </el-select>
          </div>
        </div>
      </template>

      <el-form @submit.prevent>
        <el-form-item label="评估内容">
          <el-input
            v-model="content"
            type="textarea"
            :rows="8"
            placeholder="请输入需要评估的文本，例如：元宇宙技术要求"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="onSubmit">提交</el-button>
          <el-button @click="onClear">清空</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="mt-4" v-if="result">
      <template #header>
        <span>评估结果</span>
      </template>
      <div class="space-y-2">
        <div><b>主题：</b>{{ result.topic }}</div>
        <div><b>分数：</b>{{ result.score }}</div>
        <div><b>结论：</b>{{ result.summary }}</div>
        <div v-if="result.recommendations?.length">
          <b>建议：</b>
          <ul class="list-disc pl-6">
            <li v-for="(item, idx) in result.recommendations" :key="idx">{{ item }}</li>
          </ul>
        </div>
      </div>
    </el-card>
  </div>
  
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { ElMessage } from 'element-plus';
import { postAssessStandard, type AssessStandardResponse } from '/@/api/assess/standard';

const content = ref('');
const language = ref<'zh-cn' | 'en'>('zh-cn');
const loading = ref(false);
const result = ref<AssessStandardResponse | null>(null);

const onSubmit = async () => {
  if (!content.value?.trim()) {
    ElMessage.warning('请输入评估内容');
    return;
  }
  loading.value = true;
  result.value = null;
  try {
    const data = await postAssessStandard({ content: content.value, language: language.value });
    result.value = data;
  } catch (e: any) {
    ElMessage.error(e?.msg || '评估失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};

const onClear = () => {
  content.value = '';
  result.value = null;
};
</script>

<style scoped>
.mt-4 { margin-top: 1rem; }
</style>


