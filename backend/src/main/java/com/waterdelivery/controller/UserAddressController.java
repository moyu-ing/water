package com.waterdelivery.controller;

import com.waterdelivery.common.ApiResponse;
import com.waterdelivery.common.CurrentContext;
import com.waterdelivery.dto.UserAddressRequest;
import com.waterdelivery.service.UserMallService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/addresses")
public class UserAddressController {

    private final UserMallService userMallService;

    public UserAddressController(UserMallService userMallService) {
        this.userMallService = userMallService;
    }

    @GetMapping
    public ApiResponse<?> list() {
        return ApiResponse.success(userMallService.listAddresses(CurrentContext.getUserId()));
    }

    @PostMapping
    public ApiResponse<?> save(@Valid @RequestBody UserAddressRequest request) {
        userMallService.addAddress(CurrentContext.getUserId(), request);
        return ApiResponse.success();
    }

    @PutMapping("/{id}")
    public ApiResponse<?> update(@PathVariable Long id, @Valid @RequestBody UserAddressRequest request) {
        userMallService.updateAddress(CurrentContext.getUserId(), id, request);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> delete(@PathVariable Long id) {
        userMallService.deleteAddress(CurrentContext.getUserId(), id);
        return ApiResponse.success();
    }
}
