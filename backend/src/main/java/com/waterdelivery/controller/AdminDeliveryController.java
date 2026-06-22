package com.waterdelivery.controller;

import com.waterdelivery.common.ApiResponse;
import com.waterdelivery.common.RequirePermission;
import com.waterdelivery.dto.DeliveryStaffRequest;
import com.waterdelivery.dto.DeliveryTaskRequest;
import com.waterdelivery.service.DeliveryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/delivery")
public class AdminDeliveryController {

    private final DeliveryService deliveryService;

    public AdminDeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    // ========== 配送员管理 ==========
    @RequirePermission("delivery:view")
    @GetMapping("/staff")
    public ApiResponse<List<?>> listStaff() {
        return ApiResponse.success(deliveryService.listStaff());
    }

    @RequirePermission("delivery:edit")
    @PostMapping("/staff")
    public ApiResponse<Void> saveStaff(@RequestBody DeliveryStaffRequest request) {
        deliveryService.saveStaff(request);
        return ApiResponse.success(null);
    }

    @RequirePermission("delivery:edit")
    @PutMapping("/staff/{id}")
    public ApiResponse<Void> updateStaff(@PathVariable Long id, @RequestBody DeliveryStaffRequest request) {
        deliveryService.updateStaff(id, request);
        return ApiResponse.success(null);
    }

    @RequirePermission("delivery:edit")
    @DeleteMapping("/staff/{id}")
    public ApiResponse<Void> deleteStaff(@PathVariable Long id) {
        deliveryService.deleteStaff(id);
        return ApiResponse.success(null);
    }

    // ========== 任务管理 ==========
    @RequirePermission("delivery:view")
    @GetMapping("/tasks")
    public ApiResponse<List<Map<String, Object>>> listTasks() {
        return ApiResponse.success(deliveryService.listAllTasks());
    }

    @RequirePermission("delivery:edit")
    @PostMapping("/tasks/assign")
    public ApiResponse<Void> assignTask(@RequestBody DeliveryTaskRequest request) {
        deliveryService.assignTask(request);
        return ApiResponse.success(null);
    }
}
