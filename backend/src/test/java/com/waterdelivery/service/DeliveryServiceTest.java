package com.waterdelivery.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.waterdelivery.common.BizException;
import com.waterdelivery.config.JwtUtil;
import com.waterdelivery.config.PasswordUtil;
import com.waterdelivery.dto.*;
import com.waterdelivery.entity.*;
import com.waterdelivery.mapper.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 配送管理模块测试 — 覆盖 TC-DELIV
 */
@ExtendWith(MockitoExtension.class)
public class DeliveryServiceTest {

    @Mock private DeliveryStaffMapper deliveryStaffMapper;
    @Mock private DeliveryTaskMapper deliveryTaskMapper;
    @Mock private CustomerOrderMapper customerOrderMapper;
    @Mock private OrderItemMapper orderItemMapper;
    @Mock private AppUserMapper appUserMapper;
    @Mock private PasswordUtil passwordUtil;
    @Mock private JwtUtil jwtUtil;
    @Mock private PointsService pointsService;

    @InjectMocks
    private DeliveryService deliveryService;

    // ==================== TC-DELIV-01: 配送员登录成功 ====================
    @Test
    void testStaffLoginSuccess() {
        LoginRequest request = new LoginRequest();
        request.setUsername("delivery01");
        request.setPassword("123456");

        DeliveryStaff staff = new DeliveryStaff();
        staff.setId(1L);
        staff.setUsername("delivery01");
        staff.setPassword("$2a$10$encoded");
        staff.setName("张师傅");
        staff.setPhone("13800138000");
        staff.setStatus(1);

        when(deliveryStaffMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(staff);
        when(passwordUtil.matches("123456", "$2a$10$encoded")).thenReturn(true);
        when(jwtUtil.createDeliveryToken(1L, "delivery01")).thenReturn("jwt-delivery-token");

        Map<String, Object> result = deliveryService.staffLogin(request);

        assertEquals("jwt-delivery-token", result.get("token"));
        @SuppressWarnings("unchecked")
        Map<String, Object> info = (Map<String, Object>) result.get("staffInfo");
        assertEquals(1L, info.get("id"));
        assertEquals("张师傅", info.get("name"));
        assertEquals("13800138000", info.get("phone"));
    }

    // ==================== TC-DELIV-02: 配送员登录 — 账号禁用 ====================
    @Test
    void testStaffLoginDisabled() {
        LoginRequest request = new LoginRequest();
        request.setUsername("delivery01");
        request.setPassword("123456");

        DeliveryStaff staff = new DeliveryStaff();
        staff.setId(1L);
        staff.setUsername("delivery01");
        staff.setPassword("$2a$10$encoded");
        staff.setStatus(0); // 禁用

        when(deliveryStaffMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(staff);
        when(passwordUtil.matches("123456", "$2a$10$encoded")).thenReturn(true);

        BizException ex = assertThrows(BizException.class, () -> deliveryService.staffLogin(request));
        assertEquals("配送员账号已禁用", ex.getMessage());
    }

    // ==================== TC-DELIV-03: 分配配送任务 ====================
    @Test
    void testAssignTask() {
        CustomerOrder order = new CustomerOrder();
        order.setId(1L);
        order.setStatus("PENDING_CONFIRM");

        DeliveryStaff staff = new DeliveryStaff();
        staff.setId(1L);
        staff.setName("张师傅");

        when(customerOrderMapper.selectById(1L)).thenReturn(order);
        when(deliveryStaffMapper.selectById(1L)).thenReturn(staff);
        when(deliveryTaskMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(deliveryTaskMapper.insert(any(DeliveryTask.class))).thenReturn(1);
        when(customerOrderMapper.updateById(any(CustomerOrder.class))).thenReturn(1);

        DeliveryTaskRequest request = new DeliveryTaskRequest();
        request.setOrderId(1L);
        request.setStaffId(1L);
        request.setPickupAddress("仓库A");
        request.setDeliveryAddress("客户地址B");
        request.setEstimatedTime("2026-06-23T10:00:00");

        deliveryService.assignTask(request);

        // 验证任务创建
        ArgumentCaptor<DeliveryTask> captor = ArgumentCaptor.forClass(DeliveryTask.class);
        verify(deliveryTaskMapper).insert(captor.capture());
        DeliveryTask task = captor.getValue();
        assertEquals(1L, task.getOrderId());
        assertEquals(1L, task.getStaffId());
        assertEquals("ASSIGNED", task.getStatus());

        // 验证订单状态更新为 TO_DELIVER
        assertEquals("TO_DELIVER", order.getStatus());
    }

    // ==================== TC-DELIV-04: 分配配送任务 — 订单已有任务 ====================
    @Test
    void testAssignTaskAlreadyAssigned() {
        CustomerOrder order = new CustomerOrder();
        order.setId(1L);

        DeliveryStaff staff = new DeliveryStaff();
        staff.setId(1L);

        DeliveryTask existingTask = new DeliveryTask();
        existingTask.setId(10L);
        existingTask.setOrderId(1L);

        when(customerOrderMapper.selectById(1L)).thenReturn(order);
        when(deliveryStaffMapper.selectById(1L)).thenReturn(staff);
        when(deliveryTaskMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existingTask);

        DeliveryTaskRequest request = new DeliveryTaskRequest();
        request.setOrderId(1L);
        request.setStaffId(1L);

        BizException ex = assertThrows(BizException.class, () -> deliveryService.assignTask(request));
        assertEquals("该订单已分配配送任务", ex.getMessage());
    }

    // ==================== TC-DELIV-05: 完成配送任务（含空桶回收） ====================
    @Test
    void testCompleteTaskWithBarrelReturn() {
        DeliveryTask task = new DeliveryTask();
        task.setId(1L);
        task.setOrderId(1L);
        task.setStaffId(1L);
        task.setStatus("DELIVERING");

        CustomerOrder order = new CustomerOrder();
        order.setId(1L);
        order.setUserId(1L);
        order.setTotalAmount(new BigDecimal("50.00"));
        order.setStatus("TO_DELIVER");

        AppUser user = new AppUser();
        user.setId(1L);
        user.setBarrelCount(5);

        when(deliveryTaskMapper.selectById(1L)).thenReturn(task);
        when(deliveryTaskMapper.updateById(any(DeliveryTask.class))).thenReturn(1);
        when(customerOrderMapper.selectById(1L)).thenReturn(order);
        when(appUserMapper.selectById(1L)).thenReturn(user);
        when(appUserMapper.updateById(any(AppUser.class))).thenReturn(1);
        when(customerOrderMapper.updateById(any(CustomerOrder.class))).thenReturn(1);

        DeliveryTaskCompleteRequest request = new DeliveryTaskCompleteRequest();
        request.setPhotoUrl("/uploads/sign.jpg");
        request.setRemark("已签收");
        request.setBarrelReturned(3); // 回收3个空桶

        deliveryService.completeTask(1L, request);

        // 验证任务状态
        assertEquals("COMPLETED", task.getStatus());
        assertEquals(3, task.getBarrelReturned());
        assertNotNull(task.getCompleteTime());

        // 验证订单状态
        assertEquals("COMPLETED", order.getStatus());
        assertEquals(3, order.getBarrelReturnCount());

        // 验证用户桶持有数减少：5 - 3 = 2
        assertEquals(2, user.getBarrelCount());
    }

    // ==================== TC-DELIV-06: 更新任务状态 ====================
    @Test
    void testUpdateTaskStatus() {
        DeliveryTask task = new DeliveryTask();
        task.setId(1L);
        task.setStatus("ASSIGNED");

        when(deliveryTaskMapper.selectById(1L)).thenReturn(task);
        when(deliveryTaskMapper.updateById(any(DeliveryTask.class))).thenReturn(1);

        deliveryService.updateTaskStatus(1L, "PICKED_UP");

        assertEquals("PICKED_UP", task.getStatus());
    }

    // ==================== TC-DELIV-07: 配送员管理 — 新增配送员 ====================
    @Test
    void testSaveStaff() {
        DeliveryStaffRequest request = new DeliveryStaffRequest();
        request.setUsername("delivery02");
        request.setPassword("123456");
        request.setName("李师傅");
        request.setPhone("13900139000");

        when(deliveryStaffMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(passwordUtil.encode("123456")).thenReturn("$2a$10$encoded");
        when(deliveryStaffMapper.insert(any(DeliveryStaff.class))).thenReturn(1);

        deliveryService.saveStaff(request);

        ArgumentCaptor<DeliveryStaff> captor = ArgumentCaptor.forClass(DeliveryStaff.class);
        verify(deliveryStaffMapper).insert(captor.capture());
        DeliveryStaff saved = captor.getValue();
        assertEquals("delivery02", saved.getUsername());
        assertEquals("李师傅", saved.getName());
        assertEquals(1, saved.getStatus());
    }

    // ==================== TC-DELIV-08: 删除配送员 ====================
    @Test
    void testDeleteStaff() {
        when(deliveryStaffMapper.deleteById(1L)).thenReturn(1);
        deliveryService.deleteStaff(1L);
        verify(deliveryStaffMapper).deleteById(1L);
    }
}
