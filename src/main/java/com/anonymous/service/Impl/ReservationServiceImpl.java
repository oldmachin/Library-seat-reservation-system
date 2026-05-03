package com.anonymous.service.Impl;

import com.anonymous.common.Page;
import com.anonymous.common.TimeSlot;
import com.anonymous.common.exception.BusinessException;
import com.anonymous.common.exception.InvalidOperationStatusException;
import com.anonymous.common.exception.InvalidParameterException;
import com.anonymous.common.util.ReservationStatusValidator;
import com.anonymous.common.util.ReservationTimeValidator;
import com.anonymous.dto.ReservationTimeoutMessage;
import com.anonymous.mapper.*;
import com.anonymous.model.*;
import com.anonymous.model.enums.ReservationStatus;
import com.anonymous.model.enums.ReservationTimeoutEventType;
import com.anonymous.model.enums.RoomStatus;
import com.anonymous.model.enums.SeatStatus;
import com.anonymous.mq.ReservationTimeoutProducer;
import com.anonymous.service.ReputationService;
import com.anonymous.service.ReservationService;
import com.anonymous.service.RoomSeatBroadcastService;
import com.anonymous.vo.QuickReservationResultVO;
import com.anonymous.vo.ReservationUserDetailVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class ReservationServiceImpl implements ReservationService {

    private final static Logger log = LoggerFactory.getLogger(ReservationServiceImpl.class);

    private final static int NORMAL_BOOK_THRESHOLD = 80;

    private final static int QUICK_BOOK_THRESHOLD = 60;

    private final static DateTimeFormatter BLACKLIST_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

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

    @Autowired
    private ReputationService reputationService;

    @Autowired
    private ReservationTimeoutProducer reservationTimeoutProducer;

    @Autowired
    private ReservationAdminActionMapper reservationAdminActionMapper;

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

    private void publishCheckInTimeoutMessage(Reservation reservation) {
        LocalDateTime deadline = reservation.getStartTime().plusMinutes(30);
        long delayMillis = Duration.between(LocalDateTime.now(), deadline).toMillis();

        ReservationTimeoutMessage payload = new ReservationTimeoutMessage(
                reservation.getId(),
                ReservationTimeoutEventType.CHECK_IN_TIMEOUT.name(),
                reservation.getStartTime().toString()
        );
        reservationTimeoutProducer.sendCheckInTimeoutMessage(payload, delayMillis);
    }

    private void publishTempLeaveTimeoutMessage(Reservation reservation, LocalDateTime tempLeaveStartTime) {
        ReservationTimeoutMessage payload = new ReservationTimeoutMessage(
                reservation.getId(),
                ReservationTimeoutEventType.TEMP_LEAVE_TIMEOUT.name(),
                tempLeaveStartTime.toString()
        );

        reservationTimeoutProducer.sendTempLeaveTimeoutMessage(payload);
    }


    private User validateBookingPermission(Long userId, boolean quickBooking) {
        User user = reputationService.refreshBlacklistIfNeeded(userId);
        int score = user.getReputationScore() == null ? 100 : user.getReputationScore();

        if (user.getBlacklistUntil() != null && user.getBlacklistUntil().isAfter(LocalDateTime.now())) {
            throw new BusinessException(403,
                    "当前信誉分过低，已进入黑名单，需等待至 "
                            + user.getBlacklistUntil().format(BLACKLIST_TIME_FORMATTER)
                            + " 后恢复预约权限"
            );
        }

        if (score < QUICK_BOOK_THRESHOLD) {
            throw new BusinessException(403, "当前信誉分过低，暂时无法预约");
        }

        if (!quickBooking && score < NORMAL_BOOK_THRESHOLD) {
            throw new BusinessException(403, "当前信誉分低于80分，仅可使用快捷选座");
        }

        return user;
    }

    @Override
    @Transactional
    public Long bookSeat(Long userId, Long seatId, LocalDateTime start, LocalDateTime end) {
        ReservationTimeValidator.validateBookTimeRange(start, end);
        validateFutureBookTime(start);
        validateBookingPermission(userId, false);
        List<TimeSlot> slots = ReservationTimeValidator.resolveContinuousSlots(start, end);

        Seat seat = seatMapper.findById(seatId);
        if (seat == null) {
            throw new InvalidParameterException("seat.id");
        }
        if (seat.getStatus() == null || seat.getStatus() == SeatStatus.UNAVAILABLE) {
            throw new InvalidOperationStatusException("当前座位不可预约");
        }
        if (reservationMapper.countActiveReservationsByUserId(userId) > 0) {
            throw new InvalidOperationStatusException("抱歉，您当前已有生效中的预约，不能重复占座！");
        }

        Long reservationId = tryCreatePendingReservation(userId, seat, start, end, slots);
        if (reservationId == null) {
            throw new InvalidOperationStatusException("抱歉，该时间段座位已被占用");
        }

        roomSeatBroadcastService.broadcastRoomSnapshot(seat.getRoomId());

        return reservationId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelReservation(Long userId, Long seatId) {
        Reservation reservation = reservationMapper.findLatestPendingByUserIdAndSeatId(userId, seatId);
        if (reservation == null) {
            throw new InvalidOperationStatusException("当前没有可取消的预约");
        }

        ReservationStatusValidator.validateUserCancel(reservation.getStatus());

        int rows = reservationMapper.updateStatus(
                reservation.getId(),
                ReservationStatus.PENDING.getCode(),
                ReservationStatus.USER_CANCELLED.getCode()
        );
        if (rows == 0) {
            throw new InvalidOperationStatusException("预约状态已变化，取消失败");
        }
        reservationSlotMapper.deleteByReservationId(reservation.getId());
        roomSeatBroadcastService.broadcastRoomSnapshot(reservation.getRoomId());
        reputationService.onUserCancelled(
                reservation.getUserId(),
                reservation.getId()
            );
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean checkIn(Long userId, Long seatId) {
        Reservation reservation = reservationMapper.findPending(userId);

        if (reservation == null) {
//                log.warn("【签到失败】用户{}没有代签到的记录", userId);
            throw new InvalidOperationStatusException("您没有待签到的记录，请您先预约");
        }

        if (!reservation.getSeatId().equals(seatId)) {
//                log.warn("【走错座位】用户 {} 预约了 {}, 但扫描了 {}", userId, reservation.getSeatId(), seatId);
            throw new InvalidOperationStatusException("走错位置啦！您预约的座位不是这个，请重新核对座位号！");
        }

        ReservationStatusValidator.validateCheckIn(reservation.getStatus());

        int rows = reservationMapper.updateStatus(
                reservation.getId(),
                ReservationStatus.PENDING.getCode(),
                ReservationStatus.IN_USE.getCode());

        if (rows == 0) {
            throw new InvalidOperationStatusException("预约状态已变化，签到失败");
        }


        reservationMapper.updateActualStartTime(reservation.getId(), LocalDateTime.now());
        seatMapper.updateStatus(seatId, SeatStatus.OCCUPIED.getCode());
//            log.info("【签到成功】用户 {} 已成功入座 {}", userId, seatId);
        roomSeatBroadcastService.broadcastRoomSnapshot(reservation.getRoomId());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean checkOut(Long userId) {
//        log.info("【签退服务】用户 {} 尝试签退", userId);
        Reservation reservation = reservationMapper.findInUse(userId);

        if (reservation == null) {
//                log.warn("【签退失败】用户{}没有待签退的记录", userId);
            throw new InvalidOperationStatusException("您没有待签退的记录，请您先预约");
        }

        ReservationStatusValidator.validateCheckOut(reservation.getStatus());

        int rows = reservationMapper.updateStatus(reservation.getId(), ReservationStatus.IN_USE.getCode(), ReservationStatus.COMPLETED.getCode());
        if (rows == 0) {
//                log.error("【并发冲突】更新签退状态失败，可能已被其他线程修改。单号: {}", reservation.getId());
            throw new InvalidOperationStatusException("预约状态已变化，签退失败");
        }

        reservationSlotMapper.deleteByReservationId(reservation.getId());
        reservationMapper.updateTempLeaveStartTime(reservation.getId(), null);
        reservationMapper.updateActualEndTime(reservation.getId(), LocalDateTime.now());
        seatMapper.updateStatus(reservation.getSeatId(), SeatStatus.AVAILABLE.getCode());
//            log.info("【签退成功】用户{}已成功签退", userId);
        roomSeatBroadcastService.broadcastRoomSnapshot(reservation.getRoomId());
        reputationService.onReservationCompleted(reservation.getUserId(), reservation.getId());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean leaveTemp(Long userId) {
        Reservation reservation = reservationMapper.findInUse(userId);
        if (reservation == null) {
            throw new InvalidOperationStatusException("当前没有使用中的预约，无法暂离");
        }

        ReservationStatusValidator.validateLeaveTemp(reservation.getStatus());

        LocalDateTime tempLeaveStartTime = LocalDateTime.now().withNano(0);
        reservationMapper.updateTempLeaveStartTime(reservation.getId(), tempLeaveStartTime);
        seatMapper.updateStatus(reservation.getSeatId(), SeatStatus.AWAY.getCode());
        roomSeatBroadcastService.broadcastRoomSnapshot(reservation.getRoomId());
        publishTempLeaveTimeoutMessage(reservation, tempLeaveStartTime);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean returnTemp(Long userId, Long seatId) {
        Reservation reservation = reservationMapper.findInUse(userId);
        if (reservation == null) {
            throw new InvalidOperationStatusException("当前没有使用中的预约，无法返回");
        }
        if (!reservation.getSeatId().equals(seatId)) {
            throw new InvalidOperationStatusException("座位不匹配，无法返回");
        }

        Seat seat = seatMapper.findById(seatId);

        ReservationStatusValidator.validateLeaveTemp(reservation.getStatus());
        if (seat.getStatus() == null) {
            throw new InvalidOperationStatusException("座位状态异常");
        }
        ReservationStatusValidator.validateReturnTemp(seat.getStatus().getCode());

        reservationMapper.updateTempLeaveStartTime(reservation.getId(), null);
        seatMapper.updateStatus(seatId, SeatStatus.OCCUPIED.getCode());
        roomSeatBroadcastService.broadcastRoomSnapshot(reservation.getRoomId());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processTimeout(Long reservationId) {
        try {
            Reservation reservation = reservationMapper.findById(reservationId);

            if (reservation == null || !reservation.getStatus().equals(ReservationStatus.PENDING.getCode())) {
                return;
            }

            LocalDateTime deadline = reservation.getStartTime().plusMinutes(30);
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
                    reputationService.onReservationExpired(reservation.getUserId(), reservation.getId());
//                    log.info("【座位回收】座位 {} 已重新释放到公共资源池", seatId);
                }
            }
        } catch (Exception e) {
//            log.error("【系统异常】处理超时违约失败！订单号: {}", reservationId, e);
            throw new BusinessException(500, "处理超时违约事务执行失败", e); // 必须抛出以回滚
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processTempLeaveTimeout(Long reservationId, LocalDateTime expectedTempLeaveStartTime) {
        try {
            Reservation reservation = reservationMapper.findById(reservationId);
            if (reservation == null) {
                return;
            }

            if (!Objects.equals(ReservationStatus.IN_USE.getCode(), reservation.getStatus())) {
                return;
            }

            if (reservation.getTempLeaveStartTime() == null) {
                return;
            }

            if (!reservation.getTempLeaveStartTime().equals(expectedTempLeaveStartTime)) {
                return;
            }

            LocalDateTime deadline = expectedTempLeaveStartTime.plusMinutes(30);
            if (!LocalDateTime.now().isAfter(deadline)) {
                return;
            }

            Seat seat = seatMapper.findById(reservation.getSeatId());
            if (seat == null || seat.getStatus() == null || seat.getStatus() != SeatStatus.AWAY) {
                return;
            }

            int rows = reservationMapper.updateStatusAndClearTempLeaveIfMatch(
                    reservation.getId(),
                    ReservationStatus.IN_USE.getCode(),
                    ReservationStatus.VIOLATED.getCode(),
                    expectedTempLeaveStartTime
            );

            if (rows > 0) {
                seatMapper.updateStatus(reservation.getSeatId(), SeatStatus.AVAILABLE.getCode());
                reservationSlotMapper.deleteByReservationId(reservation.getId());
                roomSeatBroadcastService.broadcastRoomSnapshot(reservation.getRoomId());
                reputationService.onReservationViolated(reservation.getUserId(), reservation.getId());
            }
        } catch (Exception e) {
            throw new BusinessException(500, "处理暂离超时事务执行失败", e);
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

    @Override
    public ReservationUserDetailVO getDetail(Long userId, Long reservationId) {
        Reservation reservation = reservationMapper.findById(reservationId);
        if (reservation == null) {
            throw new InvalidParameterException("Reservation id");
        }

        if (!Objects.equals(reservation.getUserId(), userId)) {
            throw new BusinessException(403, "无权查看该预约详情");
        }

        Room room = roomMapper.findById(reservation.getRoomId());
        Seat seat = seatMapper.findById(reservation.getSeatId());
        var adminActions = reservationAdminActionMapper.findByReservationId(reservation.getId());

        return new ReservationUserDetailVO(
                reservation.getId(),
                room.getId(),
                room.getName(),
                reservation.getSeatId(),
                seat.getSeatCode(),
                String.valueOf(seat.getType()),
                seat.getStatus().getCode(),
                reservation.getStartTime(),
                reservation.getEndTime(),
                reservation.getActualStartTime(),
                reservation.getActualEndTime(),
                reservation.getTempLeaveStartTime(),
                reservation.getStatus(),
                convertStatusText(reservation.getStatus()),
                reservation.getVersion(),
                adminActions
        );
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
            publishCheckInTimeoutMessage(reservation);
            return reservation.getId();
        } catch (DuplicateKeyException e) {
            reservationMapper.deleteById(reservation.getId());
            return null;
        }
    }

    private void validateFutureBookTime(LocalDateTime start) {
        if (!start.isAfter(LocalDateTime.now())) {
            throw new InvalidParameterException("reservation.startTime");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public QuickReservationResultVO quickBook(Long userId, LocalDateTime start, LocalDateTime end) {
        ReservationTimeValidator.validateBookTimeRange(start, end);
        validateFutureBookTime(start);
        validateBookingPermission(userId, true);
        List<TimeSlot> slots = ReservationTimeValidator.resolveContinuousSlots(start, end);

        if (reservationMapper.countActiveReservationsByUserId(userId) > 0) {
            throw new InvalidOperationStatusException("抱歉，您当前已有生效中的预约，不能重复占座！");
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

        throw new InvalidOperationStatusException("当前时间暂无可分配座位，请稍后再试或切换时间段。");
    }
}
