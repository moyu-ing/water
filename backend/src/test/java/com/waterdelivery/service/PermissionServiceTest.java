package com.waterdelivery.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.waterdelivery.entity.AdminMenu;
import com.waterdelivery.entity.AdminRoleMenu;
import com.waterdelivery.entity.AdminUserRole;
import com.waterdelivery.mapper.AdminMenuMapper;
import com.waterdelivery.mapper.AdminRoleMenuMapper;
import com.waterdelivery.mapper.AdminUserRoleMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * RBAC权限模块测试 — 覆盖 TC-RBAC
 */
@ExtendWith(MockitoExtension.class)
public class PermissionServiceTest {

    @Mock private AdminUserRoleMapper adminUserRoleMapper;
    @Mock private AdminRoleMenuMapper adminRoleMenuMapper;
    @Mock private AdminMenuMapper adminMenuMapper;

    @InjectMocks
    private PermissionService permissionService;

    // ==================== TC-RBAC-01: 获取管理员角色ID列表 ====================
    @Test
    void testGetRoleIdsByAdminId() {
        AdminUserRole ur1 = new AdminUserRole();
        ur1.setRoleId(1L);
        AdminUserRole ur2 = new AdminUserRole();
        ur2.setRoleId(2L);

        when(adminUserRoleMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(ur1, ur2));

        List<Long> roleIds = permissionService.getRoleIdsByAdminId(1L);
        assertEquals(2, roleIds.size());
        assertTrue(roleIds.contains(1L));
        assertTrue(roleIds.contains(2L));
    }

    // ==================== TC-RBAC-02: 管理员无角色时返回空 ====================
    @Test
    void testGetRoleIdsByAdminIdEmpty() {
        when(adminUserRoleMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        List<Long> roleIds = permissionService.getRoleIdsByAdminId(1L);
        assertTrue(roleIds.isEmpty());
    }

    // ==================== TC-RBAC-03: 获取管理员权限码集合 ====================
    @Test
    void testGetPermissionsByAdminId() {
        AdminUserRole ur1 = new AdminUserRole();
        ur1.setRoleId(1L);

        AdminRoleMenu rm1 = new AdminRoleMenu();
        rm1.setMenuId(10L);
        AdminRoleMenu rm2 = new AdminRoleMenu();
        rm2.setMenuId(20L);

        AdminMenu menu1 = new AdminMenu();
        menu1.setId(10L);
        menu1.setPermissionCode("admin:dashboard");

        AdminMenu menu2 = new AdminMenu();
        menu2.setId(20L);
        menu2.setPermissionCode("product:edit");

        when(adminUserRoleMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(ur1));
        when(adminRoleMenuMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(rm1, rm2));
        when(adminMenuMapper.selectBatchIds(List.of(10L, 20L))).thenReturn(List.of(menu1, menu2));

        Set<String> permissions = permissionService.getPermissionsByAdminId(1L);
        assertEquals(2, permissions.size());
        assertTrue(permissions.contains("admin:dashboard"));
        assertTrue(permissions.contains("product:edit"));
    }

    // ==================== TC-RBAC-04: 无角色时权限为空集合 ====================
    @Test
    void testGetPermissionsByAdminIdNoRoles() {
        when(adminUserRoleMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        Set<String> permissions = permissionService.getPermissionsByAdminId(1L);
        assertTrue(permissions.isEmpty());
    }

    // ==================== TC-RBAC-05: 角色无菜单时权限为空 ====================
    @Test
    void testGetPermissionsByAdminIdNoMenus() {
        AdminUserRole ur1 = new AdminUserRole();
        ur1.setRoleId(1L);

        when(adminUserRoleMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(ur1));
        when(adminRoleMenuMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        Set<String> permissions = permissionService.getPermissionsByAdminId(1L);
        assertTrue(permissions.isEmpty());
    }

    // ==================== TC-RBAC-06: 过滤空权限码 ====================
    @Test
    void testGetPermissionsFiltersBlankCodes() {
        AdminUserRole ur1 = new AdminUserRole();
        ur1.setRoleId(1L);

        AdminRoleMenu rm1 = new AdminRoleMenu();
        rm1.setMenuId(10L);

        AdminMenu menu1 = new AdminMenu();
        menu1.setId(10L);
        menu1.setPermissionCode(null); // 空权限码

        AdminMenu menu2 = new AdminMenu();
        menu2.setId(20L);
        menu2.setPermissionCode(""); // 空白权限码

        AdminMenu menu3 = new AdminMenu();
        menu3.setId(30L);
        menu3.setPermissionCode("valid:perm"); // 有效

        when(adminUserRoleMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(ur1));
        when(adminRoleMenuMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(rm1));
        when(adminMenuMapper.selectBatchIds(anyList())).thenReturn(List.of(menu1, menu2, menu3));

        Set<String> permissions = permissionService.getPermissionsByAdminId(1L);
        assertEquals(1, permissions.size());
        assertTrue(permissions.contains("valid:perm"));
    }
}
