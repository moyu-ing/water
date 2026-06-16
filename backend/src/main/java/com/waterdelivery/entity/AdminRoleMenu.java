package com.waterdelivery.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("admin_role_menu")
public class AdminRoleMenu {
    private Long id;
    private Long roleId;
    private Long menuId;
}
