package com.waterdelivery.controller;

import com.waterdelivery.common.ApiResponse;
import com.waterdelivery.common.BizException;
import com.waterdelivery.common.CurrentContext;
import com.waterdelivery.dto.DeliveryTaskCompleteRequest;
import com.waterdelivery.dto.LoginRequest;
import com.waterdelivery.service.DeliveryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/delivery")
public class DeliveryStaffController {

    private final DeliveryService deliveryService;

    public DeliveryStaffController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@RequestBody LoginRequest request) {
        return ApiResponse.success(deliveryService.staffLogin(request));
    }

    @GetMapping("/me")
    public ApiResponse<Map<String, Object>> profile() {
        Long staffId = CurrentContext.getStaffId();
        if (staffId == null) {
            throw new BizException("未登录");
        }
        return ApiResponse.success(deliveryService.getStaffProfile(staffId));
    }

    @GetMapping("/tasks")
    public ApiResponse<List<Map<String, Object>>> myTasks() {
        Long staffId = CurrentContext.getStaffId();
        if (staffId == null) {
            throw new BizException("未登录");
        }
        return ApiResponse.success(deliveryService.listStaffTasks(staffId));
    }

    @PutMapping("/tasks/{id}/status")
    public ApiResponse<Void> updateTaskStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String status = body.get("status");
        deliveryService.updateTaskStatus(id, status);
        return ApiResponse.success(null);
    }

    @PutMapping("/tasks/{id}/complete")
    public ApiResponse<Void> completeTask(@PathVariable Long id, @RequestBody DeliveryTaskCompleteRequest request) {
        deliveryService.completeTask(id, request);
        return ApiResponse.success(null);
    }
}
