package com.waterdelivery.dto;

import lombok.Data;

@Data
public class DeliveryStaffRequest {
    private String username;
    private String password;
    private String name;
    private String phone;
    private Integer status;
}
