package com.waterdelivery.controller;

import com.waterdelivery.common.ApiResponse;
import com.waterdelivery.common.CurrentContext;
import com.waterdelivery.entity.UserPoints;
import com.waterdelivery.service.PointsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user/points")
public class UserPointsController {

    private final PointsService pointsService;

    public UserPointsController(PointsService pointsService) {
        this.pointsService = pointsService;
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> balance() {
        Long userId = CurrentContext.getUserId();
        Map<String, Object> result = new HashMap<>();
        result.put("balance", pointsService.getBalance(userId));
        result.put("exchangeRate", 100); // 100积分 = 1元
        return ApiResponse.success(result);
    }

    @GetMapping("/history")
    public ApiResponse<List<UserPoints>> history() {
        return ApiResponse.success(pointsService.getHistory(CurrentContext.getUserId()));
    }
}
