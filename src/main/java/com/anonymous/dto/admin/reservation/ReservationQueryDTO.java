package com.anonymous.dto.admin.reservation;

import java.time.LocalDateTime;

public record ReservationQueryDTO(
        Integer page,
        Integer size,
        Integer status,
        Long userId,
        Long roomId,
        Long seatId,
        String username,
        String seatCode,
        LocalDateTime startDate,
        LocalDateTime endDate
) {
}
