
package com.taskmanagement.service;

import com.taskmanagement.entity.Notification;
import com.taskmanagement.entity.Task;
import com.taskmanagement.repository.NotificationRepository;
import com.taskmanagement.repository.TaskRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import com.taskmanagement.entity.Setting;
import com.taskmanagement.repository.SettingRepository;


@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final TaskRepository taskRepository;
    private final SettingRepository settingRepository;


    
    public NotificationService(
            NotificationRepository notificationRepository,
            TaskRepository taskRepository,
            SettingRepository settingRepository) {

        this.notificationRepository = notificationRepository;
        this.taskRepository = taskRepository;
        this.settingRepository = settingRepository;
    }


    @Transactional
    public void checkTaskDeadlines(Long userId) {
        Setting setting = settingRepository.findByUserId(userId)
                .orElse(null);

        // Nếu người dùng tắt thông báo thì không tạo mới
        if (setting != null && !setting.isNotificationsEnabled()) {
            return;
        }

        List<Task> tasks = taskRepository.findByUserId(userId);

        LocalDate today = LocalDate.now();
        
        


        for (Task task : tasks) {

            // Bỏ qua công việc đã hoàn thành
            if ("DONE".equals(task.getStatus())) {
                continue;
            }

            // Bỏ qua công việc không có hạn
            if (task.getDueDate() == null) {
                continue;
            }

            String title;
            String message;

            if (task.getDueDate().isBefore(today)) {

                title = "Công việc đã quá hạn";

                message = "Công việc \"" + task.getTitle()
                        + "\" đã quá hạn.";

            } else if (task.getDueDate().isEqual(today)) {

                title = "Công việc đến hạn hôm nay";

                message = "Công việc \"" + task.getTitle()
                        + "\" cần hoàn thành hôm nay.";

            } else if (task.getDueDate()
                    .isEqual(today.plusDays(1))) {

                title = "Công việc sắp đến hạn";

                message = "Công việc \"" + task.getTitle()
                        + "\" sẽ đến hạn vào ngày mai.";

            } else {
                continue;
            }

            // Tránh tạo thông báo trùng
            if (notificationRepository
                    .existsByTaskIdAndTitle(
                            task.getId(), title)) {
                continue;
            }

            Notification notification = new Notification();

            notification.setTitle(title);
            notification.setMessage(message);
            notification.setUser(task.getUser());
            notification.setTask(task);
            notification.setRead(false);
            notification.setCreatedAt(LocalDateTime.now());

            notificationRepository.save(notification);
        }
    }

    @Transactional(readOnly = true)
    public List<Notification> getNotificationsByUser(Long userId) {

        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    @Transactional(readOnly = true)
    public long countUnreadNotifications(Long userId) {
        return notificationRepository
                .findByUserIdAndIsReadFalse(userId)
                .size();
    }

    @Transactional
    public void markAsRead(Long notificationId, Long userId) {

        Notification notification = notificationRepository
                .findById(notificationId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Không tìm thấy thông báo"));

        // Chỉ cho phép người dùng sửa thông báo của mình
        if (!notification.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException(
                    "Bạn không có quyền truy cập thông báo này");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

}
