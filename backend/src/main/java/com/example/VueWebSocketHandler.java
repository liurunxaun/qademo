package com.example;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class VueWebSocketHandler extends TextWebSocketHandler {
    private final PythonWebSocketService pythonWebSocketService;

    public VueWebSocketHandler(PythonWebSocketService pythonWebSocketService) {
        this.pythonWebSocketService = pythonWebSocketService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        pythonWebSocketService.addVueSession(session);
        System.out.println("Vue WebSocket connection established");
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        System.out.println("Received message from Vue: " + message.getPayload());
        // 如果需要将消息转发给 Python WebSocket 服务端
        pythonWebSocketService.broadcastToVueSessions(message.getPayload());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        pythonWebSocketService.removeVueSession(session);
        System.out.println("Vue WebSocket connection closed");
    }
}


