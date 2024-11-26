package com.spark.service;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.*;

import com.alibaba.fastjson.JSON;

/**
 * WeblogSocketHandler 处理 WebSocket 消息
 * @author mo
 */
public class WeblogSocketHandler extends TextWebSocketHandler {

    private static final ThreadFactory THREAD_FACTORY = new ThreadFactory() {
        private final AtomicInteger threadNumber = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName("websocket-pool-thread-" + threadNumber.getAndIncrement());
            return thread;
        }
    };

    private static final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2, THREAD_FACTORY);
    private final WeblogService weblogService = new WeblogService();

    private ScheduledFuture<?> scheduledFuture;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 每隔 5 秒发送一次数据
        scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(() -> {
            try {
                // 获取话题名称和数量
                Map<String, Object> map = new HashMap<>();
                map.put("titleName", weblogService.queryWeblog().get("titleName"));
                map.put("titleCount", weblogService.queryWeblog().get("titleCount"));
                map.put("titleSum", weblogService.titleCount());

                // 将数据转为 JSON 字符串
                String jsonResponse = JSON.toJSONString(map);

                // 发送 JSON 数据
                session.sendMessage(new TextMessage(jsonResponse));

            } catch (IOException e) {
                e.printStackTrace();
                try {
                    session.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }, 0, 5, TimeUnit.SECONDS); // 初始延迟 0 秒，周期为 5 秒
    }

//    @Override
//    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
//        // 这里只是接收消息，如果你不需要处理消息，可以忽略
//        System.out.println("Received message from client: " + message.getPayload());
//    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {

        // 取消当前连接的定时任务
        if (scheduledFuture != null && !scheduledFuture.isCancelled()) {
            scheduledFuture.cancel(true);
        }
    }
}
