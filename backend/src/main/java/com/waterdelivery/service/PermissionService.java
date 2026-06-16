package com.waterdelivery.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.waterdelivery.entity.AdminMenu;
import com.waterdelivery.entity.AdminRoleMenu;
import com.waterdelivery.entity.AdminUserRole;
import com.waterdelivery.mapper.AdminMenuMapper;
import com.waterdelivery.mapper.AdminRoleMenuMapper;
import com.waterdelivery.mapper.AdminUserRoleMapper;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PermissionService {

    private final AdminUserRoleMapper adminUserRoleMapper;
    private final AdminRoleMenuMapper adminRoleMenuMapper;
    private final AdminMenuMapper adminMenuMapper;

    public PermissionService(AdminUserRoleMapper adminUserRoleMapper,
                             AdminRoleMenuMapper adminRoleMenuMapper,
                             AdminMenuMapper adminMenuMapper) {
        this.adminUserRoleMapper = adminUserRoleMapper;
        this.adminRoleMenuMapper = adminRoleMenuMapper;
        this.adminMenuMapper = adminMenuMapper;
    }

    public List<Long> getRoleIdsByAdminId(Long adminId) {
        return adminUserRoleMapper.selectList(new LambdaQueryWrapper<AdminUserRole>()
                        .eq(AdminUserRole::getAdminUserId, adminId))
                .stream()
                .map(AdminUserRole::getRoleId)
                .toList();
    }

    public Set<String> getPermissionsByAdminId(Long adminId) {
        List<Long> roleIds = getRoleIdsByAdminId(adminId);
        if (roleIds.isEmpty()) {
            return Collections.emptySet();
        }
        List<Long> menuIds = adminRoleMenuMapper.selectList(new LambdaQueryWrapper<AdminRoleMenu>()
                        .in(AdminRoleMenu::getRoleId, roleIds))
                .stream()
                .map(AdminRoleMenu::getMenuId)
                .distinct()
                .toList();
        if (menuIds.isEmpty()) {
            return Collections.emptySet();
        }
        return adminMenuMapper.selectBatchIds(menuIds).stream()
                .map(AdminMenu::getPermissionCode)
                .filter(Objects::nonNull)
                .filter(code -> !code.isBlank())
                .collect(Collectors.toSet());
    }
}
