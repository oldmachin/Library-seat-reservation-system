package com.anonymous.vo;

import java.time.LocalDateTime;

public record RoomSeatAdminActionLogVO(
        Long id,
        String resourceType,
        String actionType,
        Long operatorId,
        String operatorName,
        Long roomId,
        String roomName,
        Long seatId,
        String seatCode,
        Integer beforeStatus,
        Integer afterStatus,
        String beforeName,
        String afterName,
        Integer beforeCapacity,
        Integer afterCapacity,
        String beforeTemplate,
        String afterTemplate,
        String beforeNote,
        String afterNote,
        String reason,
        LocalDateTime createTime
) {
}
