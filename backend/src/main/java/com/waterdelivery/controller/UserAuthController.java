package com.waterdelivery.controller;

import com.waterdelivery.common.ApiResponse;
import com.waterdelivery.common.CurrentContext;
import com.waterdelivery.dto.LoginRequest;
import com.waterdelivery.dto.UserRegisterRequest;
import com.waterdelivery.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user/auth")
public class UserAuthController {

    private final AuthService authService;

    public UserAuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ApiResponse<?> register(@Valid @RequestBody UserRegisterRequest request) {
        authService.register(request);
        return ApiResponse.success();
    }

    @PostMapping("/login")
    public ApiResponse<?> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.userLogin(request));
    }

    @GetMapping("/me")
    public ApiResponse<?> me() {
        return ApiResponse.success(authService.getCurrentUserProfile());
    }
}
