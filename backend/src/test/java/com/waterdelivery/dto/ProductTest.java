package com.waterdelivery.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class ProductTest {

    @Test
    void testGetterSetter() {
        Product product = new Product();

        product.setId(1L);
        product.setName("怡宝");
        product.setPrice(new BigDecimal("15"));

        assertEquals(1L, product.getId());
        assertEquals("怡宝", product.getName());
        assertEquals(new BigDecimal("15"), product.getPrice());
    }
}