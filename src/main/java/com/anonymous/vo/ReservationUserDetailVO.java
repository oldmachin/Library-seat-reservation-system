package com.anonymous.vo;

import java.time.LocalDateTime;
import java.util.List;

public record ReservationUserDetailVO(
        Long id,
        Long roomId,
        String roomName,
        Long seatId,
        String seatCode,
        String seatType,
        Integer seatStatus,
        LocalDateTime startTime,
        LocalDateTime endTime,
        LocalDateTime actualStartTime,
        LocalDateTime actualEndTime,
        LocalDateTime tempLeaveStartTime,
        Integer status,
        String textStatus,
        Integer version,
        List<ReservationAdminActionVO> adminActions
) {
}
