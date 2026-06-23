package com.waterdelivery.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 实体类测试 — 覆盖未测试的实体
 */
public class EntityTest {

    // ---- CustomerOrder ----
    @Test
    void testCustomerOrder() {
        CustomerOrder order = new CustomerOrder();
        order.setId(1L);
        order.setOrderNo("WD202606221200001");
        order.setUserId(1L);
        order.setTotalAmount(new BigDecimal("88.00"));
        order.setStatus("PENDING_CONFIRM");
        order.setPayType("COD");
        order.setContactName("张三");
        order.setContactPhone("13800138000");
        order.setFullAddress("广东省深圳市南山区科技园路1号");
        order.setRemark("请尽快配送");
        order.setBarrelDepositAmount(new BigDecimal("60.00"));
        order.setBarrelReturnCount(0);
        order.setBarrelReturnDeduct(BigDecimal.ZERO);
        order.setCouponId(null);
        order.setCouponDiscount(BigDecimal.ZERO);
        order.setPointsUsed(0);
        order.setPointsDiscount(BigDecimal.ZERO);
        order.setPointsEarned(0);
        LocalDateTime now = LocalDateTime.now();
        order.setCreateTime(now);
        order.setUpdateTime(now);

        assertEquals(1L, order.getId());
        assertEquals("WD202606221200001", order.getOrderNo());
        assertEquals("PENDING_CONFIRM", order.getStatus());
        assertEquals(new BigDecimal("88.00"), order.getTotalAmount());
        assertEquals("张三", order.getContactName());
        assertEquals(now, order.getCreateTime());
    }

    // ---- CartItem ----
    @Test
    void testCartItem() {
        CartItem item = new CartItem();
        item.setId(1L);
        item.setUserId(1L);
        item.setProductId(10L);
        item.setQuantity(3);
        item.setChecked(1);

        assertEquals(1L, item.getId());
        assertEquals(10L, item.getProductId());
        assertEquals(3, item.getQuantity());
        assertEquals(1, item.getChecked());
    }

    // ---- UserAddress ----
    @Test
    void testUserAddress() {
        UserAddress addr = new UserAddress();
        addr.setId(1L);
        addr.setUserId(1L);
        addr.setContactName("张三");
        addr.setContactPhone("13800138000");
        addr.setProvince("广东省");
        addr.setCity("深圳市");
        addr.setDistrict("南山区");
        addr.setDetailAddress("科技园路1号");
        addr.setIsDefault(1);

        assertEquals("张三", addr.getContactName());
        assertEquals("13800138000", addr.getContactPhone());
        assertEquals("广东省深圳市南山区科技园路1号",
                addr.getProvince() + addr.getCity() + addr.getDistrict() + addr.getDetailAddress());
        assertEquals(1, addr.getIsDefault());
    }

    // ---- CouponTemplate ----
    @Test
    void testCouponTemplate() {
        CouponTemplate template = new CouponTemplate();
        template.setId(1L);
        template.setName("满100减10");
        template.setType("FULL_REDUCTION");
        template.setDiscountValue(new BigDecimal("10.00"));
        template.setMinAmount(new BigDecimal("100.00"));
        template.setTotalQuantity(100);
        template.setReceivedQuantity(10);
        template.setValidDays(30);
        template.setStatus(1);

        assertEquals("满100减10", template.getName());
        assertEquals("FULL_REDUCTION", template.getType());
        assertEquals(new BigDecimal("10.00"), template.getDiscountValue());
        assertEquals(100, template.getTotalQuantity());
    }

    // ---- UserCoupon ----
    @Test
    void testUserCoupon() {
        UserCoupon uc = new UserCoupon();
        uc.setId(1L);
        uc.setUserId(1L);
        uc.setTemplateId(10L);
        uc.setStatus("UNUSED");
        LocalDateTime now = LocalDateTime.now();
        uc.setReceiveTime(now);
        uc.setExpireTime(now.plusDays(30));

        assertEquals("UNUSED", uc.getStatus());
        assertEquals(10L, uc.getTemplateId());
        assertEquals(now, uc.getReceiveTime());
    }

    // ---- DeliveryStaff ----
    @Test
    void testDeliveryStaff() {
        DeliveryStaff staff = new DeliveryStaff();
        staff.setId(1L);
        staff.setUsername("delivery01");
        staff.setPassword("encoded_password");
        staff.setName("张师傅");
        staff.setPhone("13800138000");
        staff.setStatus(1);

        assertEquals("delivery01", staff.getUsername());
        assertEquals("张师傅", staff.getName());
        assertEquals(1, staff.getStatus());
    }

    // ---- DeliveryTask ----
    @Test
    void testDeliveryTask() {
        DeliveryTask task = new DeliveryTask();
        task.setId(1L);
        task.setOrderId(100L);
        task.setStaffId(10L);
        task.setStatus("ASSIGNED");
        task.setPickupAddress("仓库A");
        task.setDeliveryAddress("客户地址B");
        LocalDateTime now = LocalDateTime.now();
        task.setEstimatedTime(now.plusHours(2));
        task.setCompleteTime(null);
        task.setBarrelReturned(0);

        assertEquals(100L, task.getOrderId());
        assertEquals("ASSIGNED", task.getStatus());
        assertEquals("仓库A", task.getPickupAddress());
        assertNull(task.getCompleteTime());
    }

    // ---- OrderItem ----
    @Test
    void testOrderItem() {
        OrderItem item = new OrderItem();
        item.setId(1L);
        item.setOrderId(1L);
        item.setProductId(10L);
        item.setProductName("农夫山泉");
        item.setProductImage("/uploads/pic.jpg");
        item.setProductSpec("18.9L/桶");
        item.setProductPrice(new BigDecimal("20.00"));
        item.setQuantity(2);
        item.setAmount(new BigDecimal("40.00"));

        assertEquals("农夫山泉", item.getProductName());
        assertEquals(new BigDecimal("20.00"), item.getProductPrice());
        assertEquals(2, item.getQuantity());
        assertEquals(new BigDecimal("40.00"), item.getAmount());
    }

    // ---- UserPoints ----
    @Test
    void testUserPoints() {
        UserPoints up = new UserPoints();
        up.setId(1L);
        up.setUserId(1L);
        up.setPoints(500);
        up.setSource("ORDER");
        up.setSourceDesc("订单完成赠送积分");

        assertEquals(500, up.getPoints());
        assertEquals("ORDER", up.getSource());
        assertEquals("订单完成赠送积分", up.getSourceDesc());
    }

    // ---- AdminUser ----
    @Test
    void testAdminUser() {
        AdminUser admin = new AdminUser();
        admin.setId(1L);
        admin.setUsername("admin");
        admin.setPassword("encoded_password");
        admin.setNickname("超级管理员");
        admin.setStatus(1);

        assertEquals("admin", admin.getUsername());
        assertEquals("超级管理员", admin.getNickname());
        assertEquals(1, admin.getStatus());
    }

    // ---- AdminRole ----
    @Test
    void testAdminRole() {
        AdminRole role = new AdminRole();
        role.setId(1L);
        role.setName("超级管理员");
        role.setCode("ADMIN");
        role.setDescription("系统最高权限");
        role.setStatus(1);

        assertEquals("超级管理员", role.getName());
        assertEquals("ADMIN", role.getCode());
    }

    // ---- AdminMenu ----
    @Test
    void testAdminMenu() {
        AdminMenu menu = new AdminMenu();
        menu.setId(1L);
        menu.setParentId(0L);
        menu.setName("系统管理");
        menu.setPath("/admin/system");
        menu.setComponent("SystemPage");
        menu.setPermissionCode("system:view");
        menu.setType(1);
        menu.setSortNum(1);
        menu.setStatus(1);

        assertEquals("系统管理", menu.getName());
        assertEquals("system:view", menu.getPermissionCode());
        assertEquals(1, menu.getType());
    }

    // ---- AdminUserRole ----
    @Test
    void testAdminUserRole() {
        AdminUserRole ur = new AdminUserRole();
        ur.setId(1L);
        ur.setAdminUserId(1L);
        ur.setRoleId(10L);

        assertEquals(1L, ur.getAdminUserId());
        assertEquals(10L, ur.getRoleId());
    }

    // ---- AdminRoleMenu ----
    @Test
    void testAdminRoleMenu() {
        AdminRoleMenu rm = new AdminRoleMenu();
        rm.setId(1L);
        rm.setRoleId(10L);
        rm.setMenuId(100L);

        assertEquals(10L, rm.getRoleId());
        assertEquals(100L, rm.getMenuId());
    }
}
