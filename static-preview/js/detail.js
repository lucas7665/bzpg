// 标准详情页交互

// 标签页切换
document.querySelectorAll('.detail-tab').forEach(tab => {
    tab.addEventListener('click', function() {
        // 移除所有active类
        document.querySelectorAll('.detail-tab').forEach(t => t.classList.remove('active'));
        document.querySelectorAll('.detail-content').forEach(c => {
            c.style.display = 'none';
            c.classList.remove('active');
        });
        
        // 添加active类
        this.classList.add('active');
        
        // 显示对应内容
        const tabId = this.dataset.tab;
        const content = document.getElementById(tabId);
        if (content) {
            content.style.display = 'block';
            content.classList.add('active');
        }
    });
});

// 收藏功能
document.querySelectorAll('.action-btn').forEach(btn => {
    if (btn.textContent.includes('收藏')) {
        btn.addEventListener('click', function() {
            alert('收藏功能（演示）');
        });
    }
    
    if (btn.textContent.includes('跟踪')) {
        btn.addEventListener('click', function() {
            alert('跟踪功能（演示）');
        });
    }
    
    if (btn.textContent.includes('分享')) {
        btn.addEventListener('click', function() {
            alert('分享功能（演示）');
        });
    }
    
    if (btn.textContent.includes('下载')) {
        btn.addEventListener('click', function() {
            alert('下载功能（演示）');
        });
    }
});

