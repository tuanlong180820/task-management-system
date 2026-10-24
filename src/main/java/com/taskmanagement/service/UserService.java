
package com.taskmanagement.service;

import com.taskmanagement.entity.User;
import com.taskmanagement.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(String fullName,
                         String email,
                         String password) {

        String normalizedEmail = email.trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException(
                    "Email đã được sử dụng");
        }

        User user = new User();
        user.setFullName(fullName.trim());
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(password));

        return userRepository.save(user);
    }
    
    @Transactional
    public void updateProfile(
            String currentEmail,
            String fullName) {

        if (fullName == null || fullName.isBlank()
                || fullName.trim().length() > 100) {
            throw new IllegalArgumentException(
                    "Họ tên phải từ 1 đến 100 ký tự");
        }

        User user = userRepository
                .findByEmail(currentEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Không tìm thấy tài khoản"));

        user.setFullName(fullName.trim());
        user.setUpdatedAt(java.time.LocalDateTime.now());

        userRepository.save(user);
    }
    
    @Transactional
    public void changePassword(
            String email,
            String currentPassword,
            String newPassword,
            String confirmPassword) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Không tìm thấy tài khoản"));

        // Kiểm tra mật khẩu hiện tại
        if (!passwordEncoder.matches(
                currentPassword, user.getPassword())) {
            throw new IllegalArgumentException(
                    "Mật khẩu hiện tại không chính xác");
        }

        // Kiểm tra độ dài mật khẩu mới
        if (newPassword == null
                || newPassword.length() < 8) {
            throw new IllegalArgumentException(
                    "Mật khẩu mới phải có ít nhất 8 ký tự");
        }

        // Kiểm tra xác nhận mật khẩu
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException(
                    "Xác nhận mật khẩu không khớp");
        }

        // Không cho phép sử dụng lại mật khẩu cũ
        if (passwordEncoder.matches(
                newPassword, user.getPassword())) {
            throw new IllegalArgumentException(
                    "Mật khẩu mới phải khác mật khẩu hiện tại");
        }

        // Mã hóa và lưu mật khẩu mới
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(java.time.LocalDateTime.now());

        userRepository.save(user);
    }


}
