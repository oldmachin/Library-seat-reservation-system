package com.anonymous.dto;

public record ReservationTimeoutMessage(
        Long reservationId,
        String eventType,
        String referenceTime
) {
}
