package com.waterdelivery.dto;

import lombok.Data;

@Data
public class DeliveryTaskCompleteRequest {
    private String photoUrl;
    private String remark;
    private Integer barrelReturned;
}
