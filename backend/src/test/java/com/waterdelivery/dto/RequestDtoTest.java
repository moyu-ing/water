package com.waterdelivery.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 请求DTO测试 — 覆盖未测试的DTO
 */
public class RequestDtoTest {

    // ---- OrderCreateRequest ----
    @Test
    void testOrderCreateRequest() {
        OrderCreateRequest request = new OrderCreateRequest();
        request.setAddressId(1L);
        request.setRemark("请尽快配送");
        request.setUserCouponId(100L);
        request.setUsePoints(200);

        assertEquals(1L, request.getAddressId());
        assertEquals("请尽快配送", request.getRemark());
        assertEquals(100L, request.getUserCouponId());
        assertEquals(200, request.getUsePoints());
    }

    // ---- CartRequest ----
    @Test
    void testCartRequest() {
        CartRequest request = new CartRequest();
        request.setProductId(1L);
        request.setQuantity(3);
        request.setChecked(1);

        assertEquals(1L, request.getProductId());
        assertEquals(3, request.getQuantity());
        assertEquals(1, request.getChecked());
    }

    // ---- CartRequest default checked ----
    @Test
    void testCartRequestCheckedDefault() {
        CartRequest request = new CartRequest();
        assertEquals(1, request.getChecked()); // 默认值为1
    }

    // ---- UserRegisterRequest ----
    @Test
    void testUserRegisterRequest() {
        UserRegisterRequest request = new UserRegisterRequest();
        request.setUsername("newuser");
        request.setPassword("pass123");
        request.setNickname("新用户");
        request.setPhone("13800138000");

        assertEquals("newuser", request.getUsername());
        assertEquals("pass123", request.getPassword());
        assertEquals("新用户", request.getNickname());
        assertEquals("13800138000", request.getPhone());
    }

    // ---- UserAddressRequest ----
    @Test
    void testUserAddressRequest() {
        UserAddressRequest request = new UserAddressRequest();
        request.setContactName("张三");
        request.setContactPhone("13800138000");
        request.setProvince("广东省");
        request.setCity("深圳市");
        request.setDistrict("南山区");
        request.setDetailAddress("科技园路1号");
        request.setIsDefault(1);

        assertEquals("张三", request.getContactName());
        assertEquals("广东省", request.getProvince());
        assertEquals("深圳市", request.getCity());
        assertEquals(1, request.getIsDefault());
    }

    // ---- CouponTemplateRequest ----
    @Test
    void testCouponTemplateRequest() {
        CouponTemplateRequest request = new CouponTemplateRequest();
        request.setName("满100减10");
        request.setType("FULL_REDUCTION");
        request.setDiscountValue(new BigDecimal("10.00"));
        request.setMinAmount(new BigDecimal("100.00"));
        request.setTotalQuantity(100);
        request.setValidDays(30);
        request.setStatus(1);

        assertEquals("满100减10", request.getName());
        assertEquals("FULL_REDUCTION", request.getType());
        assertEquals(new BigDecimal("10.00"), request.getDiscountValue());
        assertEquals(100, request.getTotalQuantity());
        assertEquals(30, request.getValidDays());
    }

    // ---- CouponDistributeRequest ----
    @Test
    void testCouponDistributeRequest() {
        CouponDistributeRequest request = new CouponDistributeRequest();
        request.setUserIds(List.of(1L, 2L, 3L));

        assertEquals(3, request.getUserIds().size());
        assertTrue(request.getUserIds().contains(1L));
    }

    // ---- DeliveryStaffRequest ----
    @Test
    void testDeliveryStaffRequest() {
        DeliveryStaffRequest request = new DeliveryStaffRequest();
        request.setUsername("delivery01");
        request.setPassword("123456");
        request.setName("张师傅");
        request.setPhone("13800138000");
        request.setStatus(1);

        assertEquals("delivery01", request.getUsername());
        assertEquals("张师傅", request.getName());
        assertEquals(1, request.getStatus());
    }

    // ---- DeliveryTaskRequest ----
    @Test
    void testDeliveryTaskRequest() {
        DeliveryTaskRequest request = new DeliveryTaskRequest();
        request.setOrderId(1L);
        request.setStaffId(10L);
        request.setPickupAddress("仓库A");
        request.setDeliveryAddress("客户地址B");
        request.setEstimatedTime("2026-06-23T10:00:00");

        assertEquals(1L, request.getOrderId());
        assertEquals(10L, request.getStaffId());
        assertEquals("仓库A", request.getPickupAddress());
    }

    // ---- DeliveryTaskCompleteRequest ----
    @Test
    void testDeliveryTaskCompleteRequest() {
        DeliveryTaskCompleteRequest request = new DeliveryTaskCompleteRequest();
        request.setPhotoUrl("/uploads/sign.jpg");
        request.setRemark("已签收");
        request.setBarrelReturned(3);

        assertEquals("/uploads/sign.jpg", request.getPhotoUrl());
        assertEquals("已签收", request.getRemark());
        assertEquals(3, request.getBarrelReturned());
    }

    // ---- MenuRequest ----
    @Test
    void testMenuRequest() {
        MenuRequest request = new MenuRequest();
        request.setParentId(0L);
        request.setName("商品管理");
        request.setPath("/admin/products");
        request.setComponent("ProductsPage");
        request.setPermissionCode("product:view");
        request.setType(1);
        request.setSortNum(1);
        request.setStatus(1);

        assertEquals("商品管理", request.getName());
        assertEquals("product:view", request.getPermissionCode());
        assertEquals(1, request.getType());
        assertEquals(1, request.getSortNum());
    }

    // ---- OrderStatusRequest ----
    @Test
    void testOrderStatusRequest() {
        OrderStatusRequest request = new OrderStatusRequest();
        request.setStatus("TO_DELIVER");

        assertEquals("TO_DELIVER", request.getStatus());
    }
}
