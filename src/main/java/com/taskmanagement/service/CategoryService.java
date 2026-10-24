
package com.taskmanagement.service;

import com.taskmanagement.entity.Category;
import com.taskmanagement.entity.User;
import com.taskmanagement.repository.CategoryRepository;
import com.taskmanagement.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.taskmanagement.repository.TaskRepository;

import java.util.List;

@Service
public class CategoryService {

    private final TaskRepository taskRepository;
    private final ActivityLogService activityLogService;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    
        
        public CategoryService(
                CategoryRepository categoryRepository,
                UserRepository userRepository,
                TaskRepository taskRepository,
                ActivityLogService activityLogService) {

        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.activityLogService = activityLogService;
        }



    @Transactional(readOnly = true)
    public List<Category> getCategoriesByUser(Long userId) {
        return categoryRepository.findByUserId(userId);
    }

    @Transactional
    public Category createCategory(
            Long userId,
            String name,
            String description,
            String color) {

        if (name == null || name.isBlank()
                || name.trim().length() > 100) {
            throw new IllegalArgumentException(
                    "Tên danh mục phải từ 1 đến 100 ký tự");
        }

        String normalizedName = name.trim();

        if (categoryRepository.existsByNameAndUserId(
                normalizedName, userId)) {
            throw new IllegalArgumentException(
                    "Danh mục này đã tồn tại");
        }

        if (color == null
                || !color.matches("#[0-9a-fA-F]{6}")) {
            throw new IllegalArgumentException(
                    "Màu sắc không hợp lệ");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Không tìm thấy người dùng"));

        Category category = new Category();
        category.setName(normalizedName);
        category.setDescription(description);
        category.setColor(color);
        category.setUser(user);

        
        Category savedCategory = categoryRepository.save(category);

        activityLogService.recordActivity(
                userId,
                "CREATE_CATEGORY",
                "Đã tạo danh mục \"" + savedCategory.getName() + "\""
        );

        return savedCategory;

    }
    
    @Transactional(readOnly = true)
    public Category getCategoryById(Long categoryId, Long userId) {

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Không tìm thấy danh mục"));

        if (!category.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException(
                    "Không tìm thấy danh mục");
        }

        return category;
    }

    @Transactional
    public Category updateCategory(
            Long categoryId,
            Long userId,
            String name,
            String description,
            String color) {

        Category category = getCategoryById(categoryId, userId);

        if (name == null || name.isBlank()
                || name.trim().length() > 100) {
            throw new IllegalArgumentException(
                    "Tên danh mục phải từ 1 đến 100 ký tự");
        }

        String normalizedName = name.trim();

        if (!category.getName().equals(normalizedName)
                && categoryRepository.existsByNameAndUserId(
                        normalizedName, userId)) {
            throw new IllegalArgumentException(
                    "Tên danh mục đã tồn tại");
        }

        if (color == null
                || !color.matches("#[0-9a-fA-F]{6}")) {
            throw new IllegalArgumentException(
                    "Màu sắc không hợp lệ");
        }

        category.setName(normalizedName);
        category.setDescription(description);
        category.setColor(color);
        category.setUpdatedAt(java.time.LocalDateTime.now());

        
        Category updatedCategory = categoryRepository.save(category);

        activityLogService.recordActivity(
                userId,
                "UPDATE_CATEGORY",
                "Đã cập nhật danh mục \"" + updatedCategory.getName() + "\""
        );

        return updatedCategory;

    }
    
        
        
        @Transactional
        public void deleteCategory(Long categoryId, Long userId) {

        Category category = getCategoryById(categoryId, userId);

        if (taskRepository.existsByCategoryId(categoryId)) {
                throw new IllegalStateException(
                        "Không thể xóa danh mục vì đang có " +
                        "công việc sử dụng danh mục này.");
        }

        String categoryName = category.getName();

        categoryRepository.delete(category);

        activityLogService.recordActivity(
                userId,
                "DELETE_CATEGORY",
                "Đã xóa danh mục \"" + categoryName + "\""
        );
        }




}
