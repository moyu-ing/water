package com.waterdelivery.dto;

import lombok.Data;

@Data
public class DeliveryTaskRequest {
    private Long orderId;
    private Long staffId;
    private String pickupAddress;
    private String deliveryAddress;
    private String estimatedTime;
}
