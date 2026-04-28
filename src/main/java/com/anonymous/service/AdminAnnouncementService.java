package com.anonymous.service;

import com.anonymous.common.Page;
import com.anonymous.dto.AnnouncementUpsertDTO;
import com.anonymous.vo.AnnouncementAdminVO;

public interface AdminAnnouncementService {

    Page<AnnouncementAdminVO> listAnnouncements(int pageNum, int pageSize);

    AnnouncementAdminVO getAnnouncement(Long id);

    Boolean createAnnouncement(AnnouncementUpsertDTO request, Long creatorId);

    Boolean updateAnnouncement(Long id, AnnouncementUpsertDTO request);

    Boolean publishAnnouncement(Long id);

    Boolean offlineAnnouncement(Long id);

    Boolean deleteAnnouncement(Long id);
}
