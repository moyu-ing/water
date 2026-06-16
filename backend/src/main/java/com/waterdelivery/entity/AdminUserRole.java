package com.waterdelivery.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("admin_user_role")
public class AdminUserRole {
    private Long id;
    private Long adminUserId;
    private Long roleId;
}
