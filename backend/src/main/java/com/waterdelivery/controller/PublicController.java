package com.waterdelivery.controller;

import com.waterdelivery.common.ApiResponse;
import com.waterdelivery.service.UserMallService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public")
public class PublicController {

    private final UserMallService userMallService;

    public PublicController(UserMallService userMallService) {
        this.userMallService = userMallService;
    }

    @GetMapping("/categories")
    public ApiResponse<?> categories() {
        return ApiResponse.success(userMallService.listActiveCategories());
    }

    @GetMapping("/products")
    public ApiResponse<?> products(@RequestParam(required = false) Long categoryId,
                                   @RequestParam(required = false) String keyword) {
        return ApiResponse.success(userMallService.listProducts(categoryId, keyword));
    }

    @GetMapping("/products/{id}")
    public ApiResponse<?> productDetail(@PathVariable Long id) {
        return ApiResponse.success(userMallService.getProductDetail(id));
    }
}
