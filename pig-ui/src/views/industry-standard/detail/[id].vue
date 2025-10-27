<template>
  <div class="standard-detail">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="logo-section">
          <div class="logo">SAC</div>
          <div class="title">行业标准信息服务平台</div>
        </div>
        <div class="nav-tabs">
          <div class="tab" @click="goToQuery">标准查询</div>
          <div class="tab">月报查询</div>
          <div class="tab">标准公告查询</div>
          <div class="tab">帮助中心</div>
        </div>
        <div class="login-link">行业标准备案系统登录入口</div>
      </div>
    </div>

    <!-- 主要内容区 -->
    <div class="main-content">
      <!-- 左侧导航栏 -->
      <div class="sidebar">
        <div class="sidebar-item orange">查看文本</div>
        <div class="sidebar-item green">意见建议</div>
        <div class="sidebar-item green small" v-for="n in 6" :key="n">建议</div>
      </div>

      <!-- 详情内容 -->
      <div class="content-area">
        <div class="detail-header" v-if="standardDetail">
          <h1 class="standard-title">{{ standardDetail.standardName }}</h1>
          <div class="standard-info-header">
            <div class="standard-number">{{ standardDetail.standardNumber }}</div>
            <div class="industry-tag">{{ standardDetail.industryField }}</div>
            <div class="status-badge" :class="standardDetail.status">{{ standardDetail.statusText }}</div>
          </div>
        </div>

        <!-- 目录导航 -->
        <div class="toc">
          <div class="toc-item" @click="scrollTo('status')">标准状态</div>
          <div class="toc-item" @click="scrollTo('basic')">基础信息</div>
          <div class="toc-item" @click="scrollTo('filing')">备案信息</div>
          <div class="toc-item" @click="scrollTo('scope')">适用范围</div>
          <div class="toc-item" @click="scrollTo('drafting')">起草单位</div>
          <div class="toc-item" @click="scrollTo('drafters')">起草人</div>
        </div>

        <!-- 标准状态 -->
        <div id="status" class="section">
          <h2 class="section-title">标准状态</h2>
          <div class="timeline">
            <div class="timeline-item">
              <div class="timeline-dot"></div>
              <div class="timeline-content">
                <div class="timeline-label">发布于 2025-10-11</div>
              </div>
            </div>
            <div class="timeline-item">
              <div class="timeline-dot"></div>
              <div class="timeline-content">
                <div class="timeline-label">实施于 2025-10-11</div>
              </div>
            </div>
            <div class="timeline-item">
              <div class="timeline-dot empty"></div>
              <div class="timeline-content">
                <div class="timeline-label">废止</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 基础信息 -->
        <div id="basic" class="section" v-if="standardDetail">
          <h2 class="section-title">基础信息</h2>
          <div class="info-table">
            <div class="info-row">
              <div class="info-label">标准号</div>
              <div class="info-value">{{ standardDetail.standardNumber }}</div>
            </div>
            <div class="info-row">
              <div class="info-label">发布日期</div>
              <div class="info-value">{{ standardDetail.publishDate }}</div>
            </div>
            <div class="info-row">
              <div class="info-label">实施日期</div>
              <div class="info-value">{{ standardDetail.implementDate }}</div>
            </div>
            <div class="info-row">
              <div class="info-label">制修订</div>
              <div class="info-value">{{ standardDetail.revisionType }}</div>
            </div>
            <div class="info-row">
              <div class="info-label">中国标准分类号</div>
              <div class="info-value">{{ standardDetail.chinaClassification }}</div>
            </div>
            <div class="info-row">
              <div class="info-label">国际标准分类号</div>
              <div class="info-value">{{ standardDetail.internationalClassification }}</div>
            </div>
            <div class="info-row">
              <div class="info-label">技术归口</div>
              <div class="info-value">{{ standardDetail.technicalCommittee }}</div>
            </div>
            <div class="info-row">
              <div class="info-label">批准发布部门</div>
              <div class="info-value">{{ standardDetail.approvalDepartment }}</div>
            </div>
            <div class="info-row">
              <div class="info-label">行业分类</div>
              <div class="info-value">{{ standardDetail.industryClassification }}</div>
            </div>
            <div class="info-row">
              <div class="info-label">标准类别</div>
              <div class="info-value">{{ standardDetail.standardCategory }}</div>
            </div>
          </div>
        </div>

        <!-- 备案信息 -->
        <div id="filing" class="section" v-if="standardDetail">
          <h2 class="section-title">备案信息</h2>
          <div class="info-table">
            <div class="info-row">
              <div class="info-label">备案号</div>
              <div class="info-value">{{ standardDetail.recordNumber }}</div>
            </div>
            <div class="info-row">
              <div class="info-label">备案日期</div>
              <div class="info-value">{{ standardDetail.recordDate }}</div>
            </div>
          </div>
        </div>

        <!-- 适用范围 -->
        <div id="scope" class="section" v-if="standardDetail">
          <h2 class="section-title">适用范围</h2>
          <div class="content-text">
            {{ standardDetail.scope }}
          </div>
        </div>

        <!-- 起草单位 -->
        <div id="drafting" class="section" v-if="standardDetail">
          <h2 class="section-title">起草单位</h2>
          <div class="content-text">
            {{ standardDetail.draftingUnits.join('、') }}
          </div>
        </div>

        <!-- 起草人 -->
        <div id="drafters" class="section" v-if="standardDetail">
          <h2 class="section-title">起草人</h2>
          <div class="content-text">
            {{ standardDetail.draftingPersons.join('、') }}
          </div>
        </div>
      </div>
    </div>

    <!-- 页面底部 -->
    <div class="page-footer">
      <div class="footer-content">
        <div class="social-media">
          <div class="media-title">市场监管总局"新媒体矩阵"</div>
          <div class="qr-codes">
            <div class="qr-item">
              <div class="qr-code"></div>
              <div class="qr-label">市场监管总局官方微信</div>
            </div>
            <div class="qr-item">
              <div class="qr-code"></div>
              <div class="qr-label">市场监管总局官方微博</div>
            </div>
            <div class="qr-item">
              <div class="qr-code"></div>
              <div class="qr-label">市场监管总局官方抖音</div>
            </div>
            <div class="qr-item">
              <div class="qr-code"></div>
              <div class="qr-label">市场监管总局官方快手</div>
            </div>
          </div>
        </div>
        <div class="footer-info">
          <div class="copyright">
            <div class="copyright-text">版权所有侵权必究</div>
            <div class="org-info">
              <div>主管:国家标准化管理委员会</div>
              <div>主办:国家市场监督管理总局国家标准技术审评中心</div>
              <div>技术支持:北京中标赛宇科技有限公司</div>
              <div>支持电话:13261900266</div>
            </div>
          </div>
          <div class="links">
            <div class="links-title">友情链接</div>
            <div class="link-item">国家标准化管理委员会</div>
            <div class="link-item">企业标准信息公共服务平台</div>
            <div class="link-item">全国团体标准信息平台</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getStandardDetailById, type StandardDetail } from '/@/data/industry-standard'

const route = useRoute()
const router = useRouter()

// 获取标准详情数据
const standardDetail = computed(() => {
  const id = parseInt(route.params.id as string)
  return getStandardDetailById(id)
})

const scrollTo = (id: string) => {
  const element = document.getElementById(id)
  if (element) {
    element.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
}

const goToQuery = () => {
  router.push('/industry-standard/query')
}

onMounted(() => {
  console.log('Standard Detail Page - ID:', route.params.id)
  console.log('Standard Detail:', standardDetail.value)
})
</script>

<style scoped>
.standard-detail {
  min-height: 100vh;
  height: 100vh;
  overflow-y: auto;
  background-color: #f5f5f5;
  display: flex;
  flex-direction: column;
}

/* 页面头部 */
.page-header {
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
  color: white;
  padding: 20px 0;
  flex-shrink: 0;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.logo-section {
  display: flex;
  align-items: center;
  gap: 15px;
}

.logo {
  background: rgba(255, 255, 255, 0.2);
  padding: 8px 12px;
  border-radius: 4px;
  font-weight: bold;
  font-size: 18px;
}

.title {
  font-size: 20px;
  font-weight: 500;
}

.nav-tabs {
  display: flex;
  gap: 30px;
}

.tab {
  padding: 8px 16px;
  cursor: pointer;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.tab:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

.login-link {
  font-size: 14px;
  opacity: 0.9;
}

/* 主要内容区 */
.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
  display: flex;
  gap: 30px;
  flex: 1;
}

/* 左侧导航栏 */
.sidebar {
  width: 60px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.sidebar-item {
  padding: 12px 8px;
  border-radius: 6px 0 0 6px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.3s;
  writing-mode: vertical-lr;
  text-align: center;
}

.sidebar-item.orange {
  background-color: #ff9800;
  color: white;
}

.sidebar-item.green {
  background-color: #4caf50;
  color: white;
}

.sidebar-item.small {
  font-size: 10px;
  padding: 8px 6px;
}

.sidebar-item:hover {
  transform: translateX(-5px);
}

/* 详情内容区 */
.content-area {
  flex: 1;
  background: white;
  border-radius: 8px;
  padding: 40px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.detail-header {
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 2px solid #f0f0f0;
}

.standard-title {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin-bottom: 15px;
  line-height: 1.5;
}

.standard-info-header {
  display: flex;
  align-items: center;
  gap: 15px;
  flex-wrap: wrap;
}

.standard-number {
  font-size: 20px;
  font-weight: 600;
  color: #2a5298;
}

.industry-tag {
  padding: 6px 12px;
  background-color: #e3f2fd;
  color: #2a5298;
  border-radius: 4px;
  font-size: 14px;
}

.status-badge {
  padding: 6px 12px;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 500;
}

.status-badge.current {
  background-color: #e8f5e8;
  color: #2e7d32;
}

/* 目录 */
.toc {
  display: flex;
  gap: 20px;
  padding: 20px 0;
  margin-bottom: 30px;
  border-bottom: 1px solid #e0e0e0;
  flex-wrap: wrap;
}

.toc-item {
  padding: 8px 16px;
  background-color: #f5f5f5;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 14px;
}

.toc-item:hover {
  background-color: #e3f2fd;
  color: #2a5298;
}

/* 章节 */
.section {
  margin-bottom: 40px;
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 2px solid #2a5298;
}

/* 时间轴 */
.timeline {
  position: relative;
  padding-left: 30px;
}

.timeline-item {
  position: relative;
  padding-bottom: 30px;
}

.timeline-item:not(:last-child)::before {
  content: '';
  position: absolute;
  left: -20px;
  top: 25px;
  width: 2px;
  height: calc(100% - 10px);
  background-color: #e0e0e0;
}

.timeline-dot {
  position: absolute;
  left: -27px;
  top: 0;
  width: 15px;
  height: 15px;
  border-radius: 50%;
  background-color: #2a5298;
  border: 3px solid white;
  box-shadow: 0 0 0 2px #2a5298;
}

.timeline-dot.empty {
  background-color: #ccc;
  box-shadow: 0 0 0 2px #ccc;
}

.timeline-content {
  padding-left: 10px;
}

.timeline-label {
  font-size: 16px;
  color: #666;
  font-weight: 500;
}

/* 信息表格 */
.info-table {
  display: flex;
  flex-direction: column;
}

.info-row {
  display: grid;
  grid-template-columns: 200px 1fr;
  padding: 15px 0;
  border-bottom: 1px solid #f0f0f0;
}

.info-row:last-child {
  border-bottom: none;
}

.info-label {
  font-weight: 600;
  color: #666;
  font-size: 14px;
}

.info-value {
  color: #333;
  font-size: 14px;
  word-break: break-word;
}

/* 正文内容 */
.content-text {
  font-size: 16px;
  line-height: 1.8;
  color: #333;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 6px;
}

/* 页面底部 */
.page-footer {
  background-color: #1e3c72;
  color: white;
  padding: 40px 0;
  margin-top: 40px;
  flex-shrink: 0;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  justify-content: space-between;
}

.social-media {
  flex: 1;
}

.media-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 20px;
  text-align: center;
}

.qr-codes {
  display: flex;
  gap: 30px;
  justify-content: center;
}

.qr-item {
  text-align: center;
}

.qr-code {
  width: 80px;
  height: 80px;
  background-color: white;
  border-radius: 8px;
  margin-bottom: 10px;
}

.qr-label {
  font-size: 12px;
  opacity: 0.9;
}

.footer-info {
  flex: 1;
  display: flex;
  justify-content: space-between;
}

.copyright-text {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 15px;
}

.org-info div {
  margin-bottom: 5px;
  font-size: 14px;
  opacity: 0.9;
}

.links-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 15px;
}

.link-item {
  margin-bottom: 8px;
  font-size: 14px;
  opacity: 0.9;
  cursor: pointer;
}

.link-item:hover {
  opacity: 1;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .main-content {
    flex-direction: column;
  }
  
  .sidebar {
    flex-direction: row;
    width: 100%;
    overflow-x: auto;
  }
  
  .sidebar-item {
    writing-mode: horizontal-tb;
    border-radius: 4px;
  }
  
  .info-row {
    grid-template-columns: 1fr;
    gap: 10px;
  }
  
  .footer-content {
    flex-direction: column;
    gap: 30px;
  }
}
</style>
