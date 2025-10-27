<template>
  <div class="industry-standard-home">
    <div class="hero-section">
      <div class="hero-content">
        <h1 class="hero-title">行业标准信息服务平台</h1>
        <p class="hero-subtitle">提供全面的行业标准查询、浏览和管理服务</p>
        <div class="hero-actions">
          <button class="btn-primary" @click="goToQuery">开始查询</button>
          <button class="btn-secondary" @click="goToEvaluation">标准评估</button>
        </div>
      </div>
    </div>

    <div class="features-section">
      <div class="container">
        <h2 class="section-title">平台功能</h2>
        <div class="features-grid">
          <div class="feature-card">
            <div class="feature-icon">🔍</div>
            <h3 class="feature-title">标准查询</h3>
            <p class="feature-desc">支持按标准号、名称、行业等多维度查询行业标准信息</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon">📊</div>
            <h3 class="feature-title">标准评估</h3>
            <p class="feature-desc">提供标准质量评估和分析功能，助力标准制定</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon">📋</div>
            <h3 class="feature-title">详细信息</h3>
            <p class="feature-desc">展示标准的完整信息，包括起草单位、适用范围等</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon">🔄</div>
            <h3 class="feature-title">实时更新</h3>
            <p class="feature-desc">定期更新标准数据，确保信息的准确性和时效性</p>
          </div>
        </div>
      </div>
    </div>

    <div class="stats-section">
      <div class="container">
        <h2 class="section-title">数据统计</h2>
        <div class="stats-grid">
          <div class="stat-item">
            <div class="stat-number">87,141</div>
            <div class="stat-label">行业标准总数</div>
          </div>
          <div class="stat-item">
            <div class="stat-number">50+</div>
            <div class="stat-label">覆盖行业</div>
          </div>
          <div class="stat-item">
            <div class="stat-number">40+</div>
            <div class="stat-label">部委机构</div>
          </div>
          <div class="stat-item">
            <div class="stat-number">100%</div>
            <div class="stat-label">数据准确性</div>
          </div>
        </div>
      </div>
    </div>

    <div class="footer-section">
      <div class="container">
        <div class="footer-content">
          <div class="footer-info">
            <h3>行业标准信息服务平台</h3>
            <p>提供权威、准确、及时的行业标准信息服务</p>
          </div>
          <div class="footer-links">
            <div class="link-group">
              <h4>功能导航</h4>
              <a @click="goToQuery">标准查询</a>
              <a @click="goToEvaluation">标准评估</a>
            </div>
            <div class="link-group">
              <h4>关于我们</h4>
              <a>平台介绍</a>
              <a>联系我们</a>
            </div>
          </div>
        </div>
        <div class="footer-bottom">
          <p>&copy; 2025 行业标准信息服务平台. 版权所有.</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const goToQuery = () => {
  router.push('/industry-standard/query')
}

const goToEvaluation = () => {
  router.push('/evaluation')
}
</script>

<style scoped>
.industry-standard-home {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.hero-section {
  padding: 100px 0;
  text-align: center;
  color: white;
}

.hero-content {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 20px;
}

.hero-title {
  font-size: 3.5rem;
  font-weight: 700;
  margin-bottom: 20px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.hero-subtitle {
  font-size: 1.25rem;
  margin-bottom: 40px;
  opacity: 0.9;
}

.hero-actions {
  display: flex;
  gap: 20px;
  justify-content: center;
  flex-wrap: wrap;
}

.btn-primary, .btn-secondary {
  padding: 15px 30px;
  border: none;
  border-radius: 8px;
  font-size: 1.1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-primary {
  background-color: #ff6b6b;
  color: white;
}

.btn-primary:hover {
  background-color: #ff5252;
  transform: translateY(-2px);
}

.btn-secondary {
  background-color: transparent;
  color: white;
  border: 2px solid white;
}

.btn-secondary:hover {
  background-color: white;
  color: #667eea;
  transform: translateY(-2px);
}

.features-section, .stats-section {
  padding: 80px 0;
  background-color: white;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.section-title {
  text-align: center;
  font-size: 2.5rem;
  font-weight: 700;
  margin-bottom: 50px;
  color: #333;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 30px;
}

.feature-card {
  text-align: center;
  padding: 30px 20px;
  border-radius: 12px;
  background-color: #f8f9fa;
  transition: transform 0.3s ease;
}

.feature-card:hover {
  transform: translateY(-5px);
}

.feature-icon {
  font-size: 3rem;
  margin-bottom: 20px;
}

.feature-title {
  font-size: 1.5rem;
  font-weight: 600;
  margin-bottom: 15px;
  color: #333;
}

.feature-desc {
  color: #666;
  line-height: 1.6;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 30px;
}

.stat-item {
  text-align: center;
  padding: 30px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 12px;
}

.stat-number {
  font-size: 3rem;
  font-weight: 700;
  margin-bottom: 10px;
}

.stat-label {
  font-size: 1.1rem;
  opacity: 0.9;
}

.footer-section {
  background-color: #2c3e50;
  color: white;
  padding: 60px 0 20px;
}

.footer-content {
  display: grid;
  grid-template-columns: 1fr 2fr;
  gap: 50px;
  margin-bottom: 40px;
}

.footer-info h3 {
  font-size: 1.5rem;
  margin-bottom: 15px;
}

.footer-info p {
  opacity: 0.8;
  line-height: 1.6;
}

.footer-links {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 30px;
}

.link-group h4 {
  margin-bottom: 15px;
  color: #ecf0f1;
}

.link-group a {
  display: block;
  margin-bottom: 8px;
  color: #bdc3c7;
  text-decoration: none;
  cursor: pointer;
  transition: color 0.3s ease;
}

.link-group a:hover {
  color: white;
}

.footer-bottom {
  text-align: center;
  padding-top: 20px;
  border-top: 1px solid #34495e;
  opacity: 0.8;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .hero-title {
    font-size: 2.5rem;
  }
  
  .hero-actions {
    flex-direction: column;
    align-items: center;
  }
  
  .footer-content {
    grid-template-columns: 1fr;
    gap: 30px;
  }
  
  .features-grid {
    grid-template-columns: 1fr;
  }
  
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
