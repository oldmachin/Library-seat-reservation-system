package com.anonymous.websocket;

import com.anonymous.common.util.JwtUtil;
import com.anonymous.common.util.ResultResponseWriter;
import com.anonymous.mapper.UserMapper;
import com.anonymous.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    private final static String AUTH_USER_PREFIX = "auth:user:";

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private UserMapper userMapper;

    private boolean reject(ServerHttpResponse response, HttpStatus status, int code, String message) {
        response.setStatusCode(status);

        if (response instanceof ServletServerHttpResponse servletResponse) {
            HttpServletResponse rawResponse = servletResponse.getServletResponse();
            try {
                ResultResponseWriter.write(rawResponse, status.value(), code, message);
            } catch (Exception ignored) {
            }
        } else {
            response.getHeaders().add("X-Error-Code", String.valueOf(code));
            response.getHeaders().add("X-Error-Message", message);
        }

        return false;
    }


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
            return reject(response, HttpStatus.BAD_REQUEST, 400, "无效的WebSocket请求");
        }
        HttpServletRequest httpServletRequest = servletServerHttpRequest.getServletRequest();
        String token = httpServletRequest.getParameter("token");
        if (token == null || token.isBlank()) {
            return reject(response, HttpStatus.UNAUTHORIZED, 401, "未提供认证token");
        }

        try {
            String subject = jwtUtil.getSubject(token);
            if (subject == null || subject.isBlank()) {
                return reject(response, HttpStatus.UNAUTHORIZED, 401, "token无效或已过期");
            }
            Long userId = Long.valueOf(subject);

            String cachedToken = redisTemplate.opsForValue().get(AUTH_USER_PREFIX + userId);
            if (cachedToken == null || !cachedToken.equals(token)) {
                return reject(response, HttpStatus.UNAUTHORIZED, 401, "会话已失效或已在别处登录");
            }

            User user = userMapper.findById(userId);
            if (user == null) {
                redisTemplate.delete(AUTH_USER_PREFIX + userId);
                return reject(response, HttpStatus.UNAUTHORIZED, 401, "用户不存在");
            }
            if (user.getStatus() == null || user.getStatus() != 0) {
                redisTemplate.delete(AUTH_USER_PREFIX + userId);
                return reject(response, HttpStatus.FORBIDDEN, 403, "账号已被禁用，请联系管理员");
            }
            attributes.put("userId", userId);
            return true;
        } catch (Exception e) {
            return reject(response, HttpStatus.UNAUTHORIZED, 401, "WebSocket认证失败");
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, @Nullable Exception exception) {

    }
}
