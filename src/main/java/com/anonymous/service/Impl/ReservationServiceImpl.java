package com.anonymous.service.Impl;

import com.anonymous.common.Page;
import com.anonymous.common.TimeSlot;
import com.anonymous.common.util.ReservationStatusValidator;
import com.anonymous.common.util.ReservationTimeValidator;
import com.anonymous.mapper.ReservationMapper;
import com.anonymous.mapper.ReservationSlotMapper;
import com.anonymous.mapper.RoomMapper;
import com.anonymous.mapper.SeatMapper;
import com.anonymous.model.Reservation;
import com.anonymous.model.ReservationSlot;
import com.anonymous.model.Room;
import com.anonymous.model.Seat;
import com.anonymous.model.enums.ReservationStatus;
import com.anonymous.model.enums.RoomStatus;
import com.anonymous.model.enums.SeatStatus;
import com.anonymous.service.ReservationService;
import com.anonymous.service.RoomSeatBroadcastService;
import com.anonymous.vo.QuickReservationResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ReservationServiceImpl implements ReservationService {

    @Autowired
    private  ReservationMapper reservationMapper;

    @Autowired
    private SeatMapper seatMapper;

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private RoomSeatBroadcastService roomSeatBroadcastService;

    @Autowired
    private ReservationSlotMapper reservationSlotMapper;

    @Override
    @Transactional
    public Long bookSeat(Long userId, Long seatId, LocalDateTime start, LocalDateTime end) {
        ReservationTimeValidator.validateBookTimeRange(start, end);
        validateFutureBookTime(start);
        List<TimeSlot> slots = ReservationTimeValidator.resolveContinuousSlots(start, end);

        Seat seat = seatMapper.findById(seatId);
        if (seat == null) {
            throw new RuntimeException("座位不存在");
        }
        if (seat.getStatus() == null || seat.getStatus() == SeatStatus.UNAVAILABLE) {
            throw new RuntimeException("当前座位不可预约");
        }
        if (reservationMapper.countActiveReservationsByUserId(userId) > 0) {
            throw new RuntimeException("抱歉，您当前已有生效中的预约，不能重复占座！");
        }

        Long reservationId = tryCreatePendingReservation(userId, seat, start, end, slots);
        if (reservationId == null) {
            throw new RuntimeException("抱歉，该时间段座位已被占用");
        }

        roomSeatBroadcastService.broadcastRoomSnapshot(seat.getRoomId());

        return reservationId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelReservation(Long userId, Long seatId) {
        try {
            Reservation reservation = reservationMapper.findLatestPendingByUserIdAndSeatId(userId, seatId);
            if (reservation == null) {
                return false;
            }

            ReservationStatusValidator.validateUserCancel(reservation.getStatus());

            int rows = reservationMapper.updateStatus(
                    reservation.getId(),
                    ReservationStatus.PENDING.getCode(),
                    ReservationStatus.USER_CANCELLED.getCode()
            );
            if (rows == 0) {
                return false;
            }
            roomSeatBroadcastService.broadcastRoomSnapshot(reservation.getRoomId());
            reservationSlotMapper.deleteByReservationId(reservation.getId());
            return true;
        } catch (Exception e) {
            throw new RuntimeException("取消预约失败" + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean checkIn(Long userId, Long seatId) {
//        log.info("【签到业务】用户 {} 尝试在座位 {} 签到", userId, seatId);
        try {
            Reservation reservation = reservationMapper.findPending(userId);

            if (reservation == null) {
//                log.warn("【签到失败】用户{}没有代签到的记录", userId);
                throw new RuntimeException("您没有待签到的记录，请您先预约");
            }

            if (!reservation.getSeatId().equals(seatId)) {
//                log.warn("【走错座位】用户 {} 预约了 {}, 但扫描了 {}", userId, reservation.getSeatId(), seatId);
                throw new RuntimeException("走错位置啦！您预约的座位不是这个，请重新核对座位号！");
            }

            ReservationStatusValidator.validateCheckIn(reservation.getStatus());

            int rows = reservationMapper.updateStatus(
                reservation.getId(),
                ReservationStatus.PENDING.getCode(),
                ReservationStatus.IN_USE.getCode());

            if (rows == 0) {
//                log.error("【并发冲突】更新预约单状态失败，可能已被其他线程修改。单号: {}", reservation.getId());
                return false;
            }

            seatMapper.updateStatus(seatId, SeatStatus.OCCUPIED.getCode());
//            log.info("【签到成功】用户 {} 已成功入座 {}", userId, seatId);
            roomSeatBroadcastService.broadcastRoomSnapshot(reservation.getRoomId());
            return true;
        } catch (Exception e) {
//            log.error("【系统异常】签到落库失败！userId: {}, seatId: {}", userId, seatId, e);
            throw new RuntimeException("签到事务执行失败，触发回滚" + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean checkOut(Long userId) {
//        log.info("【签退服务】用户 {} 尝试签退", userId);

        try {
            Reservation reservation = reservationMapper.findInUse(userId);

            if (reservation == null) {
//                log.warn("【签退失败】用户{}没有待签退的记录", userId);
                throw new RuntimeException("您没有待签退的记录，请您先预约");
            }
            
            ReservationStatusValidator.validateCheckOut(reservation.getStatus());

            int rows = reservationMapper.updateStatus(reservation.getId(), ReservationStatus.IN_USE.getCode(), ReservationStatus.COMPLETED.getCode());
            if (rows == 0) {
//                log.error("【并发冲突】更新签退状态失败，可能已被其他线程修改。单号: {}", reservation.getId());
                return false;
            }

            reservationSlotMapper.deleteByReservationId(reservation.getId());
            seatMapper.updateStatus(reservation.getSeatId(), SeatStatus.AVAILABLE.getCode());
//            log.info("【签退成功】用户{}已成功签退", userId);
            roomSeatBroadcastService.broadcastRoomSnapshot(reservation.getRoomId());;
            return true;
        } catch (Exception e) {
//            log.error("【系统异常】签退失败！userId: {}", userId, e);
            throw new RuntimeException("签退事务执行失败，触发回滚" + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean leaveTemp(Long userId) {
        try {
            Reservation reservation = reservationMapper.findInUse(userId);
            if (reservation == null) {
                return false;
            }

            ReservationStatusValidator.validateLeaveTemp(reservation.getStatus());

            seatMapper.updateStatus(reservation.getSeatId(), SeatStatus.AWAY.getCode());
            roomSeatBroadcastService.broadcastRoomSnapshot(reservation.getRoomId());
            return true;
        } catch (Exception e) {
            throw new RuntimeException("暂离事务执行失败", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean returnTemp(Long userId, Long seatId) {
        try {
            Reservation reservation = reservationMapper.findInUse(userId);
            if (reservation == null || !reservation.getSeatId().equals(seatId)) {
                return false;
            }

            Seat seat = seatMapper.findById(seatId);

            ReservationStatusValidator.validateLeaveTemp(reservation.getStatus());
            if (seat.getStatus() == null) {
                throw new RuntimeException("座位状态异常");
            }
            ReservationStatusValidator.validateReturnTemp(seat.getStatus().getCode());

            seatMapper.updateStatus(seatId, SeatStatus.OCCUPIED.getCode());
            roomSeatBroadcastService.broadcastRoomSnapshot(reservation.getRoomId());
            return true;
        } catch (Exception e) {
            throw new RuntimeException("返回事务执行失败" + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processTimeout(Long reservationId) {
        try {
            Reservation reservation = reservationMapper.findById(reservationId);

            if (reservation == null || !reservation.getStatus().equals(ReservationStatus.PENDING.getCode())) {
                return;
            }

            LocalDateTime deadline = reservation.getStartTime().plusMinutes(15);
            if (LocalDateTime.now().isAfter(deadline)) {
//                log.warn("【触发违约】订单 {} 已超过最晚签到时间 {}，执行强制释放！", reservationId, deadline);
                int rows = reservationMapper.updateStatus(
                        reservationId,
                        ReservationStatus.PENDING.getCode(),
                        ReservationStatus.EXPIRED.getCode()
                );

                if (rows > 0) {
                    reservationSlotMapper.deleteByReservationId(reservation.getId());
                    roomSeatBroadcastService.broadcastRoomSnapshot(reservation.getRoomId());
//                    log.info("【座位回收】座位 {} 已重新释放到公共资源池", seatId);
                } else {
//                    log.info("【极限抢救】订单 {} 状态更新失败，用户可能在最后一秒完成了签到", reservationId);
                }
            }
        } catch (Exception e) {
//            log.error("【系统异常】处理超时违约失败！订单号: {}", reservationId, e);
            throw new RuntimeException("处理超时违约事务执行失败", e); // 必须抛出以回滚
        }
    }

    @Override
    public Page<Reservation> getHistory(Long userId, int pageNum, int pageSize) {
        if (pageNum < 1) {
            pageNum = 1;
        }

        int offset = (pageNum - 1) * pageSize;
        long total = reservationMapper.countByUserId(userId);
        List<Reservation> data = reservationMapper.findPageByUserId(userId, pageSize, offset);
        return new Page<>(data, total, pageNum, pageSize);
    }

    @Override
    public Reservation getCurrent(Long userId) {
        return reservationMapper.findCurrent(userId);
    }

    private Long tryCreatePendingReservation(Long userId, Seat seat, LocalDateTime start, LocalDateTime end, List<TimeSlot> slots) {
    if (seat.getStatus() == null || seat.getStatus() == SeatStatus.UNAVAILABLE) {
        return null;
    }

    int overlap = reservationMapper.countOverlap(seat.getId(), start, end);
    if (overlap > 0) {
        return null;
    }

    Reservation reservation = new Reservation();
    reservation.setUserId(userId);
    reservation.setRoomId(seat.getRoomId());
    reservation.setSeatId(seat.getId());
    reservation.setStartTime(start);
    reservation.setEndTime(end);
    reservation.setStatus(ReservationStatus.PENDING.getCode());
    reservationMapper.insert(reservation);

    List<ReservationSlot> slotRecords = slots.stream().map(slot -> {
        ReservationSlot item = new ReservationSlot();
        item.setReservationId(reservation.getId());
        item.setUserId(userId);
        item.setRoomId(seat.getRoomId());
        item.setSeatId(seat.getId());
        item.setReserveDate(start.toLocalDate());
        item.setSlotCode(slot.getCode());
        item.setSlotStartTime(slot.getStartTime());
        item.setSlotEndTime(slot.getEndTime());
        return item;
    }).toList();

        try {
            reservationSlotMapper.batchInsert(slotRecords);
            return reservation.getId();
        } catch (DuplicateKeyException e) {
            reservationMapper.deleteById(reservation.getId());
            return null;
        }
    }

    private void validateFutureBookTime(LocalDateTime start) {
        if (!start.isAfter(LocalDateTime.now())) {
            throw new RuntimeException("只能预约当前时间之后的时间段");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public QuickReservationResultVO quickBook(Long userId, LocalDateTime start, LocalDateTime end) {
    ReservationTimeValidator.validateBookTimeRange(start, end);
    validateFutureBookTime(start);
    List<TimeSlot> slots = ReservationTimeValidator.resolveContinuousSlots(start, end);

    if (reservationMapper.countActiveReservationsByUserId(userId) > 0) {
        throw new RuntimeException("抱歉，您当前已有生效中的预约，不能重复占座！");
    }

    List<Room> rooms = roomMapper.findAllByStatuses(List.of(RoomStatus.AVAILABLE.getCode()));
    rooms.sort(Comparator.comparing(Room::getId));

    for (Room room : rooms) {
        List<Seat> seats = seatMapper.findByRoomId(room.getId()).stream()
                .filter(seat -> seat.getStatus() != null && seat.getStatus() != SeatStatus.UNAVAILABLE)
                .sorted(Comparator.comparing(Seat::getSeatCode, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();

        for (Seat seat : seats) {
            Long reservationId = tryCreatePendingReservation(userId, seat, start, end, slots);
            if (reservationId != null) {
                roomSeatBroadcastService.broadcastRoomSnapshot(room.getId());
                return new QuickReservationResultVO(
                        reservationId,
                        room.getId(),
                        room.getName(),
                        seat.getId(),
                        seat.getSeatCode()
                );
            }
        }
    }

    throw new RuntimeException("当前时间暂无可分配座位，请稍后再试或切换时间段。");
}

}
