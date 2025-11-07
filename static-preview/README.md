# 标准AI立方 - 静态页面预览

这是一套基于PPT规划设计的静态页面预览，用于展示"标准AI立方"平台的页面结构和功能布局。

## 📁 文件结构

```
static-preview/
├── index.html              # 首页
├── standard-query.html      # 标准查询页面
├── standard-ai.html        # 标准大模型页面
├── standard-system.html    # 标准体系页面
├── visualization.html       # 标准可视化页面
├── standard-detail.html    # 标准详情页面
├── login.html              # 登录页面
├── admin.html              # 管理后台
├── user.html               # 用户后台
├── css/
│   ├── common.css          # 通用样式
│   ├── index.css           # 首页样式
│   ├── query.css            # 查询页面样式
│   ├── ai.css               # 大模型页面样式
│   ├── system.css           # 体系页面样式
│   ├── visualization.css   # 可视化页面样式
│   ├── detail.css           # 详情页样式
│   ├── login.css            # 登录页样式
│   ├── admin.css            # 管理后台样式
│   └── user.css             # 用户后台样式
├── js/
│   ├── query.js             # 查询页面交互
│   ├── system.js            # 体系页面交互
│   ├── visualization.js     # 可视化页面交互
│   ├── detail.js            # 详情页交互
│   └── login.js             # 登录页交互
└── README.md               # 说明文档
```

## 🚀 快速开始

### 方式一：直接打开HTML文件

直接在浏览器中打开 `index.html` 文件即可查看首页。

### 方式二：使用本地服务器（推荐）

```bash
# 使用Python启动本地服务器
cd static-preview
python3 -m http.server 8000

# 或使用Node.js的http-server
npx http-server -p 8000
```

然后在浏览器中访问：`http://localhost:8000`

## 📄 页面说明

### 公共前端页面

1. **index.html** - 首页
   - 数据概览统计
   - 核心功能入口
   - 最新动态展示

2. **standard-query.html** - 标准查询
   - 行业标准/地方标准切换
   - 高级筛选功能
   - 查询结果列表
   - 分页功能

3. **standard-ai.html** - 标准大模型
   - 6大AI功能模块
   - 立项评估示例
   - 功能卡片展示

4. **standard-system.html** - 标准体系
   - 标准体系定制
   - 领域标准体系
   - 行业标准体系

5. **visualization.html** - 标准可视化
   - 标准知识图谱
   - 统计图表
   - 标准地图

6. **standard-detail.html** - 标准详情
   - 基本信息
   - 详细信息
   - 相关标准
   - 修订历史

7. **login.html** - 登录/注册
   - 登录表单
   - 注册表单
   - 社交登录

### 后台页面

8. **admin.html** - 管理后台
   - 数据概览
   - 标准库管理
   - 用户管理
   - 角色管理

9. **user.html** - 用户后台
   - 体系维护
   - 组织管理
   - 信息统计

## 🎨 设计特点

- **现代化设计**：采用简洁现代的UI设计风格
- **响应式布局**：支持PC和移动端访问
- **交互友好**：丰富的交互效果和用户反馈
- **功能完整**：覆盖PPT规划中的所有主要功能模块

## 📝 注意事项

1. **静态演示**：这些页面是静态HTML，所有功能都是演示效果，不会真正调用后端API
2. **数据模拟**：页面中的数据都是模拟数据，用于展示页面效果
3. **样式独立**：每个页面都有独立的CSS文件，便于后续集成到Vue项目中
4. **交互基础**：JavaScript只实现了基础的交互效果，实际功能需要对接后端

## 🔄 后续集成

这些静态页面可以作为设计参考，后续可以：

1. **转换为Vue组件**：将HTML结构转换为Vue组件
2. **集成到pig-ui项目**：替换现有的页面或作为新页面
3. **对接后端API**：将演示功能替换为真实的API调用
4. **完善交互逻辑**：添加更丰富的交互和错误处理

## 📋 功能对照表

| PPT规划功能 | 静态页面 | 状态 |
|------------|---------|------|
| 标准查询 | standard-query.html | ✅ 已完成 |
| 标准大模型 | standard-ai.html | ✅ 已完成 |
| 标准体系 | standard-system.html | ✅ 已完成 |
| 标准可视化 | visualization.html | ✅ 已完成 |
| 标准详情 | standard-detail.html | ✅ 已完成 |
| 管理后台 | admin.html | ✅ 已完成 |
| 用户后台 | user.html | ✅ 已完成 |
| 登录注册 | login.html | ✅ 已完成 |

## 🎯 下一步

1. 查看静态页面效果，确认是否符合预期
2. 根据反馈调整页面设计和布局
3. 将静态页面转换为Vue组件
4. 集成到pig-ui项目中
5. 对接后端API实现真实功能

## 📞 反馈

如果对页面设计有任何建议或需要调整，请随时反馈！

---

**创建时间**：2025-01-15  
**版本**：v1.0  
**基于**：副本A4模板-网页v4.pdf

