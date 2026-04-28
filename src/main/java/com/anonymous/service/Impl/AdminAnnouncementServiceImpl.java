package com.anonymous.service.Impl;

import com.anonymous.common.Page;
import com.anonymous.common.exception.InvalidParameterException;
import com.anonymous.dto.AnnouncementUpsertDTO;
import com.anonymous.mapper.AnnouncementMapper;
import com.anonymous.model.Announcement;
import com.anonymous.model.enums.AnnouncementStatus;
import com.anonymous.service.AdminAnnouncementService;
import com.anonymous.vo.AnnouncementAdminVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class AdminAnnouncementServiceImpl implements AdminAnnouncementService {

    private final static DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private AnnouncementMapper announcementMapper;

    private AnnouncementAdminVO toAdminVO(Announcement announcement) {
        String statusText;
        try {
            statusText = AnnouncementStatus.fromCode(announcement.getStatus()).getDescription();
        } catch (IllegalArgumentException ex) {
            statusText = "未知";
        }

        return new AnnouncementAdminVO(
                announcement.getId(),
                announcement.getTitle(),
                announcement.getContent(),
                announcement.getStatus(),
                statusText,
                announcement.getIsPinned() != null && announcement.getIsPinned() == 1,
                announcement.getPublishTime(),
                announcement.getExpireTime(),
                announcement.getCreatorId(),
                announcement.getCreateTime(),
                announcement.getUpdateTime()
        );
    }

    private void validateUpsertRequest(AnnouncementUpsertDTO request) {
        if (request == null) {
            throw new InvalidParameterException("announcement");
        }
        if (request.title() == null || request.title().trim().isEmpty()) {
            throw new InvalidParameterException("announcement.title");
        }
        if (request.content() == null || request.content().trim().isEmpty()) {
            throw new InvalidParameterException("announcement.content");
        }
    }

    private Integer normalizePinned(Integer isPinned) {
        return (isPinned != null && isPinned == 1) ? 1 : 0;
    }

    private LocalDateTime parseDateTime(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(value.trim(), FORMATTER);
    }

    private void validateTimeRange(LocalDateTime publishTime, LocalDateTime expireTime) {
        if (publishTime != null && expireTime != null && !publishTime.isBefore(expireTime)) {
            throw new RuntimeException("公告发布时间必须早于过期时间");
        }
    }

    @Override
    public Page<AnnouncementAdminVO> listAnnouncements(int pageNum, int pageSize) {
        if (pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize < 1) {
            pageSize = 10;
        }
        if (pageSize > 100) {
            pageSize = 100;
        }

        int offset = (pageNum - 1) * pageSize;
        List<AnnouncementAdminVO> records = announcementMapper.findAllForAdmin(offset, pageSize)
                .stream()
                .map(this::toAdminVO)
                .toList();
        long total = announcementMapper.countAllForAdmin();

        return new Page<>(records, total, pageNum, pageSize);
    }

    @Override
    public AnnouncementAdminVO getAnnouncement(Long id) {
        Announcement announcement = announcementMapper.findById(id);
        if (announcement == null) {
            throw new InvalidParameterException("announcement.id");
        }
        return toAdminVO(announcement);
    }

    @Override
    public Boolean createAnnouncement(AnnouncementUpsertDTO request, Long creatorId) {
        validateUpsertRequest(request);

        Announcement announcement = new Announcement();
        announcement.setTitle(request.title().trim());
        announcement.setContent(request.content().trim());
        announcement.setStatus(AnnouncementStatus.DRAFT.getCode());
        announcement.setIsPinned(normalizePinned(request.isPinned()));
        announcement.setPublishTime(parseDateTime(request.publishTime()));
        announcement.setExpireTime(parseDateTime(request.expireTime()));
        announcement.setCreatorId(creatorId);

        validateTimeRange(announcement.getPublishTime(), announcement.getExpireTime());
        return announcementMapper.insert(announcement) > 0;
    }

    @Override
    public Boolean updateAnnouncement(Long id, AnnouncementUpsertDTO request) {
        Announcement existed = announcementMapper.findById(id);
        if (existed == null) {
            throw new InvalidParameterException("announcement.id");
        }

        validateUpsertRequest(request);

        Announcement announcement = new Announcement();
        announcement.setId(id);
        announcement.setTitle(request.title().trim());
        announcement.setContent(request.content().trim());
        announcement.setIsPinned(normalizePinned(request.isPinned()));
        announcement.setPublishTime(parseDateTime(request.publishTime()));
        announcement.setExpireTime(parseDateTime(request.expireTime()));

        validateTimeRange(announcement.getPublishTime(), announcement.getExpireTime());
        return announcementMapper.updateAnnouncement(announcement) > 0;

    }

    @Override
    public Boolean publishAnnouncement(Long id) {
        Announcement existed = announcementMapper.findById(id);
        if (existed == null) {
            throw new InvalidParameterException("announcement.id");
        }
        return announcementMapper.updateStatus(id, AnnouncementStatus.PUBLISHED.getCode()) > 0;
    }

    @Override
    public Boolean offlineAnnouncement(Long id) {
        Announcement existed = announcementMapper.findById(id);
        if (existed == null) {
            throw new InvalidParameterException("announcement.id");
        }
        return announcementMapper.updateStatus(id, AnnouncementStatus.OFFLINE.getCode()) > 0;
    }

    @Override
    public Boolean deleteAnnouncement(Long id) {
        Announcement existed = announcementMapper.findById(id);
        if (existed == null) {
            throw new InvalidParameterException("announcement.id");
        }
        return announcementMapper.deleteById(id) > 0;
    }
}
