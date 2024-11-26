package com.spark.service;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * @author mo
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 使用 TextWebSocketHandler 的实现类来处理 WebSocket 请求
        registry.addHandler(new WeblogSocketHandler(), "/websocket")
                .setAllowedOrigins("*");  // 允许所有来源
    }
}
