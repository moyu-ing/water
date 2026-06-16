package com.waterdelivery.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryRequest {
    private Long parentId = 0L;
    @NotBlank
    private String name;
    private String image;
    private Integer sortNum = 0;
    private Integer status = 1;
}
