
package com.taskmanagement.service;

import com.taskmanagement.entity.Setting;
import com.taskmanagement.entity.User;
import com.taskmanagement.repository.SettingRepository;
import com.taskmanagement.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SettingService {

    private final SettingRepository settingRepository;
    private final UserRepository userRepository;

    public SettingService(
            SettingRepository settingRepository,
            UserRepository userRepository) {
        this.settingRepository = settingRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Setting getOrCreateSetting(Long userId) {
        return settingRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow();

                    Setting setting = new Setting();
                    setting.setUser(user);

                    return settingRepository.save(setting);
                });
    }

    @Transactional
    public void updateSetting(
            Long userId,
            String theme,
            boolean notificationsEnabled,
            String language) {

        if (!java.util.Set.of("LIGHT", "DARK")
                .contains(theme)) {
            throw new IllegalArgumentException(
                    "Giao diện không hợp lệ");
        }

        if (!java.util.Set.of("vi", "en")
                .contains(language)) {
            throw new IllegalArgumentException(
                    "Ngôn ngữ không hợp lệ");
        }

        Setting setting = getOrCreateSetting(userId);

        setting.setTheme(theme);
        setting.setNotificationsEnabled(notificationsEnabled);
        setting.setLanguage(language);
        setting.setUpdatedAt(java.time.LocalDateTime.now());

        settingRepository.save(setting);
    }
}
