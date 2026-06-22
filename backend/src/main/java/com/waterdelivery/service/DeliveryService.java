package com.waterdelivery.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.waterdelivery.common.BizException;
import com.waterdelivery.config.JwtUtil;
import com.waterdelivery.config.PasswordUtil;
import com.waterdelivery.dto.*;
import com.waterdelivery.entity.*;
import com.waterdelivery.mapper.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DeliveryService {

    private final DeliveryStaffMapper deliveryStaffMapper;
    private final DeliveryTaskMapper deliveryTaskMapper;
    private final CustomerOrderMapper customerOrderMapper;
    private final OrderItemMapper orderItemMapper;
    private final AppUserMapper appUserMapper;
    private final PasswordUtil passwordUtil;
    private final JwtUtil jwtUtil;
    private final com.waterdelivery.service.PointsService pointsService;

    public DeliveryService(DeliveryStaffMapper deliveryStaffMapper,
                           DeliveryTaskMapper deliveryTaskMapper,
                           CustomerOrderMapper customerOrderMapper,
                           OrderItemMapper orderItemMapper,
                           AppUserMapper appUserMapper,
                           PasswordUtil passwordUtil,
                           JwtUtil jwtUtil,
                           com.waterdelivery.service.PointsService pointsService) {
        this.deliveryStaffMapper = deliveryStaffMapper;
        this.deliveryTaskMapper = deliveryTaskMapper;
        this.customerOrderMapper = customerOrderMapper;
        this.orderItemMapper = orderItemMapper;
        this.appUserMapper = appUserMapper;
        this.passwordUtil = passwordUtil;
        this.jwtUtil = jwtUtil;
        this.pointsService = pointsService;
    }

    // ==================== 配送员登录 ====================
    public Map<String, Object> staffLogin(LoginRequest request) {
        DeliveryStaff staff = deliveryStaffMapper.selectOne(new LambdaQueryWrapper<DeliveryStaff>()
                .eq(DeliveryStaff::getUsername, request.getUsername()));
        if (staff == null || !passwordUtil.matches(request.getPassword(), staff.getPassword())) {
            throw new BizException("用户名或密码错误");
        }
        if (staff.getStatus() != 1) {
            throw new BizException("配送员账号已禁用");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("token", jwtUtil.createDeliveryToken(staff.getId(), staff.getUsername()));
        Map<String, Object> info = new HashMap<>();
        info.put("id", staff.getId());
        info.put("name", staff.getName());
        info.put("phone", staff.getPhone());
        result.put("staffInfo", info);
        return result;
    }

    public Map<String, Object> getStaffProfile(Long staffId) {
        DeliveryStaff staff = deliveryStaffMapper.selectById(staffId);
        if (staff == null) {
            throw new BizException("配送员不存在");
        }
        Map<String, Object> info = new HashMap<>();
        info.put("id", staff.getId());
        info.put("name", staff.getName());
        info.put("phone", staff.getPhone());
        return info;
    }

    // ==================== 配送员管理（管理员） ====================
    public List<DeliveryStaff> listStaff() {
        return deliveryStaffMapper.selectList(new LambdaQueryWrapper<DeliveryStaff>()
                .orderByDesc(DeliveryStaff::getId));
    }

    public void saveStaff(DeliveryStaffRequest request) {
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new BizException("账号不能为空");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new BizException("密码不能为空");
        }
        DeliveryStaff existing = deliveryStaffMapper.selectOne(new LambdaQueryWrapper<DeliveryStaff>()
                .eq(DeliveryStaff::getUsername, request.getUsername()));
        if (existing != null) {
            throw new BizException("配送员账号已存在");
        }
        DeliveryStaff staff = new DeliveryStaff();
        staff.setUsername(request.getUsername());
        staff.setPassword(passwordUtil.encode(request.getPassword()));
        staff.setName(request.getName());
        staff.setPhone(request.getPhone());
        staff.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        staff.setCreateTime(LocalDateTime.now());
        staff.setUpdateTime(LocalDateTime.now());
        deliveryStaffMapper.insert(staff);
    }

    public void updateStaff(Long id, DeliveryStaffRequest request) {
        DeliveryStaff staff = deliveryStaffMapper.selectById(id);
        if (staff == null) {
            throw new BizException("配送员不存在");
        }
        staff.setName(request.getName());
        staff.setPhone(request.getPhone());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            staff.setPassword(passwordUtil.encode(request.getPassword()));
        }
        if (request.getStatus() != null) {
            staff.setStatus(request.getStatus());
        }
        staff.setUpdateTime(LocalDateTime.now());
        deliveryStaffMapper.updateById(staff);
    }

    public void deleteStaff(Long id) {
        deliveryStaffMapper.deleteById(id);
    }

    // ==================== 配送任务管理 ====================
    public List<Map<String, Object>> listAllTasks() {
        Map<Long, String> staffMap = deliveryStaffMapper.selectList(null).stream()
                .collect(Collectors.toMap(DeliveryStaff::getId, DeliveryStaff::getName));
        return deliveryTaskMapper.selectList(new LambdaQueryWrapper<DeliveryTask>()
                        .orderByDesc(DeliveryTask::getId))
                .stream()
                .map(task -> buildTaskMap(task, staffMap))
                .toList();
    }

    public List<Map<String, Object>> listStaffTasks(Long staffId) {
        return deliveryTaskMapper.selectList(new LambdaQueryWrapper<DeliveryTask>()
                        .eq(DeliveryTask::getStaffId, staffId)
                        .orderByDesc(DeliveryTask::getId))
                .stream()
                .map(task -> buildTaskMap(task, null))
                .toList();
    }

    public void assignTask(DeliveryTaskRequest request) {
        CustomerOrder order = customerOrderMapper.selectById(request.getOrderId());
        if (order == null) {
            throw new BizException("订单不存在");
        }
        DeliveryStaff staff = deliveryStaffMapper.selectById(request.getStaffId());
        if (staff == null) {
            throw new BizException("配送员不存在");
        }
        // 检查是否已有任务
        DeliveryTask existing = deliveryTaskMapper.selectOne(new LambdaQueryWrapper<DeliveryTask>()
                .eq(DeliveryTask::getOrderId, request.getOrderId()));
        if (existing != null) {
            throw new BizException("该订单已分配配送任务");
        }
        DeliveryTask task = new DeliveryTask();
        task.setOrderId(request.getOrderId());
        task.setStaffId(request.getStaffId());
        task.setStatus("ASSIGNED");
        task.setPickupAddress(request.getPickupAddress());
        task.setDeliveryAddress(request.getDeliveryAddress());
        if (request.getEstimatedTime() != null && !request.getEstimatedTime().isBlank()) {
            task.setEstimatedTime(LocalDateTime.parse(request.getEstimatedTime()));
        }
        task.setBarrelReturned(0);
        task.setCreateTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());
        deliveryTaskMapper.insert(task);
        // 更新订单状态为待配送
        order.setStatus("TO_DELIVER");
        order.setUpdateTime(LocalDateTime.now());
        customerOrderMapper.updateById(order);
    }

    public void updateTaskStatus(Long taskId, String status) {
        DeliveryTask task = deliveryTaskMapper.selectById(taskId);
        if (task == null) {
            throw new BizException("配送任务不存在");
        }
        List<String> allowed = List.of("PICKED_UP", "DELIVERING");
        if (!allowed.contains(status)) {
            throw new BizException("无效的任务状态");
        }
        task.setStatus(status);
        task.setUpdateTime(LocalDateTime.now());
        deliveryTaskMapper.updateById(task);
    }

    @Transactional(rollbackFor = Exception.class)
    public void completeTask(Long taskId, DeliveryTaskCompleteRequest request) {
        DeliveryTask task = deliveryTaskMapper.selectById(taskId);
        if (task == null) {
            throw new BizException("配送任务不存在");
        }
        task.setStatus("COMPLETED");
        task.setCompleteTime(LocalDateTime.now());
        task.setPhotoUrl(request.getPhotoUrl());
        task.setRemark(request.getRemark());
        if (request.getBarrelReturned() != null) {
            task.setBarrelReturned(request.getBarrelReturned());
        }
        task.setUpdateTime(LocalDateTime.now());
        deliveryTaskMapper.updateById(task);
        // 更新订单状态
        CustomerOrder order = customerOrderMapper.selectById(task.getOrderId());
        if (order != null) {
            order.setStatus("COMPLETED");
            if (task.getBarrelReturned() > 0) {
                order.setBarrelReturnCount(task.getBarrelReturned());
                // 扣减用户的桶持有数
                AppUser user = appUserMapper.selectById(order.getUserId());
                if (user != null) {
                    int currentBarrelCount = user.getBarrelCount() != null ? user.getBarrelCount() : 0;
                    int newCount = Math.max(0, currentBarrelCount - task.getBarrelReturned());
                    user.setBarrelCount(newCount);
                    user.setUpdateTime(LocalDateTime.now());
                    appUserMapper.updateById(user);
                }
            }
            // 订单完成时发放积分（1元 = 1积分）
            if (order.getPointsEarned() == null || order.getPointsEarned() == 0) {
                int pointsEarned = order.getTotalAmount() != null ? (int) Math.round(order.getTotalAmount().doubleValue()) : 0;
                if (pointsEarned > 0) {
                    pointsService.earnPoints(order.getUserId(), pointsEarned, "ORDER", "订单完成赠送积分");
                    order.setPointsEarned(pointsEarned);
                }
            }
            order.setUpdateTime(LocalDateTime.now());
            customerOrderMapper.updateById(order);
        }
    }

    private Map<String, Object> buildTaskMap(DeliveryTask task, Map<Long, String> staffMap) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", task.getId());
        map.put("orderId", task.getOrderId());
        map.put("staffId", task.getStaffId());
        map.put("status", task.getStatus());
        map.put("pickupAddress", task.getPickupAddress());
        map.put("deliveryAddress", task.getDeliveryAddress());
        map.put("estimatedTime", task.getEstimatedTime());
        map.put("completeTime", task.getCompleteTime());
        map.put("photoUrl", task.getPhotoUrl());
        map.put("remark", task.getRemark());
        map.put("barrelReturned", task.getBarrelReturned());
        map.put("createTime", task.getCreateTime());
        if (staffMap != null) {
            map.put("staffName", staffMap.get(task.getStaffId()));
        }
        // 加载订单信息
        CustomerOrder order = customerOrderMapper.selectById(task.getOrderId());
        if (order != null) {
            map.put("orderNo", order.getOrderNo());
            map.put("orderContactName", order.getContactName());
            map.put("orderContactPhone", order.getContactPhone());
            map.put("orderFullAddress", order.getFullAddress());
            map.put("orderTotalAmount", order.getTotalAmount());
            map.put("orderStatus", order.getStatus());
        }
        return map;
    }
}
