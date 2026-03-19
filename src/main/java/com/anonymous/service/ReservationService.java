package com.anonymous.service;

import com.anonymous.common.Page;
import com.anonymous.model.Reservation;

import java.time.LocalDateTime;

public interface ReservationService {

    Long bookSeat(Long userId, Long seatId, LocalDateTime start, LocalDateTime end);

    boolean cancelReservation(Long userId, Long seatId);

    boolean checkIn(Long userId, Long seatId);

    boolean checkOut(Long userId);

    boolean leaveTemp(Long userId);

    boolean returnTemp(Long userId, Long seatId);

    void processTimeout(Long reservationId);

    Page<Reservation> getHistory(Long userId, int pageNum, int pageSize);

    Reservation getCurrent(Long userId);
}
