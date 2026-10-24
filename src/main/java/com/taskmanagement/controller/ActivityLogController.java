
package com.taskmanagement.controller;

import com.taskmanagement.entity.User;
import com.taskmanagement.repository.UserRepository;
import com.taskmanagement.service.ActivityLogService;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ActivityLogController {

    private final ActivityLogService activityLogService;
    private final UserRepository userRepository;

    public ActivityLogController(
            ActivityLogService activityLogService,
            UserRepository userRepository) {

        this.activityLogService = activityLogService;
        this.userRepository = userRepository;
    }

    @GetMapping("/activity-logs")
    public String showActivityLogs(
            Authentication authentication,
            Model model) {

        User user = userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                    new IllegalStateException(
                        "Không tìm thấy người dùng"));

        model.addAttribute(
                "activityLogs",
                activityLogService.getActivityLogs(user.getId()));

        return "activity-logs";
    }
}
