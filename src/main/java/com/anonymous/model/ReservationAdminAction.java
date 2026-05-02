package com.anonymous.model;

import com.anonymous.model.enums.ReservationAdminActionType;

import java.time.LocalDateTime;

public class ReservationAdminAction {
    private Long id;
    private Long reservationId;
    private ReservationAdminActionType actionType;
    private Long operatorId;
    private String reason;
    private Integer penaltyLevel;
    private Integer banDays;
    private LocalDateTime createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }

    public ReservationAdminActionType getActionType() {
        return actionType;
    }

    public void setActionType(ReservationAdminActionType actionType) {
        this.actionType = actionType;
    }

    public Long getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Integer getPenaltyLevel() {
        return penaltyLevel;
    }

    public void setPenaltyLevel(Integer penaltyLevel) {
        this.penaltyLevel = penaltyLevel;
    }

    public Integer getBanDays() {
        return banDays;
    }

    public void setBanDays(Integer banDays) {
        this.banDays = banDays;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
