package com.anonymous.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class ReservationRabbitConfig {

    @Bean
    public DirectExchange reservationDelayExchange() {
        return new DirectExchange(ReservationMqConstants.DELAY_EXCHANGE);
    }

    @Bean
    public DirectExchange reservationTimeoutExchange() {
        return new DirectExchange(ReservationMqConstants.TIMEOUT_EXCHANGE);
    }

    @Bean
    public Queue checkInDelayQueue() {
        return QueueBuilder.durable(ReservationMqConstants.CHECKIN_DELAY_QUEUE)
                .withArguments(Map.of(
                        "x-dead-letter-exchange", ReservationMqConstants.TIMEOUT_EXCHANGE,
                        "x-dead-letter-routing-key", ReservationMqConstants.CHECKIN_TIMEOUT_ROUTING_KEY
                ))
                .build();
    }

    @Bean
    public Queue tempLeaveDelayQueue() {
        return QueueBuilder.durable(ReservationMqConstants.LEAVE_DELAY_QUEUE)
                .withArguments(Map.of(
                        "x-message-ttl", ReservationMqConstants.TEMP_LEAVE_TIMEOUT_MILLIS,
                        "x-dead-letter-exchange", ReservationMqConstants.TIMEOUT_EXCHANGE,
                        "x-dead-letter-routing-key", ReservationMqConstants.LEAVE_TIMEOUT_ROUTING_KEY
                ))
                .build();
    }

    @Bean
    public Queue checkInTimeoutQueue() {
        return QueueBuilder.durable(ReservationMqConstants.CHECKIN_TIMEOUT_QUEUE).build();
    }

    @Bean
    public Queue tempLeaveTimeoutQueue() {
        return QueueBuilder.durable(ReservationMqConstants.LEAVE_TIMEOUT_QUEUE).build();
    }

    @Bean
    public Binding checkInDelayBinding() {
        return BindingBuilder.bind(checkInDelayQueue())
                .to(reservationDelayExchange())
                .with(ReservationMqConstants.CHECKIN_DELAY_ROUTING_KEY);
    }

    @Bean
    public Binding tempLeaveDelayBinding() {
        return BindingBuilder.bind(tempLeaveDelayQueue())
                .to(reservationDelayExchange())
                .with(ReservationMqConstants.LEAVE_DELAY_ROUTING_KEY);
    }

    @Bean
    public Binding checkInTimeoutBinding() {
        return BindingBuilder.bind(checkInTimeoutQueue())
                .to(reservationTimeoutExchange())
                .with(ReservationMqConstants.CHECKIN_TIMEOUT_ROUTING_KEY);
    }

    @Bean
    public Binding tempLeaveTimeoutBinding() {
        return BindingBuilder.bind(tempLeaveTimeoutQueue())
                .to(reservationTimeoutExchange())
                .with(ReservationMqConstants.LEAVE_TIMEOUT_ROUTING_KEY);
    }
}
