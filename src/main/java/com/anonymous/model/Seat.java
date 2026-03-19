package com.anonymous.model;

import com.anonymous.model.enums.SeatStatus;

import java.time.LocalDateTime;

public class Seat {

    private Long id;

    private Long roomId;

    private String seatCode;

    private Integer type;

    private SeatStatus status;

    private String maintenanceNote = "";

    private Integer xAxis;

    private Integer yAxis;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    public Seat() {
    }

    public Seat(Long id, Long roomId, String seatCode, Integer type, SeatStatus status, Integer xAxis, Integer yAxis, LocalDateTime createTime, LocalDateTime updateTime) {
        this.id = id;
        this.roomId = roomId;
        this.seatCode = seatCode;
        this.type = type;
        this.status = status;
        this.xAxis = xAxis;
        this.yAxis = yAxis;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public String getSeatCode() {
        return seatCode;
    }

    public void setSeatCode(String seatCode) {
        this.seatCode = seatCode;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public SeatStatus getStatus() {
        return status;
    }

    public void setStatus(SeatStatus status) {
        this.status = status;
    }

    public String getMaintenanceNote() {
        return maintenanceNote;
    }

    public void setMaintenanceNote(String maintenanceNote) {
        this.maintenanceNote = maintenanceNote;
    }

    public Integer getXAxis() {
        return xAxis;
    }

    public void setXAxis(Integer xAxis) {
        this.xAxis = xAxis;
    }

    public Integer getYAxis() {
        return yAxis;
    }

    public void setYAxis(Integer yAxis) {
        this.yAxis = yAxis;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "Seat{" + "id=" + id + ", roomId=" + roomId + ", code=" + seatCode + ", status=" + status + "}";
    }

}
