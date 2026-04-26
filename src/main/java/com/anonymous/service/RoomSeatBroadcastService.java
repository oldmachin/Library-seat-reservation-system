package com.anonymous.service;

import com.anonymous.mapper.SeatMapper;
import com.anonymous.websocket.RoomSeatSnapshotMessage;
import com.anonymous.model.Seat;
import com.anonymous.websocket.ReservationWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Service
public class RoomSeatBroadcastService {

    @Autowired
    private SeatMapper seatMapper;

    @Autowired
    private ReservationWebSocketHandler reservationWebSocketHandler;

    @Autowired
    private ObjectMapper objectMapper;


    public boolean broadcastRoomSnapshot(Long roomId) {
        if (roomId == null) {
            return false;
        }

        try {
            List<Seat> seats = seatMapper.findByRoomId(roomId);

            RoomSeatSnapshotMessage message = new RoomSeatSnapshotMessage(
                    "room_seat_snapshot",
                    roomId,
                    seats
            );

            String json = objectMapper.writeValueAsString(message);
            return reservationWebSocketHandler.broadcastToRoom(roomId, json);
        } catch (Exception e) {
            return false;
        }
    }
}
