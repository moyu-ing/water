package com.waterdelivery.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.waterdelivery.common.BizException;
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
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 管理后台模块测试 — 数据看板、管理员管理、角色管理、菜单管理、商品/分类管理、订单管理
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class AdminServiceStatisticsTest {

    @Mock private AppUserMapper appUserMapper;
    @Mock private AdminUserMapper adminUserMapper;
    @Mock private AdminRoleMapper adminRoleMapper;
    @Mock private AdminMenuMapper adminMenuMapper;
    @Mock private AdminUserRoleMapper adminUserRoleMapper;
    @Mock private AdminRoleMenuMapper adminRoleMenuMapper;
    @Mock private CategoryMapper categoryMapper;
    @Mock private ProductMapper productMapper;
    @Mock private CustomerOrderMapper customerOrderMapper;
    @Mock private OrderItemMapper orderItemMapper;
    @Mock private PasswordUtil passwordUtil;
    @Mock private PointsService pointsService;

    @InjectMocks
    private AdminService adminService;

    // 辅助方法：设置统计页公共mock
    private void mockStatisticsCommons() {
        lenient().when(appUserMapper.selectCount(any())).thenReturn(0L);
        lenient().when(productMapper.selectCount(any())).thenReturn(0L);
        lenient().when(categoryMapper.selectCount(any())).thenReturn(0L);
        lenient().when(customerOrderMapper.selectCount(any())).thenReturn(0L);
        lenient().when(customerOrderMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());
        lenient().when(productMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());
        lenient().when(categoryMapper.selectList(any())).thenReturn(Collections.emptyList());
        lenient().when(productMapper.selectList(any())).thenReturn(Collections.emptyList());
    }

    // ==================== 数据看板 ====================

    // TC-DASH-01: 获取数据看板基本统计数据
    @Test
    void testGetStatisticsBasic() {
        lenient().when(appUserMapper.selectCount(any())).thenReturn(100L);
        lenient().when(productMapper.selectCount(any())).thenReturn(50L);
        lenient().when(categoryMapper.selectCount(any())).thenReturn(5L);

        // 总订单数
        lenient().when(customerOrderMapper.selectCount(any())).thenReturn(200L);

        // 今日数据
        CustomerOrder todayOrder = new CustomerOrder();
        todayOrder.setTotalAmount(new BigDecimal("100.00"));
        lenient().when(customerOrderMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(todayOrder));

        // 空list for others
        lenient().when(productMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());
        lenient().when(categoryMapper.selectList(any())).thenReturn(Collections.emptyList());
        lenient().when(productMapper.selectList(any())).thenReturn(Collections.emptyList());

        Map<String, Object> stats = adminService.getStatistics();

        assertEquals(100L, stats.get("totalUsers"));
        assertEquals(50L, stats.get("totalProducts"));
        assertEquals(5L, stats.get("totalCategories"));
    }

    // TC-DASH-02: 今日订单数和营收统计
    @Test
    void testStatisticsTodayOrders() {
        mockStatisticsCommons();

        CustomerOrder o1 = new CustomerOrder();
        o1.setTotalAmount(new BigDecimal("50.00"));
        CustomerOrder o2 = new CustomerOrder();
        o2.setTotalAmount(new BigDecimal("30.00"));

        lenient().when(customerOrderMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(o1, o2));

        Map<String, Object> stats = adminService.getStatistics();

        assertEquals(2, stats.get("todayNewOrders"));
        assertEquals(new BigDecimal("80.00"), stats.get("todayRevenue"));
    }

    // TC-DASH-03: 低库存预警
    @Test
    void testStatisticsLowStock() {
        mockStatisticsCommons();

        Product lowStockProduct = new Product();
        lowStockProduct.setId(10L);
        lowStockProduct.setName("库存不足商品");
        lowStockProduct.setStock(3);
        lowStockProduct.setStatus(1);

        lenient().when(productMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(lowStockProduct));

        Map<String, Object> stats = adminService.getStatistics();

        List<Product> lowStock = (List<Product>) stats.get("lowStockProducts");
        assertEquals(1, lowStock.size());
        assertEquals("库存不足商品", lowStock.get(0).getName());
    }

    // TC-DASH-04: 近7日订单趋势
    @Test
    void testStatisticsSevenDayTrend() {
        mockStatisticsCommons();

        Map<String, Object> stats = adminService.getStatistics();

        List<Map<String, Object>> trend = (List<Map<String, Object>>) stats.get("sevenDayTrend");
        assertEquals(7, trend.size());
        // 验证所有日期都存在
        for (int i = 0; i < 7; i++) {
            assertNotNull(trend.get(i).get("date"));
            assertNotNull(trend.get(i).get("count"));
        }
    }

    // TC-DASH-05: TOP5分类统计
    @Test
    void testStatisticsTopCategories() {
        mockStatisticsCommons();

        Category cat = new Category();
        cat.setId(1L);
        cat.setName("桶装水");

        Product p1 = new Product();
        p1.setCategoryId(1L);
        Product p2 = new Product();
        p2.setCategoryId(1L);

        lenient().when(categoryMapper.selectList(any())).thenReturn(List.of(cat));
        lenient().when(productMapper.selectList(any())).thenReturn(List.of(p1, p2));

        Map<String, Object> stats = adminService.getStatistics();

        List<Map<String, Object>> topCategories = (List<Map<String, Object>>) stats.get("topCategories");
        assertEquals(1, topCategories.size());
        assertEquals("桶装水", topCategories.get(0).get("categoryName"));
        assertEquals(2L, topCategories.get(0).get("count"));
    }

    // TC-DASH-06: 订单状态分布
    @Test
    void testStatisticsHasStatusDistribution() {
        mockStatisticsCommons();

        Map<String, Object> stats = adminService.getStatistics();

        // 验证状态分布字段存在且为Long类型
        assertNotNull(stats.get("pendingCount"));
        assertNotNull(stats.get("toDeliverCount"));
        assertNotNull(stats.get("completedCount"));
        assertNotNull(stats.get("cancelledCount"));
        assertTrue(stats.get("pendingCount") instanceof Long);
        assertTrue(stats.get("completedCount") instanceof Long);
    }

    // ==================== 管理员管理 ====================

    @Test
    void testSaveAdminUser() {
        AdminUserRequest request = new AdminUserRequest();
        request.setUsername("admin2");
        request.setPassword("123456");
        request.setNickname("管理员2");
        request.setStatus(1);
        request.setRoleIds(List.of(1L, 2L));

        when(adminUserMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(passwordUtil.encode("123456")).thenReturn("$2a$10$encoded");
        when(adminUserMapper.insert(any(AdminUser.class))).thenReturn(1);
        when(adminUserRoleMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(1);
        when(adminUserRoleMapper.insert(any(AdminUserRole.class))).thenReturn(1);

        adminService.saveAdminUser(request);

        ArgumentCaptor<AdminUser> captor = ArgumentCaptor.forClass(AdminUser.class);
        verify(adminUserMapper).insert(captor.capture());
        AdminUser saved = captor.getValue();
        assertEquals("admin2", saved.getUsername());
        assertEquals("$2a$10$encoded", saved.getPassword());
        assertEquals("管理员2", saved.getNickname());
    }

    @Test
    void testSaveAdminUserEmptyUsername() {
        AdminUserRequest request = new AdminUserRequest();
        request.setUsername("");
        request.setPassword("123456");

        BizException ex = assertThrows(BizException.class, () -> adminService.saveAdminUser(request));
        assertEquals("管理员账号不能为空", ex.getMessage());
    }

    @Test
    void testSaveAdminUserEmptyPassword() {
        AdminUserRequest request = new AdminUserRequest();
        request.setUsername("admin");
        request.setPassword("");

        BizException ex = assertThrows(BizException.class, () -> adminService.saveAdminUser(request));
        assertEquals("管理员密码不能为空", ex.getMessage());
    }

    @Test
    void testDeleteAdminUser() {
        when(adminUserMapper.deleteById(1L)).thenReturn(1);
        when(adminUserRoleMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(1);

        adminService.deleteAdminUser(1L);

        verify(adminUserMapper).deleteById(1L);
        verify(adminUserRoleMapper).delete(any(LambdaQueryWrapper.class));
    }

    // ==================== 角色管理 ====================

    @Test
    void testSaveRole() {
        RoleRequest request = new RoleRequest();
        request.setName("运营角色");
        request.setCode("OPERATOR");
        request.setDescription("运营管理权限");
        request.setStatus(1);
        request.setMenuIds(List.of(1L, 2L));

        when(adminRoleMapper.insert(any(AdminRole.class))).thenReturn(1);
        when(adminRoleMenuMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(1);
        when(adminRoleMenuMapper.insert(any(AdminRoleMenu.class))).thenReturn(1);

        adminService.saveRole(request);

        ArgumentCaptor<AdminRole> captor = ArgumentCaptor.forClass(AdminRole.class);
        verify(adminRoleMapper).insert(captor.capture());
        AdminRole saved = captor.getValue();
        assertEquals("运营角色", saved.getName());
        assertEquals("OPERATOR", saved.getCode());
    }

    @Test
    void testDeleteRole() {
        when(adminRoleMapper.deleteById(1L)).thenReturn(1);
        when(adminRoleMenuMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(1);
        when(adminUserRoleMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(1);

        adminService.deleteRole(1L);

        verify(adminRoleMapper).deleteById(1L);
        verify(adminRoleMenuMapper).delete(any(LambdaQueryWrapper.class));
        verify(adminUserRoleMapper).delete(any(LambdaQueryWrapper.class));
    }

    // ==================== 菜单管理 ====================

    @Test
    void testSaveMenu() {
        MenuRequest request = new MenuRequest();
        request.setParentId(0L);
        request.setName("商品管理");
        request.setPath("/admin/products");
        request.setComponent("ProductsPage");
        request.setPermissionCode("product:view");
        request.setType(1);
        request.setSortNum(1);
        request.setStatus(1);

        when(adminMenuMapper.insert(any(AdminMenu.class))).thenReturn(1);

        adminService.saveMenu(request);

        ArgumentCaptor<AdminMenu> captor = ArgumentCaptor.forClass(AdminMenu.class);
        verify(adminMenuMapper).insert(captor.capture());
        AdminMenu saved = captor.getValue();
        assertEquals("商品管理", saved.getName());
        assertEquals("product:view", saved.getPermissionCode());
        assertEquals(1, saved.getType());
    }

    @Test
    void testDeleteMenu() {
        when(adminMenuMapper.deleteById(1L)).thenReturn(1);
        when(adminRoleMenuMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(1);

        adminService.deleteMenu(1L);

        verify(adminMenuMapper).deleteById(1L);
        verify(adminRoleMenuMapper).delete(any(LambdaQueryWrapper.class));
    }

    // ==================== 商品/分类管理 ====================

    @Test
    void testSaveCategory() {
        CategoryRequest request = new CategoryRequest();
        request.setParentId(0L);
        request.setName("桶装水");
        request.setImage("/uploads/cat.jpg");
        request.setSortNum(1);
        request.setStatus(1);

        when(categoryMapper.insert(any(Category.class))).thenReturn(1);

        adminService.saveCategory(request);

        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        verify(categoryMapper).insert(captor.capture());
        Category saved = captor.getValue();
        assertEquals("桶装水", saved.getName());
        assertEquals(1, saved.getSortNum());
    }

    @Test
    void testSaveProduct() {
        ProductRequest request = new ProductRequest();
        request.setCategoryId(1L);
        request.setName("农夫山泉");
        request.setPrice(new BigDecimal("15.00"));
        request.setStock(100);
        request.setStatus(1);

        when(productMapper.insert(any(Product.class))).thenReturn(1);

        adminService.saveProduct(request);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productMapper).insert(captor.capture());
        Product saved = captor.getValue();
        assertEquals("农夫山泉", saved.getName());
        assertEquals(new BigDecimal("15.00"), saved.getPrice());
        assertEquals(100, saved.getStock());
    }

    @Test
    void testDeleteProduct() {
        when(productMapper.deleteById(1L)).thenReturn(1);
        adminService.deleteProduct(1L);
        verify(productMapper).deleteById(1L);
    }

    @Test
    void testDeleteCategory() {
        when(categoryMapper.deleteById(1L)).thenReturn(1);
        adminService.deleteCategory(1L);
        verify(categoryMapper).deleteById(1L);
    }

    // ==================== 订单管理 ====================

    @Test
    void testUpdateOrderStatus() {
        CustomerOrder order = new CustomerOrder();
        order.setId(1L);
        order.setStatus("PENDING_CONFIRM");

        when(customerOrderMapper.selectById(1L)).thenReturn(order);
        when(customerOrderMapper.updateById(any(CustomerOrder.class))).thenReturn(1);

        adminService.updateOrderStatus(1L, "TO_DELIVER");

        assertEquals("TO_DELIVER", order.getStatus());
    }

    @Test
    void testUpdateOrderStatusCompleted() {
        CustomerOrder order = new CustomerOrder();
        order.setId(1L);
        order.setUserId(1L);
        order.setStatus("TO_DELIVER");
        order.setTotalAmount(new BigDecimal("88.00"));

        when(customerOrderMapper.selectById(1L)).thenReturn(order);
        when(customerOrderMapper.updateById(any(CustomerOrder.class))).thenReturn(1);

        adminService.updateOrderStatus(1L, "COMPLETED");

        assertEquals("COMPLETED", order.getStatus());
        verify(pointsService).earnPoints(1L, 88, "ORDER", "订单完成赠送积分");
    }

    @Test
    void testUpdateOrderStatusInvalid() {
        CustomerOrder order = new CustomerOrder();
        order.setId(1L);

        when(customerOrderMapper.selectById(1L)).thenReturn(order);

        BizException ex = assertThrows(BizException.class, () ->
                adminService.updateOrderStatus(1L, "INVALID_STATUS"));
        assertEquals("订单状态不正确", ex.getMessage());
    }

    @Test
    void testUpdateOrderStatusOrderNotFound() {
        when(customerOrderMapper.selectById(999L)).thenReturn(null);

        BizException ex = assertThrows(BizException.class, () ->
                adminService.updateOrderStatus(999L, "COMPLETED"));
        assertEquals("订单不存在", ex.getMessage());
    }
}
