package com.anonymous.model;

import java.time.LocalDateTime;

public class ReputationRecord {

    private Long id;
    private Long userId;
    private Long reservationId;
    private String eventType;
    private Long operatorId;
    private Integer scoreDelta;
    private Integer scoreBefore;
    private Integer scoreAfter;

    public LocalDateTime getBlacklistUntil() {
        return blacklistUntil;
    }

    public void setBlacklistUntil(LocalDateTime blacklistUntil) {
        this.blacklistUntil = blacklistUntil;
    }

    public Integer getScoreBefore() {
        return scoreBefore;
    }

    public void setScoreBefore(Integer scoreBefore) {
        this.scoreBefore = scoreBefore;
    }

    public Long getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
    }

    private LocalDateTime blacklistUntil;
    private String reason;

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Integer getScoreAfter() {
        return scoreAfter;
    }

    public void setScoreAfter(Integer scoreAfter) {
        this.scoreAfter = scoreAfter;
    }

    public Integer getScoreDelta() {
        return scoreDelta;
    }

    public void setScoreDelta(Integer scoreDelta) {
        this.scoreDelta = scoreDelta;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    private LocalDateTime createTime;
}
