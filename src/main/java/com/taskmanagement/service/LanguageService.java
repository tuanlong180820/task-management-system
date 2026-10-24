
package com.taskmanagement.service;

import java.util.Locale;

import com.taskmanagement.entity.Setting;
import com.taskmanagement.entity.User;
import com.taskmanagement.repository.SettingRepository;
import com.taskmanagement.repository.UserRepository;

import org.springframework.stereotype.Service;

@Service
public class LanguageService {

    private final SettingRepository settingRepository;
    private final UserRepository userRepository;

    public LanguageService(
            SettingRepository settingRepository,
            UserRepository userRepository) {

        this.settingRepository = settingRepository;
        this.userRepository = userRepository;
    }

    public Locale getUserLocale(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                    new IllegalArgumentException(
                        "Không tìm thấy người dùng"));

        // Tìm cài đặt ngôn ngữ đã lưu
        Setting setting = settingRepository
                .findByUserId(user.getId())
                .orElse(null);

        if (setting != null &&
                "en".equalsIgnoreCase(setting.getLanguage())) {
            return Locale.ENGLISH;
        }

        return Locale.forLanguageTag("vi");
    }
}
