package com.waterdelivery.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("delivery_task")
public class DeliveryTask {
    private Long id;
    private Long orderId;
    private Long staffId;
    private String status;
    private String pickupAddress;
    private String deliveryAddress;
    private LocalDateTime estimatedTime;
    private LocalDateTime completeTime;
    private String photoUrl;
    private String remark;
    private Integer barrelReturned;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
