// 标准体系页面交互

// 标签页切换
document.querySelectorAll('.system-tab').forEach(tab => {
    tab.addEventListener('click', function() {
        // 移除所有active类
        document.querySelectorAll('.system-tab').forEach(t => t.classList.remove('active'));
        document.querySelectorAll('.tab-content').forEach(c => c.style.display = 'none');
        
        // 添加active类到当前标签
        this.classList.add('active');
        
        // 显示对应内容
        const tabId = this.dataset.tab;
        const content = document.getElementById(tabId);
        if (content) {
            content.style.display = 'block';
        }
    });
});

