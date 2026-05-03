package com.anonymous.model;

import java.time.LocalDateTime;

public class RoomSeatAdminAction {
    private Long id;
    private String resourceType;
    private String actionType;
    private Long operatorId;
    private Long roomId;
    private Long seatId;
    private Integer beforeStatus;
    private Integer afterStatus;
    private String beforeName;
    private String afterName;
    private Integer beforeCapacity;
    private Integer afterCapacity;
    private String beforeTemplate;
    private String afterTemplate;
    private String beforeNote;
    private String afterNote;
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

    public String getAfterNote() {
        return afterNote;
    }

    public void setAfterNote(String afterNote) {
        this.afterNote = afterNote;
    }

    public String getBeforeNote() {
        return beforeNote;
    }

    public void setBeforeNote(String beforeNote) {
        this.beforeNote = beforeNote;
    }

    public String getAfterTemplate() {
        return afterTemplate;
    }

    public void setAfterTemplate(String afterTemplate) {
        this.afterTemplate = afterTemplate;
    }

    public String getBeforeTemplate() {
        return beforeTemplate;
    }

    public void setBeforeTemplate(String beforeTemplate) {
        this.beforeTemplate = beforeTemplate;
    }

    public Integer getAfterCapacity() {
        return afterCapacity;
    }

    public void setAfterCapacity(Integer afterCapacity) {
        this.afterCapacity = afterCapacity;
    }

    public Integer getBeforeCapacity() {
        return beforeCapacity;
    }

    public void setBeforeCapacity(Integer beforeCapacity) {
        this.beforeCapacity = beforeCapacity;
    }

    public String getAfterName() {
        return afterName;
    }

    public void setAfterName(String afterName) {
        this.afterName = afterName;
    }

    public String getBeforeName() {
        return beforeName;
    }

    public void setBeforeName(String beforeName) {
        this.beforeName = beforeName;
    }

    public Integer getAfterStatus() {
        return afterStatus;
    }

    public void setAfterStatus(Integer afterStatus) {
        this.afterStatus = afterStatus;
    }

    public Integer getBeforeStatus() {
        return beforeStatus;
    }

    public void setBeforeStatus(Integer beforeStatus) {
        this.beforeStatus = beforeStatus;
    }

    public Long getSeatId() {
        return seatId;
    }

    public void setSeatId(Long seatId) {
        this.seatId = seatId;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public Long getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    private LocalDateTime createTime;

}
