package com.anonymous.model.enums;

public enum ReservationStatus {

    PENDING(0, "待使用"),
    IN_USE(1, "使用中"),
    COMPLETED(2, "已完成"),
    CANCELLED(3, "已取消"),
    VIOLATED(4, "已违约");

    private final int code;
    private final String description;

    ReservationStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static ReservationStatus fromCode(int code) {
        for (ReservationStatus status : ReservationStatus.values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的预约状态码: " + code);
    }
}
