
package com.taskmanagement.repository;

import com.taskmanagement.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByUserId(Long userId);

    List<Task> findByUserIdAndStatus(Long userId, String status);

    List<Task> findByUserIdAndPriority(Long userId, String priority);

    List<Task> findByUserIdAndCategoryId(Long userId, Long categoryId);
    
    boolean existsByCategoryId(Long categoryId);
    void deleteByUserId(Long userId);

}
