// 标准评估系统 JavaScript

document.addEventListener('DOMContentLoaded', function() {
    const form = document.getElementById('evaluationForm');
    const submitBtn = document.getElementById('submitBtn');
    const resultSection = document.getElementById('resultSection');
    const loadingState = document.getElementById('loadingState');
    const successResult = document.getElementById('successResult');
    const errorResult = document.getElementById('errorResult');
    const resultStatus = document.getElementById('resultStatus');
    const tableContent = document.getElementById('tableContent');
    const conclusionContent = document.getElementById('conclusionContent');
    const errorContent = document.getElementById('errorContent');

    // 表单提交事件
    form.addEventListener('submit', function(e) {
        e.preventDefault();
        
        const title = document.getElementById('title').value.trim();
        if (!title) {
            showError('请输入标准名称');
            return;
        }
        
        if (title.length < 2 || title.length > 200) {
            showError('标准名称长度应在2-200个字符之间');
            return;
        }
        
        submitEvaluation(title);
    });

    // 提交评估请求
    async function submitEvaluation(title) {
        // 显示加载状态
        showLoading();
        
        try {
            const response = await fetch('/admin/evaluation/assess', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ title: title })
            });
            
            const result = await response.json();
            
            if (response.ok && result.code === 0) {
                showSuccess(result.data);
            } else {
                showError(result.msg || '评估失败，请稍后重试');
            }
        } catch (error) {
            console.error('评估请求失败:', error);
            showError('网络错误，请检查连接后重试');
        }
    }

    // 显示加载状态
    function showLoading() {
        resultSection.style.display = 'block';
        loadingState.style.display = 'block';
        successResult.style.display = 'none';
        errorResult.style.display = 'none';
        resultStatus.textContent = '';
        resultStatus.className = 'badge';
        
        // 禁用提交按钮
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<i class="bi bi-hourglass-split"></i> 评估中...';
        
        // 滚动到结果区域
        resultSection.scrollIntoView({ behavior: 'smooth' });
    }

    // 显示成功结果
    function showSuccess(data) {
        loadingState.style.display = 'none';
        successResult.style.display = 'block';
        errorResult.style.display = 'none';
        
        // 设置状态徽章
        resultStatus.textContent = '评估成功';
        resultStatus.className = 'badge bg-success';
        
        // 显示评估表格
        if (data.resultTable) {
            tableContent.innerHTML = formatMarkdownTable(data.resultTable);
        } else {
            tableContent.innerHTML = '<p class="text-muted">暂无详细分析数据</p>';
        }
        
        // 显示综合结论
        if (data.result) {
            conclusionContent.textContent = data.result;
        } else {
            conclusionContent.textContent = '请查看详细分析结果';
        }
        
        // 添加淡入动画
        successResult.classList.add('fade-in');
        
        // 恢复提交按钮
        resetSubmitButton();
    }

    // 显示错误结果
    function showError(message) {
        loadingState.style.display = 'none';
        successResult.style.display = 'none';
        errorResult.style.display = 'block';
        
        // 设置状态徽章
        resultStatus.textContent = '评估失败';
        resultStatus.className = 'badge bg-danger';
        
        // 显示错误信息
        errorContent.textContent = message;
        
        // 添加淡入动画
        errorResult.classList.add('fade-in');
        
        // 恢复提交按钮
        resetSubmitButton();
    }

    // 恢复提交按钮状态
    function resetSubmitButton() {
        submitBtn.disabled = false;
        submitBtn.innerHTML = '<i class="bi bi-play-circle"></i> 开始评估';
    }

    // 格式化Markdown表格为HTML
    function formatMarkdownTable(markdown) {
        if (!markdown) return '<p class="text-muted">暂无表格数据</p>';
        
        // 简单的Markdown表格解析
        const lines = markdown.split('\n').filter(line => line.trim());
        let tableHtml = '<table class="table evaluation-table">';
        
        let isFirstRow = true;
        for (const line of lines) {
            if (line.includes('|')) {
                const cells = line.split('|').map(cell => cell.trim()).filter(cell => cell);
                
                if (isFirstRow) {
                    // 表头
                    tableHtml += '<thead><tr>';
                    for (const cell of cells) {
                        tableHtml += `<th>${escapeHtml(cell)}</th>`;
                    }
                    tableHtml += '</tr></thead><tbody>';
                    isFirstRow = false;
                } else if (cells.length > 1) {
                    // 数据行
                    tableHtml += '<tr>';
                    for (const cell of cells) {
                        tableHtml += `<td>${escapeHtml(cell)}</td>`;
                    }
                    tableHtml += '</tr>';
                }
            }
        }
        
        tableHtml += '</tbody></table>';
        return tableHtml;
    }

    // HTML转义
    function escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    // 重置表单
    window.resetForm = function() {
        form.reset();
        resultSection.style.display = 'none';
        document.getElementById('title').focus();
    };

    // 页面加载完成后的初始化
    document.getElementById('title').focus();
    
    // 添加键盘快捷键支持
    document.addEventListener('keydown', function(e) {
        // Ctrl+Enter 提交表单
        if (e.ctrlKey && e.key === 'Enter') {
            form.dispatchEvent(new Event('submit'));
        }
        
        // Escape 重置表单
        if (e.key === 'Escape') {
            resetForm();
        }
    });

    // 自动调整文本域高度
    const textarea = document.getElementById('title');
    textarea.addEventListener('input', function() {
        this.style.height = 'auto';
        this.style.height = this.scrollHeight + 'px';
    });

    // 字符计数
    const charCount = document.createElement('small');
    charCount.className = 'text-muted';
    charCount.style.float = 'right';
    textarea.parentNode.appendChild(charCount);
    
    textarea.addEventListener('input', function() {
        const length = this.value.length;
        charCount.textContent = `${length}/200`;
        
        if (length > 200) {
            charCount.className = 'text-danger';
        } else if (length > 180) {
            charCount.className = 'text-warning';
        } else {
            charCount.className = 'text-muted';
        }
    });
    
    // 初始化字符计数
    textarea.dispatchEvent(new Event('input'));
});
