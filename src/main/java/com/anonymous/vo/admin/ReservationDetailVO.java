package com.anonymous.vo.admin;

import java.time.LocalDateTime;

public record ReservationDetailVO(
        Long id,

        Long userId,
        String userName,
        String username,
        Integer userStatus,
        String userRole,

        Long roomId,
        String roomName,
        Integer roomStatus,

        Long seatId,
        String seatCode,
        String seatType,
        Integer seatStatus,

        LocalDateTime startTime,
        LocalDateTime endTime,
        LocalDateTime actualStartTime,
        LocalDateTime actualEndTime,

        Integer status,
        String statusText,

        Integer version
) {
}
