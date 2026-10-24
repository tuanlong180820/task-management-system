
package com.taskmanagement.controller;

import com.taskmanagement.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class RegisterController {

    private final UserService userService;

    public RegisterController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String showRegisterPage() {
        return "register";
    }

    @PostMapping("/register")
    public String register(
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String password,
            Model model) {

        if (fullName.isBlank()
                || email.isBlank()
                || password.length() < 8) {
            model.addAttribute("error",
                    "Vui lòng nhập đầy đủ thông tin. "
                    + "Mật khẩu phải có ít nhất 8 ký tự.");
            return "register";
        }

        try {
            userService.register(fullName, email, password);
            return "redirect:/login?registered";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            return "register";
        }
    }
}
