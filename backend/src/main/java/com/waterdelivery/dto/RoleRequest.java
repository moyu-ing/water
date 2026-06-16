package com.waterdelivery.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RoleRequest {
    private String name;
    private String code;
    private String description;
    private Integer status = 1;
    private List<Long> menuIds = new ArrayList<>();
}
