package com.anonymous.vo;

public record QuickReservationResultVO(
        Long reservationId,
        Long roomId,
        String roomName,
        Long seatId,
        String seatCode
) {
}
