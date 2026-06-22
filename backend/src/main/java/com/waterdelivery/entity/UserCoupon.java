package com.waterdelivery.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_coupon")
public class UserCoupon {
    private Long id;
    private Long userId;
    private Long templateId;
    private String status;
    private LocalDateTime receiveTime;
    private LocalDateTime useTime;
    private LocalDateTime expireTime;
    private Long orderId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
