package com.anonymous.service;

import com.anonymous.model.User;

public interface ReputationService {

    void onReservationCompleted(Long userId, Long reservationId);

    void onUserCancelled(Long userId, Long reservationId);

    void onReservationExpired(Long userId, Long reservationId);

    void onReservationViolated(Long userId, Long reservationId);

    void onAdminAdjusted(Long userId, Integer delta, String reason, Long operatorId);

    User refreshBlacklistIfNeeded(Long userId);
}
