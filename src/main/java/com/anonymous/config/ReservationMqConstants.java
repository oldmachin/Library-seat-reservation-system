package com.anonymous.config;

public final class ReservationMqConstants {

    private ReservationMqConstants() {

    }

    public final static String DELAY_EXCHANGE = "reservation.delay.exchange";
    public final static String TIMEOUT_EXCHANGE = "reservation.timeout.exchange";

    public static final String CHECKIN_DELAY_QUEUE = "reservation.checkin.delay.queue";
    public static final String LEAVE_DELAY_QUEUE = "reservation.leave.delay.queue";

    public static final String CHECKIN_TIMEOUT_QUEUE = "reservation.timeout.checkin.queue";
    public static final String LEAVE_TIMEOUT_QUEUE = "reservation.timeout.leave.queue";

    public static final String CHECKIN_DELAY_ROUTING_KEY = "reservation.delay.checkin";
    public static final String LEAVE_DELAY_ROUTING_KEY = "reservation.delay.leave";

    public static final String CHECKIN_TIMEOUT_ROUTING_KEY = "reservation.timeout.checkin";
    public static final String LEAVE_TIMEOUT_ROUTING_KEY = "reservation.timeout.leave";

    public static final int TEMP_LEAVE_TIMEOUT_MILLIS = 30 * 60 * 1000; // 30min
}
