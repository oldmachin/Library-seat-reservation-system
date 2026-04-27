package com.anonymous.service.Impl;

import com.anonymous.common.TimeSlot;
import com.anonymous.common.util.ReservationTimeValidator;
import com.anonymous.mapper.ReservationSlotMapper;
import com.anonymous.mapper.SeatMapper;
import com.anonymous.model.Seat;
import com.anonymous.model.enums.SeatStatus;
import com.anonymous.service.RoomSeatBroadcastService;
import com.anonymous.service.SeatService;
import com.anonymous.vo.SeatAvailabilityVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class SeatServiceImpl implements SeatService {

    @Autowired
    private SeatMapper seatMapper;

    @Autowired
    private RoomSeatBroadcastService roomSeatBroadcastService;

    @Autowired
    private ReservationSlotMapper reservationSlotMapper;

    @Override
    public void updateSeatStatus(Long seatId, SeatStatus status) {
        seatMapper.updateStatus(seatId, status.getCode());
    }

    @Override
    public void markDefective(Long seatId, String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new RuntimeException("损坏原因不能为空");
        }

        reason = reason.trim();

        Seat seat = seatMapper.findById(seatId);

        if (seat == null) {
            throw new RuntimeException("座位不存在");
        }

        if (seat.getStatus() == null) {
            throw new RuntimeException("座位状态异常");
        }

        if (seat.getStatus() == SeatStatus.UNAVAILABLE) {
            throw new RuntimeException("该座位已被标记损坏");
        }

        isSeatOperable(seatId);

        seatMapper.updateStatusAndNote(seatId, SeatStatus.UNAVAILABLE.getCode(), reason);
        roomSeatBroadcastService.broadcastRoomSnapshot(seat.getRoomId());
    }

    @Override
    public boolean isSeatOperable(Long seatId) {
        Seat seat = seatMapper.findById(seatId);

        if (seat.getStatus() == SeatStatus.OCCUPIED || seat.getStatus() == SeatStatus.RESERVED || seat.getStatus() == SeatStatus.AWAY) {
            throw new RuntimeException("该座位正在使用中，请先由管理员强制释放后再标记损坏");
        }

        return seat.getStatus() != SeatStatus.UNAVAILABLE;
    }

    @Override
    public List<Seat> getSeatByRoom(Long roomId) {
        return seatMapper.findByRoomId(roomId);
    }

    @Override
    public List<SeatAvailabilityVO> getSeatAvailabilityByRoom(Long roomId, LocalDateTime start, LocalDateTime end) {
        ReservationTimeValidator.validateBookTimeRange(start, end);
        List<TimeSlot> matchedSlots = ReservationTimeValidator.resolveContinuousSlots(start, end);

        List<Seat> seats = seatMapper.findByRoomId(roomId);
        if (seats.isEmpty()) {
            return List.of();
        }

        List<String> slotCodes = matchedSlots.stream().map(TimeSlot::getCode).toList();
        Set<Long> occupiedSeatIds = new HashSet<>(
                reservationSlotMapper.findOccupiedSeatIdsByRoomAndDateAndSlots(
                        roomId,
                        start.toLocalDate(),
                        slotCodes
                )
        );

        return seats.stream().map(seat -> {
            String currentStatus = toSeatVisualStatus(seat.getStatus());
            boolean unavailable = seat.getStatus() == SeatStatus.UNAVAILABLE;
            boolean reserved = occupiedSeatIds.contains(seat.getId());

            String status = unavailable ? "unavailable" : (reserved ? "reserved" : "available");
            boolean canBook = !unavailable && !reserved;

            return new SeatAvailabilityVO(
                    seat.getId(),
                    seat.getRoomId(),
                    seat.getSeatCode(),
                    seat.getType(),
                    status,
                    currentStatus,
                    canBook,
                    seat.getMaintenanceNote(),
                    seat.getXAxis(),
                    seat.getYAxis()
            );
        }).toList();
    }

    private String toSeatVisualStatus(SeatStatus status) {
        if (status == null) {
            return "unavailable";
        }
        return switch (status) {
            case AVAILABLE -> "available";
            case RESERVED -> "reserved";
            case OCCUPIED -> "occupied";
            case AWAY -> "away";
            case UNAVAILABLE -> "unavailable";
        };
    }

}
