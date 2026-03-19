package com.anonymous.model.enums;

public enum SeatStatus {
    AVAILABLE(0, "空闲"),
    RESERVED(1, "已预约"),
    OCCUPIED(2, "使用中"),
    AWAY(3, "暂时离开"),
    UNAVAILABLE(4, "不可用");

    private final int code;
    private final String description;

    SeatStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public static SeatStatus fromCode(int code) {
        for (SeatStatus status : SeatStatus.values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的预约状态码: " + code);
    }
}
