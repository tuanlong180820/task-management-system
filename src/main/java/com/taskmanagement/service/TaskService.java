
package com.taskmanagement.service;

import com.taskmanagement.entity.Task;
import com.taskmanagement.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import com.taskmanagement.entity.User;
import com.taskmanagement.repository.UserRepository;
import com.taskmanagement.entity.Category;
import com.taskmanagement.repository.CategoryRepository;
import java.util.Map;

import com.taskmanagement.repository.NotificationRepository;
import com.taskmanagement.repository.ActivityLogRepository;

@Service
public class TaskService {
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TaskRepository taskRepository;
    private final ActivityLogService activityLogService;
    private final NotificationRepository notificationRepository;
    private final ActivityLogRepository activityLogRepository;

   


        public TaskService(
                TaskRepository taskRepository,
                UserRepository userRepository,
                CategoryRepository categoryRepository,
                ActivityLogService activityLogService,
                NotificationRepository notificationRepository,
                ActivityLogRepository activityLogRepository) {

        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.activityLogService = activityLogService;
        this.notificationRepository = notificationRepository;
        this.activityLogRepository = activityLogRepository;
        }




    @Transactional(readOnly = true)
    public List<Task> getTasksByUser(Long userId) {
        return taskRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Optional<Task> getTaskById(Long taskId, Long userId) {
        return taskRepository.findById(taskId)
                .filter(task -> task.getUser().getId().equals(userId));
    }

    @Transactional
    public Task saveTask(Task task) {
        return taskRepository.save(task);
    }

    
        
        @Transactional
        public void deleteTask(Long taskId, Long userId) {

        Task task = getTaskById(taskId, userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Không tìm thấy công việc"));

        String taskTitle = task.getTitle();

        // 1. Lấy các lịch sử liên quan đến Task
        var activityLogs = activityLogRepository
                .findByTaskIdOrderByCreatedAtDesc(taskId);

        // 2. Ngắt liên kết với Task
        // nhưng giữ nguyên bản ghi lịch sử
        for (var log : activityLogs) {
                log.setTask(null);
        }

        // Đồng bộ thay đổi khóa ngoại trước khi xóa Task
        activityLogRepository.saveAllAndFlush(activityLogs);

        // 3. Xóa thông báo liên quan đến Task
        notificationRepository.deleteByTaskId(taskId);

        // 4. Xóa công việc
        taskRepository.delete(task);

        // 5. Ghi lịch sử xóa công việc
        activityLogService.recordActivity(
                userId,
                "DELETE_TASK",
                "Đã xóa công việc \"" + taskTitle + "\""
        );
        }


    
        @Transactional
        public Task createTask(
                Long userId,
                String title,
                String description,
                String priority,
                LocalDate dueDate,
                Long categoryId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Không tìm thấy người dùng"));

        Task task = new Task();
        task.setTitle(title);
        task.setDescription(description);
        task.setPriority(priority);
        task.setDueDate(dueDate);
        task.setStatus("TODO");
        task.setUser(user);

        if (categoryId != null) {
                Category category = categoryRepository
                        .findById(categoryId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Không tìm thấy danh mục"));

                if (!category.getUser().getId().equals(userId)) {
                throw new IllegalArgumentException(
                        "Danh mục không thuộc người dùng hiện tại");
                }

                task.setCategory(category);
        }

        
                Task savedTask = taskRepository.save(task);

                activityLogService.recordActivity(
                        userId,
                        "CREATE_TASK",
                        "Đã tạo công việc \"" + savedTask.getTitle() + "\""
                );

                return savedTask;

        }

        
    
        @Transactional
        public Task updateTask(
                Long taskId,
                Long userId,
                String title,
                String description,
                String status,
                String priority,
                LocalDate dueDate,
                Long categoryId) {

        
                Task task = getTaskById(taskId, userId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Không tìm thấy công việc"));


        task.setTitle(title);
        task.setDescription(description);
        task.setStatus(status);
        task.setPriority(priority);
        task.setDueDate(dueDate);

        // Xử lý trạng thái hoàn thành
        if ("DONE".equals(status)) {
                if (task.getCompletedAt() == null) {
                task.setCompletedAt(
                        java.time.LocalDateTime.now());
                }
        } else {
                task.setCompletedAt(null);
        }

        // Xử lý danh mục
        if (categoryId == null) {
                task.setCategory(null);
        } else {
                Category category = categoryRepository
                        .findById(categoryId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Không tìm thấy danh mục"));

                if (!category.getUser().getId().equals(userId)) {
                throw new IllegalArgumentException(
                        "Danh mục không thuộc người dùng hiện tại");
                }

                task.setCategory(category);
        }

        task.setUpdatedAt(
                java.time.LocalDateTime.now());

        
        Task updatedTask = taskRepository.save(task);

        activityLogService.recordActivity(
                userId,
                "UPDATE_TASK",
                "Đã cập nhật công việc \"" + updatedTask.getTitle() + "\""
        );

        return updatedTask;

        }

    
    @Transactional(readOnly = true)
    public List<Task> searchTasks(
            Long userId,
            String keyword,
            String status,
            String priority) {

        String searchKeyword = keyword == null
                ? ""
                : keyword.trim().toLowerCase();

        return taskRepository.findByUserId(userId)
                .stream()
                .filter(task ->
                        searchKeyword.isEmpty()
                        || task.getTitle().toLowerCase()
                                .contains(searchKeyword))
                .filter(task ->
                        status == null || status.isBlank()
                        || status.equals(task.getStatus()))
                .filter(task ->
                        priority == null || priority.isBlank()
                        || priority.equals(task.getPriority()))
                .toList();
    }
    
        @Transactional(readOnly = true)
        public Map<String, Long> getTaskStatistics(Long userId) {

        List<Task> tasks = taskRepository.findByUserId(userId);

        long total = tasks.size();

        long todo = tasks.stream()
                .filter(task -> "TODO".equals(task.getStatus()))
                .count();

        long inProgress = tasks.stream()
                .filter(task -> "IN_PROGRESS".equals(task.getStatus()))
                .count();

        long done = tasks.stream()
                .filter(task -> "DONE".equals(task.getStatus()))
                .count();

        long overdue = tasks.stream()
                .filter(task ->
                        task.getDueDate() != null
                        && task.getDueDate().isBefore(LocalDate.now())
                        && !"DONE".equals(task.getStatus()))
                .count();

        Map<String, Long> statistics = new HashMap<>();

        statistics.put("total", total);
        statistics.put("todo", todo);
        statistics.put("inProgress", inProgress);
        statistics.put("done", done);
        statistics.put("overdue", overdue);

        return statistics;
        }


}
