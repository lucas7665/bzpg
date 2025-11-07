// 标准可视化页面交互

// 标签页切换
document.querySelectorAll('.viz-tab').forEach(tab => {
    tab.addEventListener('click', function() {
        // 移除所有active类
        document.querySelectorAll('.viz-tab').forEach(t => t.classList.remove('active'));
        document.querySelectorAll('.viz-content').forEach(c => c.style.display = 'none');
        
        // 添加active类
        this.classList.add('active');
        
        // 显示对应内容
        const tabId = this.dataset.tab;
        const content = document.getElementById(tabId);
        if (content) {
            content.style.display = 'block';
        }
    });
});

