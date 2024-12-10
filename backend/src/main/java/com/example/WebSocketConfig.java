package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final VueWebSocketHandler vueWebSocketHandler;

    @Autowired
    public WebSocketConfig(VueWebSocketHandler vueWebSocketHandler) {
        this.vueWebSocketHandler = vueWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(vueWebSocketHandler, "/ws").setAllowedOrigins("*");
    }
}