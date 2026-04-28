package com.anonymous.vo;

import java.time.LocalDateTime;

public record AnnouncementVO(
        Long id,
        String title,
        String content,
        Boolean pinned,
        LocalDateTime publishTime,
        LocalDateTime expireTime
) {
}
