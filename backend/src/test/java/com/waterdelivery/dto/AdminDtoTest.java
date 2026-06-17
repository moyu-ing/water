package com.waterdelivery.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AdminDtoTest {

    @Test
    void testAdminUserRequest() {

        AdminUserRequest request = new AdminUserRequest();

        request.setUsername("admin");
        request.setPassword("123456");
        request.setNickname("管理员");

        assertEquals("admin", request.getUsername());
        assertEquals("123456", request.getPassword());
        assertEquals("管理员", request.getNickname());

        assertNotNull(request.getRoleIds());
        assertEquals(0, request.getRoleIds().size());

        assertEquals(1, request.getStatus());
    }

    @Test
    void testRoleRequest() {

        RoleRequest request = new RoleRequest();

        request.setName("超级管理员");
        request.setCode("ADMIN");
        request.setDescription("系统最高权限");

        assertEquals("超级管理员", request.getName());
        assertEquals("ADMIN", request.getCode());
        assertEquals("系统最高权限", request.getDescription());

        assertNotNull(request.getMenuIds());
        assertEquals(0, request.getMenuIds().size());

        assertEquals(1, request.getStatus());
    }
}