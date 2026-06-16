package com.waterdelivery.controller;

import com.waterdelivery.common.ApiResponse;
import com.waterdelivery.common.RequirePermission;
import com.waterdelivery.dto.AdminUserRequest;
import com.waterdelivery.service.AdminService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminUserController {

    private final AdminService adminService;

    public AdminUserController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    @RequirePermission("user:view")
    public ApiResponse<?> users() {
        return ApiResponse.success(adminService.listUsers());
    }

    @GetMapping("/admin-users")
    @RequirePermission("admin:view")
    public ApiResponse<?> adminUsers() {
        return ApiResponse.success(adminService.listAdminUsers());
    }

    @PostMapping("/admin-users")
    @RequirePermission("admin:edit")
    public ApiResponse<?> saveAdmin(@RequestBody AdminUserRequest request) {
        adminService.saveAdminUser(request);
        return ApiResponse.success();
    }

    @PutMapping("/admin-users/{id}")
    @RequirePermission("admin:edit")
    public ApiResponse<?> updateAdmin(@PathVariable Long id, @RequestBody AdminUserRequest request) {
        adminService.updateAdminUser(id, request);
        return ApiResponse.success();
    }

    @DeleteMapping("/admin-users/{id}")
    @RequirePermission("admin:edit")
    public ApiResponse<?> deleteAdmin(@PathVariable Long id) {
        adminService.deleteAdminUser(id);
        return ApiResponse.success();
    }
}
