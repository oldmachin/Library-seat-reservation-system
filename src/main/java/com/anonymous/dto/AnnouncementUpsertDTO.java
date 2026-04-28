package com.anonymous.dto;

public record AnnouncementUpsertDTO (
        String title,
        String content,
        Integer isPinned,
        String publishTime,
        String expireTime
) {
}
