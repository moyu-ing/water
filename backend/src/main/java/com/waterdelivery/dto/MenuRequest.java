package com.waterdelivery.dto;

import lombok.Data;

@Data
public class MenuRequest {
    private Long parentId = 0L;
    private String name;
    private String path;
    private String component;
    private String permissionCode;
    private Integer type = 1;
    private Integer sortNum = 0;
    private Integer status = 1;
}
