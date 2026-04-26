package com.anonymous.service.Impl;

import com.anonymous.common.Page;
import com.anonymous.common.exception.InvalidOperationStatusException;
import com.anonymous.common.exception.InvalidParameterException;
import com.anonymous.common.util.ReservationStatusValidator;
import com.anonymous.dto.admin.reservation.ReservationQueryDTO;
import com.anonymous.mapper.ReservationMapper;
import com.anonymous.mapper.RoomMapper;
import com.anonymous.mapper.SeatMapper;
import com.anonymous.mapper.UserMapper;
import com.anonymous.model.Reservation;
import com.anonymous.model.Room;
import com.anonymous.model.Seat;
import com.anonymous.model.User;
import com.anonymous.model.enums.ReservationStatus;
import com.anonymous.model.enums.SeatStatus;
import com.anonymous.service.AdminReservationService;
import com.anonymous.service.RoomSeatBroadcastService;
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
        ReservationDetailVO result = new ReservationDetailVO(
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
                seat.getStatus().name(),
                seat.getType(),
                reservation.getStartTime(),
                reservation.getEndTime(),
                reservation.getActualStartTime(),
                reservation.getActualEndTime(),
                reservation.getStatus(),
                this.convertStatusText(reservation.getStatus()),
                reservation.getVersion()
                );
        return result;
    }

    @Override
    public Boolean cancelReservation(Long id) {
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
            seatMapper.updateStatus(reservation.getSeatId(), SeatStatus.AVAILABLE.getCode());
            roomSeatBroadcastService.broadcastRoomSnapshot(reservation.getRoomId());
        }

        return rows > 0;
    }

    @Override
    public void completeReservation(Long id) {
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

        seatMapper.updateStatus(reservation.getSeatId(), SeatStatus.AVAILABLE.getCode());
        roomSeatBroadcastService.broadcastRoomSnapshot(reservation.getRoomId());
    }


    @Override
    public void violationReservation(Long id) {
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
            throw new InvalidOperationStatusException("预约状态已变化，无法标记违约");
        }

        seatMapper.updateStatus(reservation.getSeatId(), SeatStatus.AVAILABLE.getCode());
        roomSeatBroadcastService.broadcastRoomSnapshot(reservation.getRoomId());
    }
}
