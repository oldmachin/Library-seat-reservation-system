package com.anonymous.dto.admin.user;

public record UserQueryDTO(
        Integer page,
        Integer size,
        String username,
        String name,
        String role,
        Integer status
) {
}
