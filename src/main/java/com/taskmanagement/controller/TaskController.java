
package com.taskmanagement.controller;

import com.taskmanagement.entity.User;
import com.taskmanagement.repository.UserRepository;
import com.taskmanagement.service.TaskService;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.taskmanagement.service.CategoryService;
import java.time.LocalDate;

@Controller
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;
    private final UserRepository userRepository;
        private final CategoryService categoryService;
    public TaskController(
            TaskService taskService,
            UserRepository userRepository,
        CategoryService categoryService) {
        this.taskService = taskService;
        this.userRepository = userRepository;
        this.categoryService = categoryService;
    }

    
        @GetMapping("/new")
        public String showCreateForm(
                Authentication authentication,
                Model model) {

        User user = userRepository
                .findByEmail(authentication.getName())
                .orElseThrow();

        model.addAttribute(
                "categories",
                categoryService.getCategoriesByUser(user.getId()));

        return "task-create";
        }


    @PostMapping("/create")
    public String createTask(
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam String priority,
            @RequestParam(required = false) LocalDate dueDate,
            @RequestParam(required = false) Long categoryId,
            Authentication authentication,
            Model model) {

        if (title == null || title.isBlank()) {
            model.addAttribute(
                    "error",
                    "Tên công việc không được để trống");
            return "task-create";
        }

        if (!title.trim().isEmpty()
                && title.trim().length() > 200) {
            model.addAttribute(
                    "error",
                    "Tên công việc không được quá 200 ký tự");
            return "task-create";
        }

        if (!java.util.Set.of("LOW", "MEDIUM", "HIGH")
                .contains(priority)) {
            model.addAttribute(
                    "error",
                    "Mức độ ưu tiên không hợp lệ");
            return "task-create";
        }

        User user = userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Không tìm thấy người dùng"));

        taskService.createTask(
                user.getId(),
                title,
                description,
                priority,
                dueDate,
                categoryId
        );

        return "redirect:/dashboard";
    }
    
@GetMapping("/{id}/edit")
public String showEditForm(
        @PathVariable Long id,
        Authentication authentication,
        Model model) {

    User user = userRepository
            .findByEmail(authentication.getName())
            .orElseThrow(() ->
                    new IllegalStateException(
                            "Không tìm thấy người dùng"));

    var task = taskService
            .getTaskById(id, user.getId())
            .orElseThrow(() ->
                    new org.springframework.web.server
                            .ResponseStatusException(
                            org.springframework.http.HttpStatus.NOT_FOUND,
                            "Không tìm thấy công việc"));

    model.addAttribute("task", task);
    
        model.addAttribute(
                "categories",
                categoryService.getCategoriesByUser(user.getId()));


    return "task-edit";
}

    @PostMapping("/{id}/edit")
    public String updateTask(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam String status,
            @RequestParam String priority,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false)
            @org.springframework.format.annotation.DateTimeFormat(
                    iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
            LocalDate dueDate,
            Authentication authentication,
            Model model) {

        if (title == null || title.isBlank()
                || title.trim().length() > 200) {
            model.addAttribute("error",
                    "Tên công việc phải từ 1 đến 200 ký tự");
            return loadEditForm(id, authentication, model);
        }

        if (!java.util.Set.of("TODO", "IN_PROGRESS", "DONE")
                .contains(status)
                || !java.util.Set.of("LOW", "MEDIUM", "HIGH")
                .contains(priority)) {
            model.addAttribute("error",
                    "Trạng thái hoặc mức ưu tiên không hợp lệ");
            return loadEditForm(id, authentication, model);
        }

        User user = userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Không tìm thấy người dùng"));

        taskService.updateTask(
                id,
                user.getId(),
                title,
                description,
                status,
                priority,
                dueDate,
                categoryId
        );

        return "redirect:/dashboard";
    }

    private String loadEditForm(
            Long id,
            Authentication authentication,
            Model model) {

        User user = userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Không tìm thấy người dùng"));

        var task = taskService
                .getTaskById(id, user.getId())
                .orElseThrow(() ->
                        new org.springframework.web.server
                                .ResponseStatusException(
                                org.springframework.http.HttpStatus.NOT_FOUND,
                                "Không tìm thấy công việc"));

        model.addAttribute("task", task);

        return "task-edit";
    }
    
    @PostMapping("/{id}/delete")
    public String deleteTask(
            @PathVariable Long id,
            Authentication authentication) {

        User user = userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Không tìm thấy người dùng"));

        try {
            taskService.deleteTask(id, user.getId());
        } catch (IllegalArgumentException ex) {
            throw new org.springframework.web.server
                    .ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND,
                        "Không tìm thấy công việc");
        }

        return "redirect:/dashboard";
    }


                    

}
