package com.anonymous.service;

import com.anonymous.common.Page;
import com.anonymous.dto.admin.reservation.ReservationQueryDTO;
import com.anonymous.vo.admin.ReservationAdminVO;
import com.anonymous.vo.admin.ReservationDetailVO;

public interface AdminReservationService {
    Page<ReservationAdminVO> listReservations(ReservationQueryDTO queryDTO);

    Page<ReservationAdminVO> findCurrent(ReservationQueryDTO queryDTO);

    ReservationDetailVO getReservation(Long id);

    Boolean cancelReservation(Long id);

    void completeReservation(Long id);

    void violationReservation(Long id);
}
