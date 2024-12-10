package com.example;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.net.URI;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class PythonWebSocketService {
    private WebSocketClient pythonClient;
    private CopyOnWriteArrayList<WebSocketSession> vueSessions = new CopyOnWriteArrayList<>();

    public PythonWebSocketService() {
        try {
            pythonClient = new WebSocketClient(new URI("ws://localhost:8765")) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    System.out.println("Connected to Python server");
                }

                @Override
                public void onMessage(String message) {
                    // 转发给 Vue 客户端
                    for (WebSocketSession session : vueSessions) {
                        try {
                            session.sendMessage(new TextMessage(message));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println("Connection closed with Python: " + reason);
                }

                @Override
                public void onError(Exception ex) {
                    ex.printStackTrace();
                }
            };
            pythonClient.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendQuestionToPython(String question) {
        if (pythonClient != null && pythonClient.isOpen()) {
            pythonClient.send(question);
        }
    }

    public void addVueSession(WebSocketSession session) {
        vueSessions.add(session);
    }

    public void removeVueSession(WebSocketSession session) {
        vueSessions.remove(session);
    }
}
