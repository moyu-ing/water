package com.waterdelivery.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRequest {
    @NotNull
    private Long categoryId;
    @NotBlank
    private String name;
    private String subTitle;
    private String image;
    private String description;
    private String spec;
    @NotNull
    private BigDecimal price;
    @NotNull
    private Integer stock;
    private Integer status = 1;
}
