
package com.taskmanagement.controller;

import com.taskmanagement.entity.User;
import com.taskmanagement.repository.UserRepository;
import com.taskmanagement.service.UserService;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserRepository userRepository;
    private final UserService userService;

    public ProfileController(
            UserRepository userRepository,
            UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @GetMapping
    public String showProfile(
            Authentication authentication,
            Model model) {

        User user = userRepository
                .findByEmail(authentication.getName())
                .orElseThrow();

        model.addAttribute("user", user);

        return "profile";
    }

    @PostMapping
    public String updateProfile(
            @RequestParam String fullName,
            Authentication authentication,
            Model model,RedirectAttributes redirectAttributes) {

        try {
            userService.updateProfile(
                    authentication.getName(),
                    fullName);
                redirectAttributes.addFlashAttribute(
        "success", "Cập nhật hồ sơ thành công!");

        return "redirect:/settings?tab=profile";

            

        } catch (IllegalArgumentException ex) {
            User user = userRepository
                    .findByEmail(authentication.getName())
                    .orElseThrow();

            model.addAttribute("user", user);
            model.addAttribute("error", ex.getMessage());

            redirectAttributes.addFlashAttribute(
            "error", ex.getMessage());

            return "redirect:/settings?tab=profile";
        }
    }
}
