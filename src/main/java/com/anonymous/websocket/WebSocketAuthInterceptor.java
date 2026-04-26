package com.anonymous.websocket;

import com.anonymous.common.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private final static String AUTH_USER_PREFIX = "auth:user:";

    /**
     * 在建立WebSocket对话之前，尝试解析用户的userId
     *
     * @param request: 握手阶段的HTTP请求
     * @param response: 握手响应
     * @param wsHandler: 后面真正处理消息的Handler
     * @param attributes: 临时表，后续存入WebSocketSession
     * @return true 允许进行后续握手， false 拒绝此次握手
     * @throws Exception 在什么情况下抛出该异常
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        if (!(request instanceof ServletServerHttpRequest servletServerHttpRequest)) {
            return false;
        }
        HttpServletRequest httpServletRequest = servletServerHttpRequest.getServletRequest();
        String token = httpServletRequest.getParameter("token");
        if (token == null || token.isBlank()) {
            return false;
        }

        try {
            String subject = jwtUtil.getSubject(token);
            if (subject == null || subject.isBlank()) {
                return false;
            }
            Long userId = Long.valueOf(subject);

            String cachedToken = redisTemplate.opsForValue().get(AUTH_USER_PREFIX + userId);
            if (cachedToken == null || !cachedToken.equals(token)) {
                return false;
            }
            attributes.put("userId", userId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, @Nullable Exception exception) {

    }
}
