package com.anonymous.mq;

import com.anonymous.config.ReservationMqConstants;
import com.anonymous.dto.ReservationTimeoutMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class ReservationTimeoutProducer {

    private final static Logger log = LoggerFactory.getLogger(ReservationTimeoutProducer.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private byte[] toBody(ReservationTimeoutMessage payload) {
        try {
            return objectMapper.writeValueAsBytes(payload);
        } catch (Exception e) {
            throw new RuntimeException("序列化超时消息失败", e);
        }
    }

    public void sendCheckInTimeoutMessage(ReservationTimeoutMessage payload, long delayMillis) {
        byte[] body = toBody(payload);

        Message message = MessageBuilder.withBody(body)
                .setContentType("application/json")
                .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                .setExpiration(String.valueOf(Math.max(delayMillis, 1000)))
                .build();

        log.info("发送签到超时消息 reservationId={}, delayMillis={}", payload.reservationId(), delayMillis);
        rabbitTemplate.send(
                ReservationMqConstants.DELAY_EXCHANGE,
                ReservationMqConstants.CHECKIN_DELAY_ROUTING_KEY,
                message
        );
    }

    public void sendTempLeaveTimeoutMessage(ReservationTimeoutMessage payload) {
        byte[] body = toBody(payload);

        Message message = MessageBuilder.withBody(body)
                .setContentType("application/json")
                .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                .build();

        log.info("发送暂离超时消息 reservationId={}, referenceTime={}", payload.reservationId(), payload.referenceTime());
        rabbitTemplate.send(
                ReservationMqConstants.DELAY_EXCHANGE,
                ReservationMqConstants.LEAVE_DELAY_ROUTING_KEY,
                message
        );
    }
}
