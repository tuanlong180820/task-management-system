
package com.taskmanagement.service;

import com.taskmanagement.entity.ActivityLog;
import com.taskmanagement.entity.User;
import com.taskmanagement.repository.ActivityLogRepository;
import com.taskmanagement.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;

    public ActivityLogService(
            ActivityLogRepository activityLogRepository,
            UserRepository userRepository) {

        this.activityLogRepository = activityLogRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void recordActivity(
            Long userId,
            String action,
            String description) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Không tìm thấy người dùng"));

        ActivityLog log = new ActivityLog();

        log.setUser(user);
        log.setAction(action);
        log.setDescription(description);
        log.setCreatedAt(LocalDateTime.now());

        activityLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public List<ActivityLog> getActivityLogs(Long userId) {

        return activityLogRepository
                .findByUserIdOrderByCreatedAtDesc(userId);
    }
}
