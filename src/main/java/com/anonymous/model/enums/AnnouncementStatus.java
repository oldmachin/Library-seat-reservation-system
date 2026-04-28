package com.anonymous.model.enums;

public enum AnnouncementStatus {
    DRAFT(0, "草稿"),
    PUBLISHED(1, "已发布"),
    OFFLINE(2, "已下线");

    private final int code;
    private final String description;

    AnnouncementStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static AnnouncementStatus fromCode(Integer code) {
        if (code == null) {
            throw new IllegalArgumentException("公告状态不能为空");
        }
        for (AnnouncementStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知公告状态码: " + code);
    }
}
