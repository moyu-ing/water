package com.waterdelivery.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("admin_menu")
public class AdminMenu {
    private Long id;
    private Long parentId;
    private String name;
    private String path;
    private String component;
    private String permissionCode;
    private Integer type;
    private Integer sortNum;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
