
package com.taskmanagement.controller;

import com.taskmanagement.entity.User;
import com.taskmanagement.repository.UserRepository;
import com.taskmanagement.service.CategoryService;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;
    private final UserRepository userRepository;

    public CategoryController(
            CategoryService categoryService,
            UserRepository userRepository) {
        this.categoryService = categoryService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String listCategories(
            Authentication authentication,
            Model model) {

        User user = getCurrentUser(authentication);

        model.addAttribute("categories",
                categoryService.getCategoriesByUser(
                        user.getId()));

        return "categories";
    }

    @GetMapping("/new")
    public String showCreateForm() {
        return "category-create";
    }

    @PostMapping("/create")
    public String createCategory(
            @RequestParam String name,
            @RequestParam(required = false)
            String description,
            @RequestParam String color,
            Authentication authentication,
            Model model) {

        User user = getCurrentUser(authentication);

        try {
            categoryService.createCategory(
                    user.getId(),
                    name,
                    description,
                    color);

            return "redirect:/categories";

        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            return "category-create";
        }
    }

    private User getCurrentUser(
            Authentication authentication) {

        return userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Không tìm thấy người dùng"));
    }
    
    @GetMapping("/{id}/edit")
    public String showEditForm(
            @PathVariable Long id,
            Authentication authentication,
            Model model) {

        User user = getCurrentUser(authentication);

        try {
            model.addAttribute(
                    "category",
                    categoryService.getCategoryById(
                            id, user.getId()));

            return "category-edit";

        } catch (IllegalArgumentException ex) {
            throw new org.springframework.web.server
                    .ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND,
                        "Không tìm thấy danh mục");
        }
    }

    @PostMapping("/{id}/edit")
    public String updateCategory(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestParam(required = false)
            String description,
            @RequestParam String color,
            Authentication authentication,
            Model model) {

        User user = getCurrentUser(authentication);

        try {
            categoryService.updateCategory(
                    id,
                    user.getId(),
                    name,
                    description,
                    color);

            return "redirect:/categories";

        } catch (IllegalArgumentException ex) {

            // Chỉ hiển thị lỗi nhập liệu nếu danh mục
            // thực sự thuộc người dùng hiện tại.
            try {
                model.addAttribute(
                        "category",
                        categoryService.getCategoryById(
                                id, user.getId()));
            } catch (IllegalArgumentException notFound) {
                throw new org.springframework.web.server
                        .ResponseStatusException(
                            org.springframework.http.HttpStatus.NOT_FOUND,
                            "Không tìm thấy danh mục");
            }

            model.addAttribute("error", ex.getMessage());
            return "category-edit";
        }
    }
    
    @PostMapping("/{id}/delete")
    public String deleteCategory(
            @PathVariable Long id,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        User user = getCurrentUser(authentication);

        try {
            categoryService.deleteCategory(
                    id,
                    user.getId());

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Xóa danh mục thành công!");

        } catch (IllegalStateException ex) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    ex.getMessage());

        } catch (IllegalArgumentException ex) {

            throw new org.springframework.web.server
                    .ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND,
                        "Không tìm thấy danh mục");
        }

        return "redirect:/categories";
    }


}
