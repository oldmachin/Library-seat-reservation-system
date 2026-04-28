package com.anonymous.scheduled;

import com.anonymous.mapper.ReservationMapper;
import com.anonymous.model.Reservation;
import com.anonymous.service.ReservationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class ReservationTimeoutFallbackTask {

    private final static Logger log = LoggerFactory.getLogger(ReservationTimeoutFallbackTask.class);

    @Autowired
    private ReservationMapper reservationMapper;

    @Autowired
    private ReservationService reservationService;

    @Scheduled(cron = "0 * * * * *")
    public void scanCheckInTimeouts() {
        LocalDateTime deadline = LocalDateTime.now().minusMinutes(30);
        List<Reservation> candidates = reservationMapper.findCheckInTimeoutCandidates(deadline);

        if (!candidates.isEmpty()) {
            log.info("兜底扫描发现签到超时候选 {} 条", candidates.size());
        }

        for (Reservation reservation : candidates) {
            try {
                reservationService.processTimeout(reservation.getId());
            } catch (Exception ignored) {
            }
        }
    }

    @Scheduled(cron = "30 * * * * *")
    public void scanTempLeaveTimeouts() {
        LocalDateTime deadline = LocalDateTime.now().minusMinutes(30);
        List<Reservation> candidates = reservationMapper.findTempLeaveTimeoutCandidates(deadline);

        if (!candidates.isEmpty()) {
            log.info("兜底扫描发现暂离超时候选 {} 条", candidates.size());
        }

        for (Reservation reservation : candidates) {
            try {
                if (reservation.getTempLeaveStartTime() != null) {
                    reservationService.processTempLeaveTimeout(
                            reservation.getId(),
                            reservation.getTempLeaveStartTime()
                    );
                }
            } catch (Exception ignored) {
            }
        }
    }
}
