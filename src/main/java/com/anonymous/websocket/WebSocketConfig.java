package com.anonymous.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;


/** 处理WebSocket服务的类 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final ReservationWebSocketHandler reservationWebSocketHandler;

    private final WebSocketAuthInterceptor webSocketAuthInterceptor;

    public WebSocketConfig(ReservationWebSocketHandler reservationWebSocketHandler, WebSocketAuthInterceptor webSocketAuthInterceptor) {
        this.reservationWebSocketHandler = reservationWebSocketHandler;
        this.webSocketAuthInterceptor = webSocketAuthInterceptor;
    }

    /**
     *
     * */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(reservationWebSocketHandler, "/ws/reservation")
                .addInterceptors(webSocketAuthInterceptor)
                .setAllowedOrigins("*");
    }
}
