package com.anonymous.vo;

import java.time.LocalDateTime;

public record ReservationAdminActionLogVO(
        Long id,
        Long reservationId,
        String actionType,
        Long operatorId,
        String operatorName,
        Long userId,
        String userName,
        String username,
        Long roomId,
        String roomName,
        Long seatId,
        String seatCode,
        String reason,
        Integer penaltyLevel,
        Integer banDays,
        LocalDateTime createTime
) {
}
