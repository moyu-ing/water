package com.waterdelivery.controller;

import com.waterdelivery.common.ApiResponse;
import com.waterdelivery.common.CurrentContext;
import com.waterdelivery.dto.OrderCreateRequest;
import com.waterdelivery.service.UserMallService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/orders")
public class UserOrderController {

    private final UserMallService userMallService;

    public UserOrderController(UserMallService userMallService) {
        this.userMallService = userMallService;
    }

    @PostMapping
    public ApiResponse<?> create(@Valid @RequestBody OrderCreateRequest request) {
        return ApiResponse.success(userMallService.createOrder(CurrentContext.getUserId(), request));
    }

    @GetMapping
    public ApiResponse<?> list() {
        return ApiResponse.success(userMallService.listOrders(CurrentContext.getUserId()));
    }

    @GetMapping("/{id}")
    public ApiResponse<?> detail(@PathVariable Long id) {
        return ApiResponse.success(userMallService.getOrderDetail(CurrentContext.getUserId(), id));
    }
}
