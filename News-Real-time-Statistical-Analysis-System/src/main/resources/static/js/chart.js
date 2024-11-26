// WebSocket 连接
let socket = new WebSocket('ws://localhost:8080/websocket');

// 只初始化一次图表
let titleChart = null;
let exposureGauge = null;

// WebSocket 连接成功时
socket.onopen = function(event) {
    console.log('WebSocket connection established');
};

// 处理消息
socket.onmessage = function(event) {
    const data = JSON.parse(event.data);

    // 更新图表和仪表盘
    updateCharts(data);
    fillTable(data);
};

// 图表更新
function updateCharts(data) {
    // 如果没有初始化图表，则初始化图表
    if (!titleChart) {
        titleChart = echarts.init(document.getElementById('titleChart'));
    }
    if (!exposureGauge) {
        exposureGauge = echarts.init(document.getElementById('topicGauge'));
    }

    // 只保留前10个数据
    const maxItems = 10;
    const limitedTitleName = data.titleName ? data.titleName.slice(0, maxItems) : [];
    const limitedTitleCount = data.titleCount ? data.titleCount.slice(0, maxItems) : [];

    // 更新条形图
    const titleOption = {
        tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
        grid: { left: '20%', right: '20%', bottom: '15%', top: '15%' }, // 增加上下边距
        xAxis: {
            type: 'value', // 设置 x 轴为数值轴
            splitLine: { lineStyle: { type: 'dotted' } }, // 横向网格线
        },
        yAxis: {
            type: 'category', // 设置 y 轴为类目轴
            data: limitedTitleName, // 只显示前10个类目
            axisLabel: {
                formatter: value => value.length > 10 ? value.slice(0, 10) + '...' : value, // 长度超过8的文本添加省略号
                rotate: 0, // 不旋转y轴标签
                interval: 0, // 确保所有标签都显示
                fontSize: 12, // 调整字体大小
            }
        },
        series: [{
            data: limitedTitleCount, // 只显示前10个数据
            type: 'bar', // 类型设置为条形图
            barCategoryGap: '40%', // 条形之间的间距
            itemStyle: {
                color: '#d1250d',
                borderRadius: [0, 10, 10, 0] // 圆角
            },
            label: { show: true, position: 'right' } // 显示标签
        }]
    };

    titleChart.setOption(titleOption, true);

    // 更新仪表盘
    const gaugeOption = {
        tooltip: { formatter: '{a} <br/>{b}: {c}个' }, // 提示框显示的内容
        series: [{
            name: '话题曝光量',  // 仪表盘的名称
            type: 'gauge',     // 类型为仪表盘
            startAngle: 225,   // 仪表盘起始角度
            endAngle: -45,     // 仪表盘结束角度
            radius: '90%',     // 仪表盘的半径
            min: 0,            // 设置最小值
            max: 10000,        // 设置最大值
            axisLine: {
                lineStyle: {
                    width: 20,  // 轴线的宽度
                    color: [
                        [0.2, '#6bcf85'],  // 绿色，值在0-2000之间时显示
                        [0.8, '#4f98ca'],  // 蓝色，值在2000-8000之间时显示
                        [1, '#f45b5b']     // 红色，值在8000-10000之间时显示
                    ]
                }
            },
            pointer: {
                width: 6,          // 指针的宽度
                length: '70%',     // 指针的长度
                color: 'auto'      // 自动根据数据变化设置指针颜色
            },
            detail: {
                valueAnimation: true, // 启动数值动画，平滑显示数值变化
                formatter: '{value}个话题', // 显示的详细信息格式
                fontSize: 24,          // 数值的字体大小
                offsetCenter: [0, '70%'] // 设置详细信息的显示位置，向下偏移70%
            },
            title: {
                offsetCenter: [0, '-30%'] // 设置仪表盘标题的位置，向上偏移30%
            },
            data: [{
                value: data.titleSum, // 动态获取话题总数量的数据
                name: '话题数量'      // 显示在仪表盘上的数据名称
            }]
        }]
    };

    exposureGauge.setOption(gaugeOption, true);
}

// 动态填充表格内容
function fillTable(data) {
    const tableBody = document.getElementById('titleTableBody');
    tableBody.innerHTML = ''; // 清空之前的表格内容
    data.titleName.forEach((title, index) => {
        const row = document.createElement('tr');
        row.innerHTML = `<td>${title}</td><td>${data.titleCount[index]}</td>`;
        tableBody.appendChild(row);
    });
}

// 处理 WebSocket 错误
socket.onerror = function(event) {
    console.error('WebSocket error:', event);
};

// 处理 WebSocket 关闭
socket.onclose = function(event) {
    console.log('WebSocket connection closed:', event);
    // 尝试重连
    reconnectWebSocket();
};

// 连接断开后尝试重连
function reconnectWebSocket() {
    setTimeout(() => {
        console.log('Reconnecting WebSocket...');
        socket = new WebSocket('ws://localhost:8080/websocket'); // 创建新的 WebSocket 连接
        socket.onopen = function() {
            console.log('Reconnected WebSocket connection established');
        };
        socket.onmessage = function(event) {
            const data = JSON.parse(event.data);
            updateCharts(data);
            fillTable(data);
        };
        socket.onerror = function(event) {
            console.error('WebSocket error during reconnect:', event);
        };
        socket.onclose = function(event) {
            console.log('WebSocket connection closed after reconnect:', event);
            reconnectWebSocket(); // 如果重新连接关闭，再次尝试重连
        };
    }, 5000); // 5秒后重连
}
