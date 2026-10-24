
package com.taskmanagement.repository;

import com.taskmanagement.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Notification> findByUserIdAndIsReadFalse(Long userId);
    
    boolean existsByTaskIdAndTitle(Long taskId, String title);
    void deleteByUserId(Long userId);
    void deleteByTaskId(Long taskId);

}
