package com.anonymous.service.Impl;

import com.anonymous.mapper.SeatMapper;
import com.anonymous.model.Seat;
import com.anonymous.model.enums.SeatStatus;
import com.anonymous.service.SeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SeatServiceImpl implements SeatService {

    @Autowired
    private SeatMapper seatMapper;

    @Override
    public void updateSeatStatus(Long seatId, SeatStatus status) {
        seatMapper.updateStatus(seatId, status.getCode());
    }

    @Override
    public void markDefective(Long seatId, String reason) {
        Seat seat = seatMapper.findById(seatId);

        if (seat == null) {
            throw new RuntimeException("座位不存在");
        }

        if (seat.getStatus() != SeatStatus.UNAVAILABLE) {

        }

        seat.setStatus(SeatStatus.UNAVAILABLE);
        seat.setMaintenanceNote(reason);

        seatMapper.updateStatusAndNote(seatId, SeatStatus.UNAVAILABLE.getCode(), reason);
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
}
