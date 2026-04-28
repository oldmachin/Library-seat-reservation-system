package com.anonymous.vo;

import java.time.LocalDateTime;

public record AnnouncementAdminVO(
        Long id,
        String title,
        String content,
        Integer status,
        String statusText,
        Boolean pinned,
        LocalDateTime publishTime,
        LocalDateTime expireTime,
        Long creatorId,
        LocalDateTime createTime,
        LocalDateTime updateTime
) {
}
