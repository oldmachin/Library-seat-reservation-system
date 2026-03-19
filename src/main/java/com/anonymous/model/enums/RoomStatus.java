package com.anonymous.model.enums;

public enum RoomStatus {
    AVAILABLE(0, "开放中"),
    MAINTAINING(1, "维护中"),
    DISCARD(2, "已废弃");

    private final int code;
    private final String description;

    RoomStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public static RoomStatus fromCode(int code) {
        for (RoomStatus status : RoomStatus.values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的预约状态码: " + code);
    }
}
