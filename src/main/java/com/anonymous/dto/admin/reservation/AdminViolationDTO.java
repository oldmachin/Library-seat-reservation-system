package com.anonymous.dto.admin.reservation;

public record AdminViolationDTO(
        String reason,
        Integer penaltyLevel,
        Integer banDays
) {
}
