package com.anonymous.service.Impl;

import com.anonymous.common.Page;
import com.anonymous.common.exception.InvalidOperationStatusException;
import com.anonymous.common.exception.InvalidParameterException;
import com.anonymous.common.util.ReservationStatusValidator;
import com.anonymous.dto.ReservationAdminActionQueryDTO;
import com.anonymous.dto.admin.reservation.ReservationQueryDTO;
import com.anonymous.mapper.*;
import com.anonymous.model.*;
import com.anonymous.model.enums.ReservationAdminActionType;
import com.anonymous.model.enums.ReservationStatus;
import com.anonymous.model.enums.SeatStatus;
import com.anonymous.service.AdminReservationService;
import com.anonymous.service.ReputationService;
import com.anonymous.service.RoomSeatBroadcastService;
import com.anonymous.vo.ReservationAdminActionLogVO;
import com.anonymous.vo.admin.ReservationAdminVO;
import com.anonymous.vo.admin.ReservationDetailVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminReservationServiceImpl implements AdminReservationService {

    @Autowired
    private ReservationMapper reservationMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private SeatMapper seatMapper;

    @Autowired
    private RoomSeatBroadcastService roomSeatBroadcastService;

    @Autowired
    private ReservationSlotMapper reservationSlotMapper;

    @Autowired
    private ReservationAdminActionMapper reservationAdminActionMapper;

    @Autowired
    private ReputationService reputationService;

    private void recordAdminAction(Long reservationId,
                                   ReservationAdminActionType actionType,
                                   String reason,
                                   Integer penaltyLevel,
                                   Integer banDays) {
        ReservationAdminAction action = new ReservationAdminAction();
        action.setReservationId(reservationId);
        action.setActionType(actionType);
        action.setReason(reason == null ? "" : reason.trim());
        action.setPenaltyLevel(penaltyLevel);
        action.setBanDays(banDays);

        try {
            action.setOperatorId(com.anonymous.common.util.SecurityUtils.getCurrentUserId());
        } catch (RuntimeException e) {
            action.setOperatorId(null);
        }

        reservationAdminActionMapper.insert(action);
    }


    @Override
    public Page<ReservationAdminVO> listReservations(ReservationQueryDTO queryDTO) {
        Integer pageNum = queryDTO.page();
        Integer pageSize = queryDTO.size();

        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }
        if (pageSize > 100) {
            pageSize = 100;
        }

        int offset = (pageNum - 1) * pageSize;

        List<ReservationAdminVO> rawRecords = reservationMapper.findReservationsByCondition(queryDTO, offset, pageSize);
        List<ReservationAdminVO> records = rawRecords.stream()
                .map(vo -> new ReservationAdminVO(
                        vo.id(),
                        vo.userId(),
                        vo.userName(),
                        vo.username(),
                        vo.roomId(),
                        vo.roomName(),
                        vo.seatId(),
                        vo.seatCode(),
                        vo.status(),
                        convertStatusText(vo.status()),
                        vo.startTime(),
                        vo.endTime(),
                        vo.actualStartTime(),
                        vo.actualEndTime()
                ))
                .toList();

        long total = reservationMapper.countReservationsByCondition(queryDTO);

        return new Page<>(records, total, pageNum, pageSize);
    }

    @Override
    public Page<ReservationAdminVO> findCurrent(ReservationQueryDTO queryDTO) {

        Integer pageNum = queryDTO.page();
        Integer pageSize = queryDTO.size();

        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }
        if (pageSize > 100) {
            pageSize = 100;
        }

        int offset = (pageNum - 1) * pageSize;

        List<ReservationAdminVO> rawRecords = reservationMapper.findAllCurrent(offset, pageSize);
        List<ReservationAdminVO> records = rawRecords.stream()
                .map(vo -> new ReservationAdminVO(
                        vo.id(),
                        vo.userId(),
                        vo.userName(),
                        vo.username(),
                        vo.roomId(),
                        vo.roomName(),
                        vo.seatId(),
                        vo.seatCode(),
                        vo.status(),
                        convertStatusText(vo.status()),
                        vo.startTime(),
                        vo.endTime(),
                        vo.actualStartTime(),
                        vo.actualEndTime()
                ))
                .toList();

        long total = reservationMapper.countReservationsCurrent();

        return new Page<>(records, total, pageNum, pageSize);
    }

    @Override
    public Page<ReservationAdminActionLogVO> listReservationActions(ReservationAdminActionQueryDTO query) {
        Integer pageNum = query.page();
        Integer pageSize = query.size();

        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }
        if (pageSize > 100) {
            pageSize = 100;
        }

        int offset = (pageNum - 1) * pageSize;

        List<ReservationAdminActionLogVO> records =
                reservationAdminActionMapper.findPage(query, offset, pageSize);
        long total = reservationAdminActionMapper.countPage(query);

        return new Page<>(records, total, pageNum, pageSize);
    }

    private String convertStatusText(Integer status) {
        return switch (status) {
            case 0 -> ReservationStatus.PENDING.getDescription();
            case 1 -> ReservationStatus.IN_USE.getDescription();
            case 2 -> ReservationStatus.COMPLETED.getDescription();
            case 3 -> ReservationStatus.USER_CANCELLED.getDescription();
            case 4 -> ReservationStatus.EXPIRED.getDescription();
            case 5 -> ReservationStatus.ADMIN_CANCELLED.getDescription();
            case 6 -> ReservationStatus.VIOLATED.getDescription();
            default -> "未知";
        };
    }

    @Override
    public ReservationDetailVO getReservation(Long id) {
        Reservation reservation = reservationMapper.findById(id);
        if (reservation == null) {
            throw new InvalidParameterException("Reservation id");
        }
        User user = userMapper.findById(reservation.getUserId());
        Room room = roomMapper.findById(reservation.getRoomId());
        Seat seat = seatMapper.findById(reservation.getSeatId());
        var adminActions = reservationAdminActionMapper.findByReservationId(reservation.getId());
        return new ReservationDetailVO(
                id,
                user.getId(),
                user.getName(),
                user.getUsername(),
                user.getStatus(),
                user.getRole(),
                room.getId(),
                room.getName(),
                room.getStatus(),
                reservation.getSeatId(),
                seat.getSeatCode(),
                String.valueOf(seat.getType()),
                seat.getStatus().getCode(),
                reservation.getStartTime(),
                reservation.getEndTime(),
                reservation.getActualStartTime(),
                reservation.getActualEndTime(),
                reservation.getStatus(),
                this.convertStatusText(reservation.getStatus()),
                reservation.getVersion(),
                adminActions
        );
    }

    @Override
    public Boolean cancelReservation(Long id, String reason) {
        Reservation reservation = reservationMapper.findById(id);
        if (reservation == null) {
            throw new InvalidParameterException("Reservation id");
        }

        ReservationStatusValidator.validateAdminCancel(reservation.getStatus());

        int rows = reservationMapper.updateStatus(
                id,
                ReservationStatus.PENDING.getCode(),
                ReservationStatus.ADMIN_CANCELLED.getCode()
        );

        if (rows > 0) {
            reservationSlotMapper.deleteByReservationId(reservation.getId());
            roomSeatBroadcastService.broadcastRoomSnapshot(reservation.getRoomId());

            recordAdminAction(
                    reservation.getId(),
                    ReservationAdminActionType.CANCEL,
                    (reason == null || reason.trim().isEmpty()) ? "管理员取消预约" : reason.trim(),
                    null,
                    null
            );
        }

        return rows > 0;
    }

    @Override
    public Boolean completeReservation(Long id) {
        Reservation reservation = reservationMapper.findById(id);
        if (reservation == null) {
            throw new InvalidParameterException("Reservation id");
        }

        ReservationStatusValidator.validateAdminComplete(reservation.getStatus());

        int rows = reservationMapper.updateStatus(
                id,
                ReservationStatus.IN_USE.getCode(),
                ReservationStatus.COMPLETED.getCode()
        );

        if (rows == 0) {
            throw new InvalidOperationStatusException("预约状态已变化，无法完成预约");
        }

        reservationMapper.updateActualEndTime(reservation.getId(), java.time.LocalDateTime.now());
        seatMapper.updateStatus(reservation.getSeatId(), SeatStatus.AVAILABLE.getCode());
        reservationSlotMapper.deleteByReservationId(reservation.getId());
        roomSeatBroadcastService.broadcastRoomSnapshot(reservation.getRoomId());

        reputationService.onReservationCompleted(reservation.getUserId(), reservation.getId());

        recordAdminAction(
                reservation.getId(),
                ReservationAdminActionType.COMPLETE,
                "管理员标记预约完成",
                null,
                null
        );

        return true;
    }

    @Override
    public Boolean violationReservation(Long id, String reason, Integer penaltyLevel, Integer banDays) {
        Reservation reservation = reservationMapper.findById(id);
        if (reservation == null) {
            throw new InvalidParameterException("Reservation id");
        }

        ReservationStatusValidator.validateAdminViolation(reservation.getStatus());

        int rows = reservationMapper.updateStatus(
                id,
                ReservationStatus.PENDING.getCode(),
                ReservationStatus.VIOLATED.getCode()
        );

        if (rows == 0) {
            throw new InvalidOperationStatusException("预约状态已变化，无法完成预约");
        }

        reservationSlotMapper.deleteByReservationId(reservation.getId());
        roomSeatBroadcastService.broadcastRoomSnapshot(reservation.getRoomId());

        reputationService.onReservationViolated(reservation.getUserId(), reservation.getId());

        recordAdminAction(
                reservation.getId(),
                ReservationAdminActionType.VIOLATION,
                (reason == null || reason.trim().isEmpty()) ? "管理员标记预约违约" : reason.trim(),
                penaltyLevel,
                banDays
        );

        return true;
    }
}
