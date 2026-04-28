package com.anonymous.mq;

import com.anonymous.config.ReservationMqConstants;
import com.anonymous.dto.ReservationTimeoutMessage;
import com.anonymous.model.enums.ReservationTimeoutEventType;
import com.anonymous.service.ReservationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

@Component
public class ReservationTimeoutConsumer {

    private final static Logger log = LoggerFactory.getLogger(ReservationTimeoutProducer.class);

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ObjectMapper objectMapper;

    private ReservationTimeoutMessage readMessage(byte[] body) {
        try {
            return objectMapper.readValue(body, ReservationTimeoutMessage.class);
        } catch (Exception e) {
            throw new RuntimeException("反序列化预约超时消息失败", e);
        }
    }

    @RabbitListener(queues = ReservationMqConstants.CHECKIN_TIMEOUT_QUEUE)
    public void onCheckInTimeOut(byte[] body) {
        ReservationTimeoutMessage message = readMessage(body);
        if (!ReservationTimeoutEventType.CHECK_IN_TIMEOUT.name().equals(message.eventType())) {
            return;
        }

        log.info("收到签到超时消息 reservationId={}", message.reservationId());
        reservationService.processTimeout(message.reservationId());
    }

    @RabbitListener(queues = ReservationMqConstants.LEAVE_TIMEOUT_QUEUE)
    public void onTempLeaveTimeout(byte[] body) {
        ReservationTimeoutMessage message = readMessage(body);
        if (!ReservationTimeoutEventType.TEMP_LEAVE_TIMEOUT.name().equals(message.eventType())) {
            return;
        }

       log.info("收到暂离超时消息 reservationId={}, referenceTime={}", message.reservationId(), message.referenceTime());
        LocalDateTime expectedTempLeaveStartTime = LocalDateTime.parse(message.referenceTime());
        reservationService.processTempLeaveTimeout(message.reservationId(), expectedTempLeaveStartTime);
    }
}
