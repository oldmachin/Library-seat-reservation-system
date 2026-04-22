package com.anonymous.vo.admin;

public record RoomAdminVO(
        Long id,
        String name,
        Integer capacity,
        Integer status,
        String statusText
) {
}
