package com.waterdelivery.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LoginRequestTest {

    @Test
    void testGetterSetter() {
        LoginRequest request = new LoginRequest();

        request.setUsername("admin");
        request.setPassword("123456");

        assertEquals("admin", request.getUsername());
        assertEquals("123456", request.getPassword());
    }
}