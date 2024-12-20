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
            pythonClient = new WebSocketClient(new URI("ws://10.43.108.62:8765")) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    System.out.println("Connected to Python WebSocket");
                }

                @Override
                public void onMessage(String message) {
                    broadcastToVueSessions(message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println("Python WebSocket closed: " + reason);
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
        try {
            if (pythonClient != null && pythonClient.isOpen()) {
                pythonClient.send(question);
                System.out.println("Sent question to Python: " + question);
            } else {
                System.out.println("Python WebSocket is not open. Cannot send question.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void broadcastToVueSessions(String message) {
        for (WebSocketSession session : vueSessions) {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(message));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void addVueSession(WebSocketSession session) {
        vueSessions.add(session);
    }

    public void removeVueSession(WebSocketSession session) {
        vueSessions.remove(session);
    }
}
