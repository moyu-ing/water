package com.waterdelivery.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.waterdelivery.common.BizException;
import com.waterdelivery.common.CurrentContext;
import com.waterdelivery.config.JwtUtil;
import com.waterdelivery.config.PasswordUtil;
import com.waterdelivery.dto.LoginRequest;
import com.waterdelivery.dto.UserRegisterRequest;
import com.waterdelivery.entity.AdminUser;
import com.waterdelivery.entity.AppUser;
import com.waterdelivery.mapper.AdminUserMapper;
import com.waterdelivery.mapper.AppUserMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 用户认证模块测试 — 覆盖 TC-AUTH-01 到 TC-AUTH-09
 */
@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private AppUserMapper appUserMapper;
    @Mock
    private AdminUserMapper adminUserMapper;
    @Mock
    private PasswordUtil passwordUtil;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private PermissionService permissionService;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        CurrentContext.clear();
    }

    @AfterEach
    void tearDown() {
        CurrentContext.clear();
    }

    // ==================== TC-AUTH-01: 用户注册成功 ====================
    @Test
    void testRegisterSuccess() {
        UserRegisterRequest request = new UserRegisterRequest();
        request.setUsername("testuser");
        request.setPassword("123456");
        request.setNickname("测试用户");
        request.setPhone("13800138000");

        when(appUserMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(passwordUtil.encode("123456")).thenReturn("$2a$10$encoded");
        when(appUserMapper.insert(any(AppUser.class))).thenReturn(1);

        authService.register(request);

        ArgumentCaptor<AppUser> captor = ArgumentCaptor.forClass(AppUser.class);
        verify(appUserMapper).insert(captor.capture());
        AppUser saved = captor.getValue();
        assertEquals("testuser", saved.getUsername());
        assertEquals("$2a$10$encoded", saved.getPassword());
        assertEquals("测试用户", saved.getNickname());
        assertEquals("13800138000", saved.getPhone());
        assertEquals(1, saved.getStatus());
        assertNotNull(saved.getCreateTime());
        assertNotNull(saved.getUpdateTime());
    }

    // ==================== TC-AUTH-02: 用户名已存在 ====================
    @Test
    void testRegisterDuplicateUsername() {
        UserRegisterRequest request = new UserRegisterRequest();
        request.setUsername("testuser");
        request.setPassword("123456");
        request.setNickname("测试用户");
        request.setPhone("13800138000");

        AppUser existing = new AppUser();
        existing.setId(1L);
        existing.setUsername("testuser");
        when(appUserMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existing);

        BizException ex = assertThrows(BizException.class, () -> authService.register(request));
        assertEquals("用户名已存在", ex.getMessage());
        verify(appUserMapper, never()).insert(any());
    }

    // ==================== TC-AUTH-03: 用户登录成功 ====================
    @Test
    void testUserLoginSuccess() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("123456");

        AppUser user = new AppUser();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("$2a$10$encoded");
        user.setNickname("测试用户");
        user.setPhone("13800138000");
        user.setBarrelCount(3);
        user.setPoints(500);
        user.setStatus(1);

        when(appUserMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);
        when(passwordUtil.matches("123456", "$2a$10$encoded")).thenReturn(true);
        when(jwtUtil.createUserToken(1L, "testuser")).thenReturn("jwt-token-user");

        Map<String, Object> result = authService.userLogin(request);

        assertEquals("jwt-token-user", result.get("token"));
        @SuppressWarnings("unchecked")
        Map<String, Object> info = (Map<String, Object>) result.get("userInfo");
        assertEquals(1L, info.get("id"));
        assertEquals("testuser", info.get("username"));
        assertEquals("测试用户", info.get("nickname"));
        assertEquals("13800138000", info.get("phone"));
        assertEquals(3, info.get("barrelCount"));
        assertEquals(500, info.get("points"));
    }

    // ==================== TC-AUTH-04: 用户登录失败 — 密码错误 ====================
    @Test
    void testUserLoginWrongPassword() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("wrong");

        AppUser user = new AppUser();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("$2a$10$encoded");
        user.setStatus(1);

        when(appUserMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);
        when(passwordUtil.matches("wrong", "$2a$10$encoded")).thenReturn(false);

        BizException ex = assertThrows(BizException.class, () -> authService.userLogin(request));
        assertEquals("用户名或密码错误", ex.getMessage());
    }

    // ==================== TC-AUTH-05: 管理员登录成功 ====================
    @Test
    void testAdminLoginSuccess() {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("admin123");

        AdminUser admin = new AdminUser();
        admin.setId(1L);
        admin.setUsername("admin");
        admin.setPassword("$2a$10$encoded");
        admin.setNickname("超级管理员");
        admin.setStatus(1);

        Set<String> permissions = Set.of("admin:dashboard", "product:edit", "order:view");
        List<Long> roleIds = List.of(1L);

        when(adminUserMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(admin);
        when(passwordUtil.matches("admin123", "$2a$10$encoded")).thenReturn(true);
        when(permissionService.getPermissionsByAdminId(1L)).thenReturn(permissions);
        when(permissionService.getRoleIdsByAdminId(1L)).thenReturn(roleIds);
        when(jwtUtil.createAdminToken(eq(1L), eq("admin"), anyList())).thenReturn("jwt-token-admin");

        Map<String, Object> result = authService.adminLogin(request);

        assertEquals("jwt-token-admin", result.get("token"));
        @SuppressWarnings("unchecked")
        Map<String, Object> info = (Map<String, Object>) result.get("adminInfo");
        assertEquals(1L, info.get("id"));
        assertEquals("admin", info.get("username"));
        assertEquals("超级管理员", info.get("nickname"));
        assertEquals(permissions, info.get("permissions"));
        assertEquals(roleIds, info.get("roleIds"));
    }

    // ==================== TC-AUTH-06: 配送员登录成功（见 DeliveryServiceTest） ====================
    // 注：配送员登录在 DeliveryService 中实现，此处不做重复测试

    // ==================== TC-AUTH-07: 管理员登录 — 账号已禁用 ====================
    @Test
    void testAdminLoginDisabled() {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("123456");

        AdminUser admin = new AdminUser();
        admin.setId(1L);
        admin.setUsername("admin");
        admin.setPassword("$2a$10$encoded");
        admin.setStatus(0); // 已禁用

        when(adminUserMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(admin);
        when(passwordUtil.matches("123456", "$2a$10$encoded")).thenReturn(true);

        BizException ex = assertThrows(BizException.class, () -> authService.adminLogin(request));
        assertEquals("管理员账号已禁用", ex.getMessage());
    }

    // ==================== TC-AUTH-08: 用户登录 — 账号已禁用 ====================
    @Test
    void testUserLoginDisabled() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("123456");

        AppUser user = new AppUser();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("$2a$10$encoded");
        user.setStatus(0); // 已禁用

        when(appUserMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);
        when(passwordUtil.matches("123456", "$2a$10$encoded")).thenReturn(true);

        BizException ex = assertThrows(BizException.class, () -> authService.userLogin(request));
        assertEquals("账号已禁用", ex.getMessage());
    }

    // ==================== TC-AUTH-09: 获取当前用户信息 ====================
    @Test
    void testGetCurrentUserProfile() {
        CurrentContext.TokenPayload payload = new CurrentContext.TokenPayload();
        payload.setType("USER");
        payload.setUserId(1L);
        payload.setUsername("testuser");
        CurrentContext.set(payload);

        AppUser user = new AppUser();
        user.setId(1L);
        user.setUsername("testuser");
        user.setNickname("测试用户");
        user.setPhone("13800138000");
        user.setBarrelCount(2);
        user.setPoints(300);

        when(appUserMapper.selectById(1L)).thenReturn(user);

        Map<String, Object> profile = authService.getCurrentUserProfile();
        assertEquals(1L, profile.get("id"));
        assertEquals("testuser", profile.get("username"));
        assertEquals("测试用户", profile.get("nickname"));
        assertEquals(300, profile.get("points"));
    }
}
