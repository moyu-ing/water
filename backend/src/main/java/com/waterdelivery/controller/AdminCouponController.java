package com.waterdelivery.controller;

import com.waterdelivery.common.ApiResponse;
import com.waterdelivery.common.RequirePermission;
import com.waterdelivery.dto.CouponDistributeRequest;
import com.waterdelivery.dto.CouponTemplateRequest;
import com.waterdelivery.service.CouponService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/coupons")
public class AdminCouponController {

    private final CouponService couponService;

    public AdminCouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @RequirePermission("coupon:view")
    @GetMapping
    public ApiResponse<List<?>> listTemplates() {
        return ApiResponse.success(couponService.listTemplates());
    }

    @RequirePermission("coupon:edit")
    @PostMapping
    public ApiResponse<Void> saveTemplate(@RequestBody CouponTemplateRequest request) {
        couponService.saveTemplate(request);
        return ApiResponse.success(null);
    }

    @RequirePermission("coupon:edit")
    @PutMapping("/{id}")
    public ApiResponse<Void> updateTemplate(@PathVariable Long id, @RequestBody CouponTemplateRequest request) {
        couponService.updateTemplate(id, request);
        return ApiResponse.success(null);
    }

    @RequirePermission("coupon:edit")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteTemplate(@PathVariable Long id) {
        couponService.deleteTemplate(id);
        return ApiResponse.success(null);
    }

    @RequirePermission("coupon:edit")
    @PostMapping("/{id}/distribute")
    public ApiResponse<Void> distribute(@PathVariable Long id, @RequestBody CouponDistributeRequest request) {
        couponService.distribute(id, request);
        return ApiResponse.success(null);
    }
}
