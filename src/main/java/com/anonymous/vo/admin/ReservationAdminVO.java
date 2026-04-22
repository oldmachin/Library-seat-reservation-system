package com.anonymous.vo.admin;

import java.time.LocalDateTime;

public record ReservationAdminVO(
        Long id,
        Long userId,
        String userName,
        String username,
        Long roomId,
        String roomName,
        Long seatId,
        String seatCode,
        Integer status,
        String statusText,
        LocalDateTime startTime,
        LocalDateTime endTime,
        LocalDateTime actualStartTime,
        LocalDateTime actualEndTime
) {
}
