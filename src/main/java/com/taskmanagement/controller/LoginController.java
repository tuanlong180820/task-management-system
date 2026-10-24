
package com.taskmanagement.controller;

import com.taskmanagement.entity.User;
import com.taskmanagement.repository.UserRepository;
import com.taskmanagement.service.TaskService;
import com.taskmanagement.service.NotificationService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.taskmanagement.service.SettingService;

@Controller
public class LoginController {
    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final TaskService taskService;
    private final SettingService settingService;

    public LoginController(
            UserRepository userRepository,
            NotificationService notificationService,
            TaskService taskService,SettingService settingService) {
        this.userRepository = userRepository;
        this.taskService = taskService;
        this.notificationService = notificationService;
        this.settingService = settingService;
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    
    @GetMapping("/dashboard")
    public String showDashboard(
            Authentication authentication,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "") String status,
            @RequestParam(defaultValue = "") String priority,
            Model model) {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Không tìm thấy người dùng"));
                    
                notificationService.checkTaskDeadlines(user.getId());
                long unreadCount =
        notificationService.countUnreadNotifications(user.getId());
        var setting = settingService.getOrCreateSetting(user.getId());

        model.addAttribute("theme", setting.getTheme());

        model.addAttribute("unreadCount", unreadCount);


        model.addAttribute("fullName", user.getFullName());

        model.addAttribute("tasks",
                taskService.searchTasks(
                        user.getId(),
                        keyword,
                        status,
                        priority));

        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("priority", priority);
        
        model.addAttribute(
                "statistics",
                taskService.getTaskStatistics(user.getId()));

        return "dashboard";
    }

}
