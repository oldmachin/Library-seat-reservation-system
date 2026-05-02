package com.anonymous.service;

import com.anonymous.common.Page;
import com.anonymous.dto.ReservationAdminActionQueryDTO;
import com.anonymous.dto.admin.reservation.ReservationQueryDTO;
import com.anonymous.vo.ReservationAdminActionLogVO;
import com.anonymous.vo.admin.ReservationAdminVO;
import com.anonymous.vo.admin.ReservationDetailVO;

public interface AdminReservationService {
    Page<ReservationAdminVO> listReservations(ReservationQueryDTO queryDTO);

    Page<ReservationAdminVO> findCurrent(ReservationQueryDTO queryDTO);

    Page<ReservationAdminActionLogVO> listReservationActions(ReservationAdminActionQueryDTO query);

    ReservationDetailVO getReservation(Long id);

    Boolean cancelReservation(Long id, String reason);

    Boolean completeReservation(Long id);

    Boolean violationReservation(Long id, String reason, Integer penaltyLevel, Integer banDays);
}
