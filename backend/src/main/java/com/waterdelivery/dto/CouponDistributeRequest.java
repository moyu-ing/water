package com.waterdelivery.dto;

import lombok.Data;

import java.util.List;

@Data
public class CouponDistributeRequest {
    private List<Long> userIds;
}
