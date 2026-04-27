package com.anonymous.service;

import com.anonymous.common.Page;
import com.anonymous.model.User;
import com.anonymous.vo.ReputationRecordVO;
import com.anonymous.vo.UserReputationVO;

public interface UserService {
    public User login(String username, String password);

    public User findById(Long id);

    public boolean updateProfile(Long id, String name);

    public boolean changePassword(Long id, String oldPassword, String newPassword);

    public UserReputationVO getUserReputation(Long id);

    Page<ReputationRecordVO> getReputationRecords(Long userId, int pageNum, int pageSize);
}
