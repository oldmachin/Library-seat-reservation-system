package com.anonymous.service;

import com.anonymous.vo.AnnouncementVO;

import java.util.List;

public interface AnnouncementService {

    List<AnnouncementVO> listPublishedAnnouncements();

    AnnouncementVO getPublishedAnnouncement(Long id);
}
