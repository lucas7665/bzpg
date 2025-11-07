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
        this.textContent = isHidden ? '收起' : '展开';
    });
}

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

