package com.anonymous.vo;

public record SeatAvailabilityVO(
        Long id,
        Long roomId,
        String seatCode,
        Integer type,
        String status,
        String currentStatus,
        Boolean canBook,
        String maintenanceNote,
        Integer xAxis,
        Integer yAxis
) {
}
