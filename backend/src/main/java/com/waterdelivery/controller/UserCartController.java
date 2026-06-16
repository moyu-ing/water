package com.waterdelivery.controller;

import com.waterdelivery.common.ApiResponse;
import com.waterdelivery.common.CurrentContext;
import com.waterdelivery.dto.CartRequest;
import com.waterdelivery.service.UserMallService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/cart")
public class UserCartController {

    private final UserMallService userMallService;

    public UserCartController(UserMallService userMallService) {
        this.userMallService = userMallService;
    }

    @GetMapping
    public ApiResponse<?> list() {
        return ApiResponse.success(userMallService.listCart(CurrentContext.getUserId()));
    }

    @PostMapping
    public ApiResponse<?> add(@Valid @RequestBody CartRequest request) {
        userMallService.addCart(CurrentContext.getUserId(), request);
        return ApiResponse.success();
    }

    @PutMapping("/{id}")
    public ApiResponse<?> update(@PathVariable Long id, @Valid @RequestBody CartRequest request) {
        userMallService.updateCart(CurrentContext.getUserId(), id, request);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> delete(@PathVariable Long id) {
        userMallService.deleteCart(CurrentContext.getUserId(), id);
        return ApiResponse.success();
    }
}
