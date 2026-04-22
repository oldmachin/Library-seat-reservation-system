package com.anonymous.vo.admin;

public record UserAdminVO(
        Long id,
        String name,
        String username,
        String role,
        Integer status,
        String statusText
) {
}
