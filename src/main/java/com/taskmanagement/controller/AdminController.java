
package com.taskmanagement.controller;

import com.taskmanagement.repository.UserRepository;
import com.taskmanagement.repository.TaskRepository;
import com.taskmanagement.repository.CategoryRepository;
import com.taskmanagement.repository.ActivityLogRepository;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import com.taskmanagement.entity.User;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@Controller
public class AdminController {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final CategoryRepository categoryRepository;
    private final ActivityLogRepository activityLogRepository;

    public AdminController(
            UserRepository userRepository,
            TaskRepository taskRepository,
            CategoryRepository categoryRepository,
            ActivityLogRepository activityLogRepository) {

        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.categoryRepository = categoryRepository;
        this.activityLogRepository = activityLogRepository;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public String adminDashboard(Model model) {

        model.addAttribute("totalUsers",
                userRepository.count());

        model.addAttribute("totalTasks",
                taskRepository.count());

        model.addAttribute("totalCategories",
                categoryRepository.count());

        model.addAttribute("totalActivities",
                activityLogRepository.count());

        return "admin-dashboard";
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/users")
    public String listUsers(
            @RequestParam(defaultValue = "") String keyword,
            Model model) {

        String searchKeyword = keyword.trim();

        List<User> users;

        if (searchKeyword.isEmpty()) {
            users = userRepository.findAll(
                    org.springframework.data.domain.Sort
                            .by(org.springframework.data.domain.Sort.Direction.DESC, "id")
            );
        } else {
            users = userRepository
                    .findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrderByIdDesc(
                            searchKeyword,
                            searchKeyword
                    );
        }

        model.addAttribute("users", users);
        model.addAttribute("keyword", searchKeyword);

        return "admin-users";
    }

}
