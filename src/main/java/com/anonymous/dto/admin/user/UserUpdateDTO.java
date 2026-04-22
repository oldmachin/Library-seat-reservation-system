package com.anonymous.dto.admin.user;

public record UserUpdateDTO(
        Long id,
        String password,
        Integer status
) {
}
