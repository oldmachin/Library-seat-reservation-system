package com.anonymous.websocket;

import com.anonymous.model.Seat;

import java.util.List;

public record RoomSeatSnapshotMessage(
        String type,
        Long roomId,
        List<Seat> seats
) {
}
