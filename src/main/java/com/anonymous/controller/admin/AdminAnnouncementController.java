package com.anonymous.controller.admin;

import com.anonymous.common.Page;
import com.anonymous.common.Result;
import com.anonymous.common.util.SecurityUtils;
import com.anonymous.dto.AnnouncementUpsertDTO;
import com.anonymous.service.AdminAnnouncementService;
import com.anonymous.vo.AnnouncementAdminVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/announcement")
public class AdminAnnouncementController {

    @Autowired
    private AdminAnnouncementService adminAnnouncementService;

    @GetMapping
    public Result<Page<AnnouncementAdminVO>> listAnnouncements(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        return Result.success(
                adminAnnouncementService.listAnnouncements(pageNum, pageSize),
                "查询公告列表成功"
        );
    }

    @GetMapping("/{id}")
    public Result<AnnouncementAdminVO> getAnnouncement(@PathVariable Long id) {
        return Result.success(adminAnnouncementService.getAnnouncement(id), "查询公告成功");
    }

    @PostMapping
    public Result<Boolean> createAnnouncement(@RequestBody AnnouncementUpsertDTO request) {
        Long creatorId = SecurityUtils.getCurrentUserId();
        return Result.success(
                adminAnnouncementService.createAnnouncement(request, creatorId),
                "新增公告成功"
        );
    }

    @PutMapping("/{id}")
    public Result<Boolean> updateAnnouncement(@PathVariable Long id,
                                              @RequestBody AnnouncementUpsertDTO request) {
        return Result.success(
                adminAnnouncementService.updateAnnouncement(id, request),
                "更新公告成功"
        );
    }

    @PutMapping("/{id}/publish")
    public Result<Boolean> publishAnnouncement(@PathVariable Long id) {
        return Result.success(
                adminAnnouncementService.publishAnnouncement(id),
                "发布公告成功"
        );
    }

    @PutMapping("/{id}/offline")
    public Result<Boolean> offlineAnnouncement(@PathVariable Long id) {
        return Result.success(
                adminAnnouncementService.offlineAnnouncement(id),
                "下线公告成功"
        );
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteAnnouncement(@PathVariable Long id) {
        return Result.success(
                adminAnnouncementService.deleteAnnouncement(id),
                "删除公告成功"
        );
    }
}
