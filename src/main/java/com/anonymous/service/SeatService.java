package com.anonymous.service;

import com.anonymous.model.Seat;
import com.anonymous.model.enums.SeatStatus;

import java.util.List;

public interface SeatService {

    void updateSeatStatus(Long seatId, SeatStatus status);

    void markDefective(Long seatId, String reason);

    List<Seat> getSeatByRoom(Long roomId);

    boolean isSeatOperable(Long seatId);
}
