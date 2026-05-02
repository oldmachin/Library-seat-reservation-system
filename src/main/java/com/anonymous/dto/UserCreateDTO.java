package com.anonymous.dto;

public record UserCreateDTO(
        String name,
        String username,
        String password,
        String role
) {
}
