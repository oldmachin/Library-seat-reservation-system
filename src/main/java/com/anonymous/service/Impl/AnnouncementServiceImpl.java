package com.anonymous.service.Impl;

import com.anonymous.common.exception.InvalidParameterException;
import com.anonymous.mapper.AnnouncementMapper;
import com.anonymous.model.Announcement;
import com.anonymous.service.AnnouncementService;
import com.anonymous.vo.AnnouncementVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnnouncementServiceImpl implements AnnouncementService {

    @Autowired
    private AnnouncementMapper announcementMapper;

    private AnnouncementVO toAnnouncementVO(Announcement announcement) {
        return new AnnouncementVO(
                announcement.getId(),
                announcement.getTitle(),
                announcement.getContent(),
                announcement.getIsPinned() != null && announcement.getIsPinned() == 1,
                announcement.getPublishTime(),
                announcement.getExpireTime()
        );
    }

    @Override
    public List<AnnouncementVO> listPublishedAnnouncements() {
        return announcementMapper.findPublishedActive().stream()
                .map(this::toAnnouncementVO)
                .toList();
    }

    @Override
    public AnnouncementVO getPublishedAnnouncement(Long id) {
        Announcement announcement = announcementMapper.findPublishedActiveById(id);
        if (announcement == null) {
            throw new InvalidParameterException("announcement.id");
        }
        return toAnnouncementVO(announcement);
    }
}
