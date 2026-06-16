package com.waterdelivery.controller;

import com.waterdelivery.common.ApiResponse;
import com.waterdelivery.common.RequirePermission;
import com.waterdelivery.dto.CategoryRequest;
import com.waterdelivery.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/categories")
public class AdminCategoryController {

    private final AdminService adminService;

    public AdminCategoryController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping
    @RequirePermission("category:view")
    public ApiResponse<?> list() {
        return ApiResponse.success(adminService.listCategories());
    }

    @PostMapping
    @RequirePermission("category:edit")
    public ApiResponse<?> save(@Valid @RequestBody CategoryRequest request) {
        adminService.saveCategory(request);
        return ApiResponse.success();
    }

    @PutMapping("/{id}")
    @RequirePermission("category:edit")
    public ApiResponse<?> update(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        adminService.updateCategory(id, request);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    @RequirePermission("category:edit")
    public ApiResponse<?> delete(@PathVariable Long id) {
        adminService.deleteCategory(id);
        return ApiResponse.success();
    }
}
