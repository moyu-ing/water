package com.waterdelivery.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserAddressRequest {
    @NotBlank
    private String contactName;
    @NotBlank
    private String contactPhone;
    @NotBlank
    private String province;
    @NotBlank
    private String city;
    @NotBlank
    private String district;
    @NotBlank
    private String detailAddress;
    private Integer isDefault = 0;
}
