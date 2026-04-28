package com.anonymous.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ReservationWebSocketHandler extends TextWebSocketHandler {

    private final Map<Long, Set<WebSocketSession>> userSessionMap = new ConcurrentHashMap<>();

    private final Map<Long, Set<WebSocketSession>> roomSubscribers = new ConcurrentHashMap<>();

    private final Map<String, Long> sessionRoomMap = new ConcurrentHashMap<>();

    private Long getUserId(WebSocketSession session) {
        Object raw = session.getAttributes().get("userId");
        if (raw instanceof Long userId) {
            return userId;
        }
        return null;
    }

    private void registerUserSession(Long userId, WebSocketSession session) {
        if (userId == null || session == null) {
            return;
        }
        Set<WebSocketSession> sessions = userSessionMap.computeIfAbsent(userId, key -> ConcurrentHashMap.newKeySet());
        sessions.add(session);
    }

    private void cleanupSession(WebSocketSession session) {
        if (session == null) {
            return;
        }

        Long userId = getUserId(session);
        if (userId != null) {
            Set<WebSocketSession> sessions = userSessionMap.get(userId);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    userSessionMap.remove(userId);
                }
            }
        }

        Long roomId = sessionRoomMap.remove(session.getId());
        if (roomId != null) {
            Set<WebSocketSession> subscribers = roomSubscribers.get(roomId);
            if (subscribers != null) {
                subscribers.remove(session);
                if (subscribers.isEmpty()) {
                    roomSubscribers.remove(roomId);
                }
            }
        }
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
            session.close();
            return;
        }
        String payload = message.getPayload();
        String text = (payload == null) ? "" : payload.trim();

        if (text.isBlank()) {
            session.sendMessage(new TextMessage("服务端已经受到消息：" + payload + "，但是服务未被受理"));
            return;
        }
        Long roomId;
        try {
            roomId = Long.valueOf(text);
        } catch(NumberFormatException e) {
            session.sendMessage(new TextMessage("订阅失败：roomId格式错误"));
            return;
        }
        Long oldRoomId = sessionRoomMap.get(session.getId());
        if (oldRoomId != null) {
            Set<WebSocketSession> oldSubscribers = roomSubscribers.get(oldRoomId);
            if (oldSubscribers != null) {
                oldSubscribers.remove(session);
                if (oldSubscribers.isEmpty()) {
                    roomSubscribers.remove(oldRoomId);
                }
            }
        }
        Set<WebSocketSession> subscribers = roomSubscribers.computeIfAbsent(roomId, key -> ConcurrentHashMap.newKeySet());
        subscribers.add(session);
        sessionRoomMap.put(session.getId(), roomId);
        session.sendMessage(new TextMessage("订阅房间成功：" + roomId));
    }

    /**
     * 用户上线时登记在线
     * @param session
     * @throws Exception
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = getUserId(session);
        if (userId == null) {
            session.close();
            return;
        }
        registerUserSession(userId, session);
    }

    /**
     * 用户断开时清理连接
     * @param session
     * @param status
     * @throws Exception
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        cleanupSession(session);
    }

    public boolean sendToUser(Long userId, String text) {
        if (userId == null || text == null || text.isBlank()) {
            return false;
        }
        Set<WebSocketSession> sessions = userSessionMap.get(userId);

        if (sessions == null || sessions.isEmpty()) {
            return false;
        }

        Set<WebSocketSession> cancelSessions = new HashSet<>();
        boolean sent = false;

        for (WebSocketSession session : sessions) {
            if (!session.isOpen()) {
                cancelSessions.add(session);
            } else {
                try {
                    session.sendMessage(new TextMessage(text));
                    sent = true;
                } catch (Exception e) {
                    cancelSessions.add(session);
                }
            }
        }

        for (WebSocketSession session : cancelSessions) {
            cleanupSession(session);
        }
        return sent;
    }

    public boolean broadcastToRoom(Long roomId, String text) {
        if (roomId == null || text == null || text.isBlank()) {
            return false;
        }

        Set<WebSocketSession> subscribers = roomSubscribers.get(roomId);
        if (subscribers == null || subscribers.isEmpty()) {
            return false;
        }
        Set<WebSocketSession> cancelSubscribers = new HashSet<>();
        boolean isSent = false;

        for (WebSocketSession session: subscribers) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage((text)));
                    isSent = true;
                } catch (Exception e) {
                    cancelSubscribers.add(session);
                }
            } else {
                cancelSubscribers.add(session);
            }
        }

        subscribers.removeAll(cancelSubscribers);
        if (subscribers.isEmpty()) {
            roomSubscribers.remove(roomId);
        }
        return isSent;
    }

    public boolean closeUserSessions(Long userId, String reason) {
        if (userId == null) {
            return false;
        }

        Set<WebSocketSession> sessions = userSessionMap.get(userId);
        if (sessions == null || sessions.isEmpty()) {
            return false;
        }

        Set<WebSocketSession> snapshot = new HashSet<>(sessions);
        boolean closed = false;

        for (WebSocketSession session : snapshot) {
            try {
                if (session.isOpen()) {
                    session.close(new CloseStatus(4003, reason == null || reason.isBlank() ? "账号已被禁用" : reason));
                }
            } catch (Exception ignored) {
            } finally {
                cleanupSession(session);
                closed = true;
            }
        }

        return closed;
    }
}
