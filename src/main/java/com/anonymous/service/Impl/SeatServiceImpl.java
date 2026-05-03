package com.anonymous.service.Impl;

import com.anonymous.common.TimeSlot;
import com.anonymous.common.exception.InvalidOperationStatusException;
import com.anonymous.common.exception.InvalidParameterException;
import com.anonymous.common.util.ReservationTimeValidator;
import com.anonymous.common.util.SecurityUtils;
import com.anonymous.mapper.ReservationSlotMapper;
import com.anonymous.mapper.RoomSeatAdminActionMapper;
import com.anonymous.mapper.SeatMapper;
import com.anonymous.model.RoomSeatAdminAction;
import com.anonymous.model.Seat;
import com.anonymous.model.enums.AdminResourceType;
import com.anonymous.model.enums.RoomSeatAdminActionType;
import com.anonymous.model.enums.SeatStatus;
import com.anonymous.service.RoomSeatBroadcastService;
import com.anonymous.service.SeatService;
import com.anonymous.vo.SeatAvailabilityVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Autowired
    private RoomSeatAdminActionMapper roomSeatAdminActionMapper;

    @Override
    public void updateSeatStatus(Long seatId, SeatStatus status) {
        seatMapper.updateStatus(seatId, status.getCode());
    }

    private void recordSeatAction(RoomSeatAdminActionType actionType,
                                  Seat before,
                                  Integer afterStatus,
                                  String afterNote,
                                  String reason) {
        RoomSeatAdminAction action = new RoomSeatAdminAction();
        action.setResourceType(AdminResourceType.SEAT.name());
        action.setActionType(actionType.name());
        action.setOperatorId(SecurityUtils.getCurrentUserId());

        action.setRoomId(before.getRoomId());
        action.setSeatId(before.getId());

        action.setBeforeStatus(before.getStatus() == null ? null : before.getStatus().getCode());
        action.setAfterStatus(afterStatus);

        action.setBeforeNote(before.getMaintenanceNote());
        action.setAfterNote(afterNote);

        action.setReason(reason == null ? "" : reason.trim());

        try {
            action.setOperatorId(SecurityUtils.getCurrentUserId());
        } catch (RuntimeException e) {
            action.setOperatorId(null);
        }

        roomSeatAdminActionMapper.insert(action);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markDefective(Long seatId, String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new InvalidParameterException("seat.reason");
        }

        reason = reason.trim();

        Seat seat = seatMapper.findById(seatId);

        if (seat == null) {
            throw new InvalidParameterException("seat.id");
        }

        if (seat.getStatus() == null) {
            throw new InvalidParameterException("seat.status");
        }

        if (seat.getStatus() == SeatStatus.UNAVAILABLE) {
            throw new InvalidOperationStatusException("该座位已被标记损坏");
        }

        isSeatOperable(seatId);

        seatMapper.updateStatusAndNote(seatId, SeatStatus.UNAVAILABLE.getCode(), reason);
        roomSeatBroadcastService.broadcastRoomSnapshot(seat.getRoomId());
    }

    @Override
    public boolean isSeatOperable(Long seatId) {
        Seat seat = seatMapper.findById(seatId);

        if (seat.getStatus() == SeatStatus.OCCUPIED || seat.getStatus() == SeatStatus.RESERVED || seat.getStatus() == SeatStatus.AWAY) {
            throw new InvalidOperationStatusException("该座位正在使用中，请先由管理员强制释放后再标记损坏");
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateSeatStatusByAdmin(Long seatId, Integer status, String maintenanceNote) {
        if (seatId == null) {
            throw new InvalidParameterException("seat.id");
        }
        if (status == null) {
            throw new InvalidParameterException("seat.status");
        }
        if (status != SeatStatus.AVAILABLE.getCode() && status != SeatStatus.UNAVAILABLE.getCode()) {
            throw new InvalidOperationStatusException("管理员只能设置座位为可用或不可用");
        }

        Seat seat = seatMapper.findById(seatId);
        if (seat == null) {
            throw new InvalidParameterException("seat.id");
        }

        if (seat.getStatus() == SeatStatus.RESERVED
                || seat.getStatus() == SeatStatus.OCCUPIED
                || seat.getStatus() == SeatStatus.AWAY) {
            throw new InvalidOperationStatusException("座位正在预约或使用中，不能直接修改状态");
        }

        String note = status == SeatStatus.UNAVAILABLE.getCode()
                ? (maintenanceNote == null ? "" : maintenanceNote.trim())
                : "";

        int rows = seatMapper.updateStatusAndNote(seatId, status, note);
        if (rows > 0) {
            roomSeatBroadcastService.broadcastRoomSnapshot(seat.getRoomId());
        }

        recordSeatAction(
                RoomSeatAdminActionType.SEAT_STATUS_UPDATED,
                seat,
                status,
                note,
                status == SeatStatus.UNAVAILABLE.getCode() ? "管理员设为不可用" : "管理员恢复可用"
        );

        return rows > 0;
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
