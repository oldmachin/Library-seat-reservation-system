package com.anonymous.vo;

import java.time.LocalDateTime;

public record ReservationAdminActionVO(
        Long id,
        String actionType,
        Long operatorId,
        String reason,
        Integer penaltyLevel,
        Integer banDays,
        LocalDateTime createTime
) {
}
