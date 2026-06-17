package com.waterdelivery.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class ProductRequestTest {

    @Test
    void testGetterSetter() {
        ProductRequest request = new ProductRequest();

        request.setCategoryId(1L);
        request.setName("农夫山泉");
        request.setPrice(new BigDecimal("10.50"));
        request.setStock(100);

        assertEquals(1L, request.getCategoryId());
        assertEquals("农夫山泉", request.getName());
        assertEquals(new BigDecimal("10.50"), request.getPrice());
        assertEquals(100, request.getStock());
    }
}