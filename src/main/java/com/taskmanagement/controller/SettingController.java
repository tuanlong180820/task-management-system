
package com.taskmanagement.controller;

import com.taskmanagement.entity.User;
import com.taskmanagement.repository.UserRepository;
import com.taskmanagement.service.SettingService;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.taskmanagement.service.AccountDeletionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.Locale;


import org.springframework.web.servlet.LocaleResolver;

@Controller
@RequestMapping("/settings")
public class SettingController {

    private final SettingService settingService;
    private final UserRepository userRepository;
    private final AccountDeletionService accountDeletionService;
    private final LocaleResolver localeResolver;


    public SettingController(
            SettingService settingService,
            UserRepository userRepository,
            AccountDeletionService accountDeletionService,
            LocaleResolver localeResolver) {
        this.settingService = settingService;
        this.userRepository = userRepository;
        this.accountDeletionService = accountDeletionService;
        this.localeResolver = localeResolver;
    }

    
    @GetMapping
    public String showSettings(
            Authentication authentication,
            @RequestParam(defaultValue = "profile") String tab,
            Model model) {

        User user = getCurrentUser(authentication);

        if (!java.util.Set.of(
                "profile", "security", "preferences")
                .contains(tab)) {
            tab = "profile";
        }

        model.addAttribute("user", user);
        model.addAttribute("setting",
                settingService.getOrCreateSetting(user.getId()));
        model.addAttribute("activeTab", tab);

        return "settings";
    }

    @PostMapping
    public String updateSettings(
            @RequestParam String theme,
            @RequestParam(defaultValue = "false")
            boolean notificationsEnabled,
            @RequestParam String language,
            Authentication authentication,
            HttpServletRequest request,
            HttpServletResponse response) {

        User user = getCurrentUser(authentication);
         
        settingService.updateSetting(
                user.getId(),
                theme,
                notificationsEnabled,
                language);
        
        
        Locale selectedLocale = "en".equalsIgnoreCase(language)
                ? Locale.ENGLISH
                : Locale.forLanguageTag("vi");

        localeResolver.setLocale(
                request,
                response,
                selectedLocale
        );


       

        return "redirect:/settings?tab=preferences&success";
    }

    private User getCurrentUser(Authentication authentication) {
        return userRepository
                .findByEmail(authentication.getName())
                .orElseThrow();
    }
    
    @PostMapping("/delete-account")
    public String deleteAccount(
            @RequestParam String currentPassword,
            Authentication authentication,
            HttpServletRequest request,
            HttpServletResponse response,
            RedirectAttributes redirectAttributes) {

        try {
            accountDeletionService.deleteAccount(
                    authentication.getName(),
                    currentPassword);

            new SecurityContextLogoutHandler().logout(
                    request, response, authentication);

            return "redirect:/login?accountDeleted";

        } catch (IllegalArgumentException ex) {

            redirectAttributes.addFlashAttribute(
                    "error", ex.getMessage());

            return "redirect:/settings?tab=security";
        }
    }

}
