package com.anonymous.controller;

import com.anonymous.common.Result;
import com.anonymous.service.AnnouncementService;
import com.anonymous.vo.AnnouncementVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/announcement")
public class AnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    @GetMapping
    public Result<List<AnnouncementVO>> getAnnouncements() {
        return Result.success(announcementService.listPublishedAnnouncements(), "查询成功");
    }

    @GetMapping("/{id}")
    public Result<AnnouncementVO> getAnnouncement(@PathVariable Long id) {
        return Result.success(announcementService.getPublishedAnnouncement(id), "查询成功");
    }
}
