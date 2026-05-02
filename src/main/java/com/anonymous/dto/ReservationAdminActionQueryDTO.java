package com.anonymous.dto;

public record ReservationAdminActionQueryDTO(
        Integer page,
        Integer size,
        String actionType,
        Long reservationId,
        Long operatorId,
        String username
) {
}
