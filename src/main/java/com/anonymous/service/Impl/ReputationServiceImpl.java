package com.anonymous.service.Impl;

import com.anonymous.common.exception.BusinessException;
import com.anonymous.mapper.ReputationRecordMapper;
import com.anonymous.mapper.UserMapper;
import com.anonymous.model.ReputationRecord;
import com.anonymous.model.User;
import com.anonymous.model.enums.ReputationEventType;
import com.anonymous.service.ReputationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ReputationServiceImpl implements ReputationService {

    private static final int DEFAULT_SCORE = 100;
    private static final int MIN_SCORE = 0;
    private static final int MAX_SCORE = 120;
    private static final int BLACKLIST_THRESHOLD = 60;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ReputationRecordMapper reputationRecordMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onReservationCompleted(Long userId, Long reservationId) {
        applyChange(userId, reservationId, null, ReputationEventType.COMPLETED, 1, "预约正常完成，信誉分 +1");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onUserCancelled(Long userId, Long reservationId) {
        applyChange(userId, reservationId, null, ReputationEventType.USER_CANCELLED, -2, "用户主动取消预约，信誉分 -2");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onReservationExpired(Long userId, Long reservationId) {
        applyChange(userId, reservationId, null, ReputationEventType.EXPIRED, -10, "预约超时未签到，信誉分 -10");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onReservationViolated(Long userId, Long reservationId) {
        applyChange(userId, reservationId, null, ReputationEventType.VIOLATED, -10, "预约违约，信誉分 -10");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onAdminAdjusted(Long userId, Integer delta, String reason, Long operatorId) {
        applyChange(userId, null, operatorId, ReputationEventType.ADMIN_ADJUSTED, delta, reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User refreshBlacklistIfNeeded(Long userId) {
        User user = userMapper.findByIdForUpdate(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        if (user.getBlacklistUntil() != null && !user.getBlacklistUntil().isAfter(LocalDateTime.now())) {
            releaseBlacklist(user);
            user = userMapper.findById(userId);
        }

        return user;
    }

    private void applyChange(Long userId,
                             Long reservationId,
                             Long operatorId,
                             ReputationEventType eventType,
                             int delta,
                             String reason) {
        User user = userMapper.findByIdForUpdate(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        if (user.getBlacklistUntil() != null && !user.getBlacklistUntil().isAfter(LocalDateTime.now())) {
            releaseBlacklist(user);
            user = userMapper.findByIdForUpdate(userId);
        }

        int scoreBefore = normalizeScore(user.getReputationScore());
        int scoreAfter = clamp(scoreBefore + delta);
        LocalDateTime blacklistUntil = user.getBlacklistUntil();

        if (scoreAfter < BLACKLIST_THRESHOLD) {
            LocalDateTime candidate = LocalDateTime.now().plusDays(7);
            if (blacklistUntil == null || blacklistUntil.isBefore(candidate)) {
                blacklistUntil = candidate;
            }
        } else if (eventType == ReputationEventType.ADMIN_ADJUSTED) {
            blacklistUntil = null;
        }

        userMapper.updateReputationState(userId, scoreAfter, blacklistUntil);

        ReputationRecord record = new ReputationRecord();
        record.setUserId(userId);
        record.setReservationId(reservationId);
        record.setOperatorId(operatorId);
        record.setEventType(eventType.name());
        record.setScoreBefore(scoreBefore);
        record.setScoreDelta(delta);
        record.setScoreAfter(scoreAfter);
        record.setReason(reason);
        record.setBlacklistUntil(blacklistUntil);
        reputationRecordMapper.insert(record);
    }

    private void releaseBlacklist(User user) {
        int scoreBefore = normalizeScore(user.getReputationScore());
        int scoreAfter = Math.max(scoreBefore, BLACKLIST_THRESHOLD);

        userMapper.updateReputationState(user.getId(), scoreAfter, null);

        ReputationRecord record = new ReputationRecord();
        record.setUserId(user.getId());
        record.setReservationId(null);
        record.setOperatorId(null);
        record.setEventType(ReputationEventType.BLACKLIST_RELEASED.name());
        record.setScoreBefore(scoreBefore);
        record.setScoreDelta(scoreAfter - scoreBefore);
        record.setScoreAfter(scoreAfter);
        record.setReason("黑名单期满，恢复至60分");
        record.setBlacklistUntil(null);
        reputationRecordMapper.insert(record);
    }

    private int normalizeScore(Integer score) {
        return score == null ? DEFAULT_SCORE : clamp(score);
    }

    private int clamp(int score) {
        if (score < MIN_SCORE) {
            return MIN_SCORE;
        }
        if (score > MAX_SCORE) {
            return MAX_SCORE;
        }
        return score;
    }
}
