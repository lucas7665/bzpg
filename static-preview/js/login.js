// 登录页面交互

// 标签页切换
document.querySelectorAll('.tab-btn').forEach(btn => {
    btn.addEventListener('click', function() {
        // 移除所有active类
        document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
        document.querySelectorAll('.login-form').forEach(f => f.style.display = 'none');
        
        // 添加active类
        this.classList.add('active');
        
        // 显示对应表单
        const tab = this.dataset.tab;
        const form = document.getElementById(tab + 'Form');
        if (form) {
            form.style.display = 'block';
            form.classList.add('active');
        }
    });
});

// 表单提交
document.getElementById('loginForm')?.addEventListener('submit', function(e) {
    e.preventDefault();
    alert('登录功能（演示）');
});

document.getElementById('registerForm')?.addEventListener('submit', function(e) {
    e.preventDefault();
    alert('注册功能（演示）');
});

