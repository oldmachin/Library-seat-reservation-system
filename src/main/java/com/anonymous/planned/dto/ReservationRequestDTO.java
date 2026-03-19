package com.anonymous.planned.dto;

public record ReservationRequestDTO(
        Long userId,
        Long seatId,
        String startTime,
        String endTime
) {
}
