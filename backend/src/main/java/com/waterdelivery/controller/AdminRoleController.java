package com.waterdelivery.controller;

import com.waterdelivery.common.ApiResponse;
import com.waterdelivery.common.RequirePermission;
import com.waterdelivery.dto.RoleRequest;
import com.waterdelivery.service.AdminService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/roles")
public class AdminRoleController {

    private final AdminService adminService;

    public AdminRoleController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping
    @RequirePermission("role:view")
    public ApiResponse<?> list() {
        return ApiResponse.success(adminService.listRoles());
    }

    @PostMapping
    @RequirePermission("role:edit")
    public ApiResponse<?> save(@RequestBody RoleRequest request) {
        adminService.saveRole(request);
        return ApiResponse.success();
    }

    @PutMapping("/{id}")
    @RequirePermission("role:edit")
    public ApiResponse<?> update(@PathVariable Long id, @RequestBody RoleRequest request) {
        adminService.updateRole(id, request);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    @RequirePermission("role:edit")
    public ApiResponse<?> delete(@PathVariable Long id) {
        adminService.deleteRole(id);
        return ApiResponse.success();
    }
}
