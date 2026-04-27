package com.anonymous.vo;

import java.time.LocalDateTime;

public record ReputationRecordVO(
        Long id,
        Long reservationId,
        String eventType,
        Integer scoreDelta,
        Integer scoreAfter,
        String reason,
        LocalDateTime createTime
) {
}
