
package com.taskmanagement.service;

import com.taskmanagement.entity.User;
import com.taskmanagement.repository.*;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountDeletionService {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final CategoryRepository categoryRepository;
    private final NotificationRepository notificationRepository;
    private final ActivityLogRepository activityLogRepository;
    private final SettingRepository settingRepository;
    private final PasswordEncoder passwordEncoder;

    public AccountDeletionService(
            UserRepository userRepository,
            TaskRepository taskRepository,
            CategoryRepository categoryRepository,
            NotificationRepository notificationRepository,
            ActivityLogRepository activityLogRepository,
            SettingRepository settingRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.categoryRepository = categoryRepository;
        this.notificationRepository = notificationRepository;
        this.activityLogRepository = activityLogRepository;
        this.settingRepository = settingRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void deleteAccount(
            String email,
            String currentPassword) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Không tìm thấy tài khoản"));

        if (currentPassword == null ||
                !passwordEncoder.matches(
                        currentPassword, user.getPassword())) {
            throw new IllegalArgumentException(
                    "Mật khẩu xác nhận không chính xác");
        }

        Long userId = user.getId();

        // Xóa dữ liệu theo thứ tự khóa ngoại
        notificationRepository.deleteByUserId(userId);
        activityLogRepository.deleteByUserId(userId);

        taskRepository.deleteByUserId(userId);

        categoryRepository.deleteByUserId(userId);
        settingRepository.deleteByUserId(userId);

        // Xóa tài khoản sau cùng
        userRepository.delete(user);

        // Đẩy các lệnh thay đổi xuống Database
        userRepository.flush();
    }
}
