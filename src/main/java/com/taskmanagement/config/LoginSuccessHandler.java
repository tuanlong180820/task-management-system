
package com.taskmanagement.config;

import java.io.IOException;
import java.util.Locale;

import com.taskmanagement.service.LanguageService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.LocaleResolver;

@Component
public class LoginSuccessHandler
        implements AuthenticationSuccessHandler {

    private final LanguageService languageService;
    private final LocaleResolver localeResolver;

    public LoginSuccessHandler(
            LanguageService languageService,
            LocaleResolver localeResolver) {

        this.languageService = languageService;
        this.localeResolver = localeResolver;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication)
            throws IOException, ServletException {

        Locale userLocale = languageService.getUserLocale(
                authentication.getName());

        // Khôi phục ngôn ngữ từ MySQL
        localeResolver.setLocale(
                request,
                response,
                userLocale);

        // Chuyển đến Dashboard
        response.sendRedirect(
                request.getContextPath() + "/dashboard");
    }
}
