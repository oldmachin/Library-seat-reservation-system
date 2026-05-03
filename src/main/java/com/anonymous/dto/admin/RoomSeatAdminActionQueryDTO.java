package com.anonymous.dto.admin;

public record RoomSeatAdminActionQueryDTO(
        Integer page,
        Integer size,
        String resourceType,
        String actionType,
        Long roomId,
        Long seatId,
        Long operatorId,
        String keyword
) {
}
