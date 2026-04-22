package com.anonymous.dto.admin.room;

public record RoomQueryDTO(
        Integer page,
        Integer size,
        Long id,
        String name,
        Integer capacity,
        Integer status
) {
}
