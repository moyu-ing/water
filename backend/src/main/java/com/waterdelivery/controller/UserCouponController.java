package com.waterdelivery.controller;

import com.waterdelivery.common.ApiResponse;
import com.waterdelivery.common.CurrentContext;
import com.waterdelivery.service.CouponService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user/coupons")
public class UserCouponController {

    private final CouponService couponService;

    public UserCouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @GetMapping
    public ApiResponse<List<Map<String, Object>>> myCoupons() {
        return ApiResponse.success(couponService.listUserCoupons(CurrentContext.getUserId()));
    }

    @GetMapping("/available")
    public ApiResponse<List<Map<String, Object>>> availableCoupons() {
        return ApiResponse.success(couponService.listAvailableCoupons(CurrentContext.getUserId()));
    }

    @PostMapping("/receive/{templateId}")
    public ApiResponse<Void> receive(@PathVariable Long templateId) {
        couponService.receiveCoupon(CurrentContext.getUserId(), templateId);
        return ApiResponse.success(null);
    }
}
