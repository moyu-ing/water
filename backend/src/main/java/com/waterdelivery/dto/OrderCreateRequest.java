package com.waterdelivery.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderCreateRequest {
    @NotNull
    private Long addressId;
    private String remark;
}
