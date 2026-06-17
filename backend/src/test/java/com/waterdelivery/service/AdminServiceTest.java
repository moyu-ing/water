package com.waterdelivery.service;

import com.waterdelivery.dto.AdminUserRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AdminServiceTest {

    @Test
    void testUsernameValidation() {

        AdminUserRequest request = new AdminUserRequest();

        request.setUsername("");
        request.setPassword("123456");

        assertTrue(request.getUsername().isBlank());
    }

    @Test
    void testPasswordValidation() {

        AdminUserRequest request = new AdminUserRequest();

        request.setUsername("admin");
        request.setPassword("");

        assertTrue(request.getPassword().isBlank());
    }

    @Test
    void testNormalRequest() {

        AdminUserRequest request = new AdminUserRequest();

        request.setUsername("admin");
        request.setPassword("123456");

        assertFalse(request.getUsername().isBlank());
        assertFalse(request.getPassword().isBlank());
    }
}