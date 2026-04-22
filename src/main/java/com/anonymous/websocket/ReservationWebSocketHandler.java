package com.anonymous.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ReservationWebSocketHandler extends TextWebSocketHandler {

    private final Map<Long, WebSocketSession> userSessionMap = new ConcurrentHashMap<>();

    private Long getUserId(WebSocketSession wsSession) {
        Object raw = wsSession.getAttributes().get("userId");
        if (raw instanceof Long userId) {
            return userId;
        }
        return null;
    }

    /**
     * 受到消息时处理消息
     * @param session
     * @param message
     * @throws Exception
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long userId = getUserId(session);
        if (userId == null) {
            return;
        }
        String payload = message.getPayload();

        if (session.isOpen()) {
            session.sendMessage(new TextMessage("服务端已经受到消息：" + payload));
        }
    }

    /**
     * 用户上线时登记在线
     * @param session
     * @throws Exception
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = getUserId(session);
        if (userId != null) {
            userSessionMap.put(userId, session);
        } else {
            session.close();
        }
    }

    /**
     * 用户断开时清理连接
     * @param session
     * @param status
     * @throws Exception
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long userId = getUserId(session);
        if (userId != null) {
            userSessionMap.remove(userId, session);
        }
    }

    public boolean sendToUser(Long userId, String text) {
        if (userId == null || text == null || text.isBlank()) {
            return false;
        }
        WebSocketSession session = userSessionMap.get(userId);

        if (session == null) {
            return false;
        }

        if (!session.isOpen()) {
            userSessionMap.remove(userId, session);
            return false;
        }
        try {
            session.sendMessage(new TextMessage(text));
            return true;
        } catch (Exception e) {
            userSessionMap.remove(userId, session);
            return false;
        }
    }
}
