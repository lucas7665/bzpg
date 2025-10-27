<template>
  <div class="industry-standard-query">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="logo-section">
          <div class="logo">SAC</div>
          <div class="title">行业标准信息服务平台</div>
        </div>
        <div class="nav-tabs">
          <div class="tab active">标准查询</div>
          <div class="tab">月报查询</div>
          <div class="tab">标准公告查询</div>
          <div class="tab">帮助中心</div>
        </div>
        <div class="login-link">行业标准备案系统登录入口</div>
      </div>
    </div>

    <!-- 搜索区域 -->
    <div class="search-section">
      <div class="search-container">
        <div class="search-bar">
          <input 
            v-model="searchKeyword" 
            type="text" 
            placeholder="请输入标准号或标准名称关键字"
            class="search-input"
            @keyup.enter="handleSearch"
          />
          <button class="search-btn" @click="handleSearch">查询</button>
          <button class="reset-btn" @click="handleReset">重置</button>
        </div>
        
      </div>
    </div>

    <!-- 主体内容区 -->
    <div class="main-content-wrapper">
      <div class="main-content">
        <!-- 左侧部委筛选 -->
        <div class="left-panel">
          <div class="panel-header">
            <span class="panel-title">部委</span>
            <button class="clear-btn" @click="clearDepartment">清空</button>
          </div>
          <div class="panel-content">
            <div 
              v-for="dept in departments" 
              :key="dept.code"
              class="filter-item"
              :class="{ active: selectedDepartment === dept.code }"
              @click="selectDepartment(dept.code)"
            >
              {{ dept.name }} ({{ dept.count }})
            </div>
          </div>
        </div>

        <!-- 右侧内容区 -->
        <div class="right-panel">
          <!-- 行业代码筛选 -->
          <div class="industry-filter-section">
            <div class="filter-header">
              <span class="filter-title">行业代码：</span>
              <button class="all-btn" @click="clearIndustry">全部</button>
            </div>
            <div class="alphabet-nav">
              <span 
                v-for="letter in alphabet" 
                :key="letter"
                class="alphabet-item"
                :class="{ active: selectedLetter === letter }"
                @click="selectLetter(letter)"
              >
                {{ letter }}
              </span>
            </div>
            <div class="industry-list">
              <div 
                v-for="industry in filteredIndustries" 
                :key="industry.code"
                class="industry-item"
                :class="{ active: selectedIndustry === industry.code }"
                @click="selectIndustry(industry.code)"
              >
                {{ industry.code }} {{ industry.name }} ({{ industry.count }})
              </div>
            </div>
          </div>

          <!-- 备案日期和标准状态筛选 -->
          <div class="status-filters">
            <div class="filter-row">
              <div class="filter-row-header">
                <span class="filter-label">备案日期</span>
                <button class="single-select-btn">单选</button>
              </div>
              <div class="filter-options">
                <button 
                  class="filter-option" 
                  :class="{ active: selectedFilingDate === '' }"
                  @click="selectedFilingDate = ''"
                >
                  全部
                </button>
                <button 
                  class="filter-option" 
                  :class="{ active: selectedFilingDate === '1' }"
                  @click="selectedFilingDate = '1'"
                >
                  近一月
                </button>
                <button 
                  class="filter-option" 
                  :class="{ active: selectedFilingDate === '3' }"
                  @click="selectedFilingDate = '3'"
                >
                  近三月
                </button>
                <button 
                  class="filter-option" 
                  :class="{ active: selectedFilingDate === '6' }"
                  @click="selectedFilingDate = '6'"
                >
                  近半年
                </button>
                <button 
                  class="filter-option" 
                  :class="{ active: selectedFilingDate === '12' }"
                  @click="selectedFilingDate = '12'"
                >
                  近一年
                </button>
                <button 
                  class="filter-option" 
                  :class="{ active: selectedFilingDate === '24' }"
                  @click="selectedFilingDate = '24'"
                >
                  近两年
                </button>
                <button 
                  class="filter-option" 
                  :class="{ active: selectedFilingDate === '36' }"
                  @click="selectedFilingDate = '36'"
                >
                  近三年
                </button>
              </div>
            </div>
            <div class="filter-row">
              <div class="filter-row-header">
                <span class="filter-label">标准状态</span>
                <button class="single-select-btn">单选</button>
              </div>
              <div class="filter-options">
                <button 
                  class="filter-option" 
                  :class="{ active: selectedStatus === '' }"
                  @click="selectedStatus = ''"
                >
                  全部
                </button>
                <button 
                  class="filter-option" 
                  :class="{ active: selectedStatus === 'current' }"
                  @click="selectedStatus = 'current'"
                >
                  现行
                </button>
                <button 
                  class="filter-option" 
                  :class="{ active: selectedStatus === 'abolished' }"
                  @click="selectedStatus = 'abolished'"
                >
                  废止
                </button>
              </div>
            </div>
          </div>

          <!-- 明细列表 -->
          <div class="results-section">
            <div class="results-container">
              <div class="table-wrapper">
                <table class="results-table">
                <thead>
                  <tr>
                    <th>#</th>
                    <th>标准号</th>
                    <th>标准名称</th>
                    <th>行业领域</th>
                    <th>状态</th>
                    <th>批准日期</th>
                    <th>实施日期</th>
                  </tr>
                </thead>
                <tbody>
                  <tr 
                    v-for="(item, index) in paginatedResults" 
                    :key="item.id"
                    class="result-row"
                    @click="viewDetail(item)"
                  >
                    <td>{{ (currentPage - 1) * pageSize + index + 1 }}</td>
                    <td class="standard-number">{{ item.standardNumber }}</td>
                    <td class="standard-name">{{ item.standardName }}</td>
                    <td>{{ item.industryField }}</td>
                    <td>
                      <span class="status-badge" :class="item.status">
                        {{ item.statusText }}
                      </span>
                    </td>
                    <td>{{ item.approvalDate }}</td>
                    <td>{{ item.implementationDate }}</td>
                  </tr>
                </tbody>
              </table>
              </div>

              <!-- 分页 -->
              <div class="pagination-section">
                <div class="pagination-info">
                  显示第{{ (currentPage - 1) * pageSize + 1 }}到第{{ Math.min(currentPage * pageSize, totalRecords) }}条记录,
                  总共{{ totalRecords }}条记录 每页显示 {{ pageSize }} 条记录
                </div>
                <div class="pagination-controls">
                  <button 
                    class="page-btn" 
                    :disabled="currentPage === 1"
                    @click="goToPage(currentPage - 1)"
                  >
                    &lt;
                  </button>
                  <button 
                    v-for="page in visiblePages" 
                    :key="page"
                    class="page-btn"
                    :class="{ active: page === currentPage }"
                    @click="goToPage(page)"
                  >
                    {{ page }}
                  </button>
                  <button 
                    class="page-btn" 
                    :disabled="currentPage === totalPages"
                    @click="goToPage(currentPage + 1)"
                  >
                    &gt;
                  </button>
                </div>
              </div>
            </div>
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
import { useRouter } from 'vue-router'
import { 
  sampleStandards, 
  departments, 
  industries, 
  alphabet, 
  searchStandards,
  type IndustryStandard 
} from '/@/data/industry-standard'

const router = useRouter()

// 搜索关键词
const searchKeyword = ref('')

// 筛选条件
const selectedDepartment = ref('')
const selectedIndustry = ref('')
const selectedLetter = ref('')
const selectedFilingDate = ref('')
const selectedStatus = ref('')

// 分页
const currentPage = ref(1)
const pageSize = ref(15)
const totalRecords = ref(sampleStandards.length)

// 搜索结果
const searchResults = ref<IndustryStandard[]>(sampleStandards)

// 计算属性
const filteredIndustries = computed(() => {
  if (!selectedLetter.value) return industries
  return industries.filter(industry => 
    industry.code.startsWith(selectedLetter.value)
  )
})

const totalPages = computed(() => {
  return Math.ceil(totalRecords.value / pageSize.value)
})

const visiblePages = computed(() => {
  const pages = []
  const start = Math.max(1, currentPage.value - 2)
  const end = Math.min(totalPages.value, currentPage.value + 2)
  
  for (let i = start; i <= end; i++) {
    pages.push(i)
  }
  
  if (end < totalPages.value) {
    pages.push('...')
    pages.push(totalPages.value)
  }
  
  return pages
})

const paginatedResults = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return searchResults.value.slice(start, end)
})

// 方法
const handleSearch = () => {
  const filters = {
    department: selectedDepartment.value,
    industry: selectedIndustry.value,
    status: selectedStatus.value,
    filingDate: selectedFilingDate.value
  }
  
  searchResults.value = searchStandards(searchKeyword.value, filters)
  totalRecords.value = searchResults.value.length
  currentPage.value = 1
}

const handleReset = () => {
  searchKeyword.value = ''
  selectedDepartment.value = ''
  selectedIndustry.value = ''
  selectedLetter.value = ''
  selectedFilingDate.value = ''
  selectedStatus.value = ''
  currentPage.value = 1
  
  // 重置搜索结果
  searchResults.value = sampleStandards
  totalRecords.value = sampleStandards.length
}

const selectDepartment = (code: string) => {
  selectedDepartment.value = selectedDepartment.value === code ? '' : code
  handleSearch()
}

const selectIndustry = (code: string) => {
  selectedIndustry.value = selectedIndustry.value === code ? '' : code
  handleSearch()
}

const selectLetter = (letter: string) => {
  selectedLetter.value = selectedLetter.value === letter ? '' : letter
}

const clearDepartment = () => {
  selectedDepartment.value = ''
  handleSearch()
}

const clearIndustry = () => {
  selectedIndustry.value = ''
  handleSearch()
}

const goToPage = (page: number | string) => {
  const pageNum = typeof page === 'string' ? parseInt(page) : page
  if (pageNum >= 1 && pageNum <= totalPages.value) {
    currentPage.value = pageNum
  }
}

const viewDetail = (item: IndustryStandard) => {
  router.push(`/industry-standard/detail/${item.id}`)
}

onMounted(() => {
  // 初始化数据
  searchResults.value = sampleStandards
  totalRecords.value = sampleStandards.length
})
</script>

<style scoped>
.industry-standard-query {
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

.tab.active {
  background-color: rgba(255, 255, 255, 0.2);
}

.login-link {
  font-size: 14px;
  opacity: 0.9;
}

/* 搜索区域 */
.search-section {
  background: white;
  padding: 30px 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  flex-shrink: 0;
}

.search-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.search-bar {
  display: flex;
  gap: 15px;
  margin-bottom: 30px;
}

.search-input {
  flex: 1;
  padding: 12px 16px;
  border: 2px solid #e0e0e0;
  border-radius: 6px;
  font-size: 16px;
  outline: none;
  transition: border-color 0.3s;
}

.search-input:focus {
  border-color: #2a5298;
}

.search-btn, .reset-btn {
  padding: 12px 24px;
  border: none;
  border-radius: 6px;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s;
}

.search-btn {
  background-color: #2a5298;
  color: white;
}

.search-btn:hover {
  background-color: #1e3c72;
}

.reset-btn {
  background-color: #f5f5f5;
  color: #666;
  border: 1px solid #e0e0e0;
}

.reset-btn:hover {
  background-color: #e0e0e0;
}

/* 主体内容区 */
.main-content-wrapper {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.main-content {
  display: flex;
  gap: 20px;
  background-color: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  flex: 1;
  min-height: 0;
}

/* 左侧部委面板 */
.left-panel {
  width: 280px;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  background-color: white;
}

.left-panel .panel-header {
  background-color: #2a5298;
  color: white;
  padding: 12px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.left-panel .panel-title {
  font-weight: 600;
  color: white;
}

.left-panel .clear-btn {
  background: none;
  border: none;
  color: white;
  cursor: pointer;
  font-size: 14px;
  padding: 4px 8px;
}

.left-panel .panel-content {
  max-height: 600px;
  overflow-y: auto;
  padding: 10px;
}

.left-panel .filter-item {
  padding: 10px 12px;
  cursor: pointer;
  border-radius: 4px;
  margin-bottom: 4px;
  transition: all 0.3s;
  font-size: 14px;
}

.left-panel .filter-item:hover {
  background-color: #f0f8ff;
}

.left-panel .filter-item.active {
  background-color: #e3f2fd;
  color: #2a5298;
  font-weight: 500;
}

/* 右侧内容区 */
.right-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
  min-height: 0;
  overflow: hidden;
}

/* 行业代码筛选区 */
.industry-filter-section {
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  padding: 15px;
  background-color: #fafafa;
}

.filter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.filter-title {
  font-weight: 600;
  color: #333;
  font-size: 15px;
}

.all-btn {
  background-color: #2a5298;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
}

.alphabet-nav {
  display: flex;
  gap: 8px;
  margin-bottom: 15px;
  flex-wrap: wrap;
}

.alphabet-item {
  padding: 6px 10px;
  cursor: pointer;
  border-radius: 4px;
  background-color: white;
  border: 1px solid #e0e0e0;
  font-size: 13px;
  transition: all 0.3s;
}

.alphabet-item:hover {
  background-color: #f0f8ff;
  border-color: #2a5298;
}

.alphabet-item.active {
  background-color: #2a5298;
  color: white;
  border-color: #2a5298;
}

.industry-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 8px;
  max-height: 250px;
  overflow-y: auto;
  padding: 10px;
}

.industry-item {
  padding: 8px 12px;
  cursor: pointer;
  border-radius: 4px;
  background-color: white;
  border: 1px solid #e0e0e0;
  font-size: 13px;
  transition: all 0.3s;
  text-align: center;
}

.industry-item:hover {
  background-color: #f0f8ff;
  border-color: #2a5298;
}

.industry-item.active {
  background-color: #e3f2fd;
  color: #2a5298;
  border-color: #2a5298;
  font-weight: 500;
}

/* 备案日期和标准状态筛选 */
.status-filters {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.filter-row {
  background-color: #fafafa;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  padding: 15px;
}

.filter-row-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.filter-label {
  font-weight: 600;
  color: #666;
  font-size: 14px;
}

.single-select-btn {
  background: none;
  border: none;
  color: #666;
  cursor: pointer;
  font-size: 12px;
  padding: 4px 8px;
}

.filter-options {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.filter-option {
  padding: 8px 16px;
  border: 1px solid #e0e0e0;
  background-color: white;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.3s;
}

.filter-option:hover {
  border-color: #2a5298;
}

.filter-option.active {
  background-color: #2a5298;
  color: white;
  border-color: #2a5298;
}

/* 结果区域 */
.results-section {
  margin-top: 0;
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
  overflow: hidden;
}

.results-container {
  padding: 0;
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
}

/* 表格容器 - 添加滚动 */
.table-wrapper {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  background-color: white;
  min-height: 400px;
}

.results-table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 0;
}

.results-table thead {
  position: sticky;
  top: 0;
  z-index: 10;
}

.results-table th {
  background-color: #f8f9fa;
  padding: 15px 12px;
  text-align: left;
  font-weight: 600;
  color: #333;
  border-bottom: 2px solid #e0e0e0;
}

.results-table td {
  padding: 15px 12px;
  border-bottom: 1px solid #e0e0e0;
}

.result-row {
  cursor: pointer;
  transition: background-color 0.3s;
}

.result-row:hover {
  background-color: #f8f9fa;
}

.standard-number {
  font-weight: 600;
  color: #2a5298;
}

.standard-name {
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.status-badge {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.current {
  background-color: #e8f5e8;
  color: #2e7d32;
}

.status-badge.abolished {
  background-color: #ffebee;
  color: #c62828;
}

/* 分页 */
.pagination-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 0;
  margin-top: 15px;
  background-color: white;
  flex-shrink: 0;
}

.pagination-info {
  color: #666;
  font-size: 14px;
}

.pagination-controls {
  display: flex;
  gap: 5px;
}

.page-btn {
  padding: 8px 12px;
  border: 1px solid #e0e0e0;
  background-color: white;
  cursor: pointer;
  border-radius: 4px;
  transition: all 0.3s;
}

.page-btn:hover:not(:disabled) {
  background-color: #f0f8ff;
  border-color: #2a5298;
}

.page-btn.active {
  background-color: #2a5298;
  color: white;
  border-color: #2a5298;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
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
@media (max-width: 1024px) {
  .main-content {
    flex-direction: column;
  }
  
  .left-panel {
    width: 100%;
  }
  
  .industry-list {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  }
}

@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    gap: 20px;
  }
  
  .nav-tabs {
    flex-wrap: wrap;
    justify-content: center;
  }
  
  .main-content-wrapper {
    padding: 10px;
  }
  
  .main-content {
    padding: 10px;
  }
  
  .industry-list {
    grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  }
  
  .filter-options {
    flex-direction: column;
  }
  
  .filter-option {
    width: 100%;
  }
  
  .pagination-section {
    flex-direction: column;
    gap: 15px;
  }
  
  .footer-content {
    flex-direction: column;
    gap: 30px;
  }
  
  .qr-codes {
    flex-wrap: wrap;
  }
  
  .footer-info {
    flex-direction: column;
    gap: 20px;
  }
}
</style>
