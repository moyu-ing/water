package com.waterdelivery.controller;

import com.waterdelivery.common.ApiResponse;
import com.waterdelivery.common.RequirePermission;
import com.waterdelivery.dto.OrderStatusRequest;
import com.waterdelivery.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

    private final AdminService adminService;

    public AdminOrderController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping
    @RequirePermission("order:view")
    public ApiResponse<?> list() {
        return ApiResponse.success(adminService.listOrders());
    }

    @PutMapping("/{id}/status")
    @RequirePermission("order:edit")
    public ApiResponse<?> updateStatus(@PathVariable Long id, @Valid @RequestBody OrderStatusRequest request) {
        adminService.updateOrderStatus(id, request.getStatus());
        return ApiResponse.success();
    }
}
