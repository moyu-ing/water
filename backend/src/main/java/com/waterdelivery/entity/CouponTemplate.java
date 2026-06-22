package com.waterdelivery.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("coupon_template")
public class CouponTemplate {
    private Long id;
    private String name;
    private String type;
    private BigDecimal discountValue;
    private BigDecimal minAmount;
    private Integer totalQuantity;
    private Integer receivedQuantity;
    private Integer validDays;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
