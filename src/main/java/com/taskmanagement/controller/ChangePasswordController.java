
package com.taskmanagement.controller;

import com.taskmanagement.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequestMapping("/change-password")
public class ChangePasswordController {

    private final UserService userService;

    public ChangePasswordController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String showForm() {
        return "change-password";
    }

    @PostMapping
    public String changePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            Authentication authentication,
            HttpServletRequest request,
            HttpServletResponse response,
            Model model,RedirectAttributes redirectAttributes) {

        try {
            userService.changePassword(
                    authentication.getName(),
                    currentPassword,
                    newPassword,
                    confirmPassword
            );

            new SecurityContextLogoutHandler().logout(
                    request, response, authentication);

            return "redirect:/login?passwordChanged";

        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute(
            "error", ex.getMessage());

            return "redirect:/settings?tab=security";
        }
    }
}
