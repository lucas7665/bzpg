// 标准查询页面交互

// 标签页切换
document.querySelectorAll('.tab-btn').forEach(btn => {
    btn.addEventListener('click', function() {
        // 移除所有active类
        document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
        // 添加active类到当前按钮
        this.classList.add('active');
        
        // 这里可以添加切换内容的逻辑
        const tab = this.dataset.tab;
        console.log('切换到:', tab);
    });
});

// 高级筛选展开/收起
const toggleFilter = document.querySelector('.toggle-filter');
const filterContent = document.querySelector('.filter-content');

if (toggleFilter && filterContent) {
    toggleFilter.addEventListener('click', function() {
        const isHidden = filterContent.style.display === 'none';
        filterContent.style.display = isHidden ? 'block' : 'none';
        const arrow = this.querySelector('.arrow');
        if (arrow) {
            arrow.textContent = isHidden ? '▲' : '▼';
        }
        this.classList.toggle('active', !isHidden);
    });
}

// 快速筛选标签
document.querySelectorAll('.filter-tag').forEach(tag => {
    tag.addEventListener('click', function() {
        document.querySelectorAll('.filter-tag').forEach(t => t.classList.remove('active'));
        this.classList.add('active');
        const filter = this.dataset.filter;
        console.log('应用筛选:', filter);
    });
});

// 视图切换
document.querySelectorAll('.view-btn').forEach(btn => {
    btn.addEventListener('click', function() {
        document.querySelectorAll('.view-btn').forEach(b => b.classList.remove('active'));
        this.classList.add('active');
        const view = this.dataset.view;
        console.log('切换到视图:', view);
    });
});

// 搜索功能（示例）
document.querySelectorAll('.search-btn, .search-btn-large').forEach(btn => {
    btn.addEventListener('click', function() {
        const input = this.previousElementSibling;
        const keyword = input.value.trim();
        if (keyword) {
            console.log('搜索关键词:', keyword);
            // 这里可以添加实际的搜索逻辑
            alert('搜索功能：' + keyword);
        }
    });
});

// 回车搜索
document.querySelectorAll('.search-input, .search-input-large').forEach(input => {
    input.addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            const btn = this.nextElementSibling;
            if (btn) btn.click();
        }
    });
});

