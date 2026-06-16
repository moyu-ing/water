package com.waterdelivery.controller;

import com.waterdelivery.common.ApiResponse;
import com.waterdelivery.common.RequirePermission;
import com.waterdelivery.dto.ProductRequest;
import com.waterdelivery.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/products")
public class AdminProductController {

    private final AdminService adminService;

    public AdminProductController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping
    @RequirePermission("product:view")
    public ApiResponse<?> list() {
        return ApiResponse.success(adminService.listProducts());
    }

    @PostMapping
    @RequirePermission("product:edit")
    public ApiResponse<?> save(@Valid @RequestBody ProductRequest request) {
        adminService.saveProduct(request);
        return ApiResponse.success();
    }

    @PutMapping("/{id}")
    @RequirePermission("product:edit")
    public ApiResponse<?> update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        adminService.updateProduct(id, request);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    @RequirePermission("product:edit")
    public ApiResponse<?> delete(@PathVariable Long id) {
        adminService.deleteProduct(id);
        return ApiResponse.success();
    }
}
