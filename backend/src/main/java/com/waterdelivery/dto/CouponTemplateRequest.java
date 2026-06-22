package com.waterdelivery.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CouponTemplateRequest {
    private String name;
    private String type;
    private BigDecimal discountValue;
    private BigDecimal minAmount;
    private Integer totalQuantity;
    private Integer validDays;
    private Integer status;
}
