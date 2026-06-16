package com.waterdelivery.controller;

import com.waterdelivery.common.ApiResponse;
import com.waterdelivery.common.RequirePermission;
import com.waterdelivery.dto.MenuRequest;
import com.waterdelivery.service.AdminService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/menus")
public class AdminMenuController {

    private final AdminService adminService;

    public AdminMenuController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping
    @RequirePermission("menu:view")
    public ApiResponse<?> list() {
        return ApiResponse.success(adminService.listMenus());
    }

    @PostMapping
    @RequirePermission("menu:edit")
    public ApiResponse<?> save(@RequestBody MenuRequest request) {
        adminService.saveMenu(request);
        return ApiResponse.success();
    }

    @PutMapping("/{id}")
    @RequirePermission("menu:edit")
    public ApiResponse<?> update(@PathVariable Long id, @RequestBody MenuRequest request) {
        adminService.updateMenu(id, request);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    @RequirePermission("menu:edit")
    public ApiResponse<?> delete(@PathVariable Long id) {
        adminService.deleteMenu(id);
        return ApiResponse.success();
    }
}
