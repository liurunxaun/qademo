package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class VueWebSocketHandler extends TextWebSocketHandler {

    @Autowired
    private PythonWebSocketService pythonService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        pythonService.addVueSession(session);
        System.out.println("Vue WebSocket connected");
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        // 处理来自 Vue 的消息
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        pythonService.removeVueSession(session);
        System.out.println("Vue WebSocket disconnected");
    }
}

