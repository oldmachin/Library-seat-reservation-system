package com.anonymous.dto.admin.room;

public record RoomCreateDTO(
        String name,
        Integer status,
        String layoutTemplate
) {
}
