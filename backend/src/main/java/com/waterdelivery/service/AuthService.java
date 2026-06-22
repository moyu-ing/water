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
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
public class AuthService {

    private final AppUserMapper appUserMapper;
    private final AdminUserMapper adminUserMapper;
    private final PasswordUtil passwordUtil;
    private final JwtUtil jwtUtil;
    private final PermissionService permissionService;

    public AuthService(AppUserMapper appUserMapper,
                       AdminUserMapper adminUserMapper,
                       PasswordUtil passwordUtil,
                       JwtUtil jwtUtil,
                       PermissionService permissionService) {
        this.appUserMapper = appUserMapper;
        this.adminUserMapper = adminUserMapper;
        this.passwordUtil = passwordUtil;
        this.jwtUtil = jwtUtil;
        this.permissionService = permissionService;
    }

    public void register(UserRegisterRequest request) {
        AppUser existing = appUserMapper.selectOne(new LambdaQueryWrapper<AppUser>()
                .eq(AppUser::getUsername, request.getUsername()));
        if (existing != null) {
            throw new BizException("用户名已存在");
        }
        AppUser user = new AppUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordUtil.encode(request.getPassword()));
        user.setNickname(request.getNickname());
        user.setPhone(request.getPhone());
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        appUserMapper.insert(user);
    }

    public Map<String, Object> userLogin(LoginRequest request) {
        AppUser user = appUserMapper.selectOne(new LambdaQueryWrapper<AppUser>()
                .eq(AppUser::getUsername, request.getUsername()));
        if (user == null || !passwordUtil.matches(request.getPassword(), user.getPassword())) {
            throw new BizException("用户名或密码错误");
        }
        if (user.getStatus() != 1) {
            throw new BizException("账号已禁用");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("token", jwtUtil.createUserToken(user.getId(), user.getUsername()));
        result.put("userInfo", buildUserInfo(user));
        return result;
    }

    public Map<String, Object> adminLogin(LoginRequest request) {
        AdminUser adminUser = adminUserMapper.selectOne(new LambdaQueryWrapper<AdminUser>()
                .eq(AdminUser::getUsername, request.getUsername()));
        if (adminUser == null || !passwordUtil.matches(request.getPassword(), adminUser.getPassword())) {
            throw new BizException("用户名或密码错误");
        }
        if (adminUser.getStatus() != 1) {
            throw new BizException("管理员账号已禁用");
        }
        Set<String> permissions = permissionService.getPermissionsByAdminId(adminUser.getId());
        Map<String, Object> result = new HashMap<>();
        result.put("token", jwtUtil.createAdminToken(adminUser.getId(), adminUser.getUsername(), permissions.stream().toList()));
        result.put("adminInfo", buildAdminInfo(adminUser, permissions));
        return result;
    }

    public Map<String, Object> getCurrentUserProfile() {
        AppUser user = appUserMapper.selectById(CurrentContext.getUserId());
        return buildUserInfo(user);
    }

    public Map<String, Object> getCurrentAdminProfile() {
        AdminUser admin = adminUserMapper.selectById(CurrentContext.getAdminId());
        Set<String> permissions = permissionService.getPermissionsByAdminId(admin.getId());
        return buildAdminInfo(admin, permissions);
    }

    private Map<String, Object> buildUserInfo(AppUser user) {
        Map<String, Object> info = new HashMap<>();
        info.put("id", user.getId());
        info.put("username", user.getUsername());
        info.put("nickname", user.getNickname());
        info.put("phone", user.getPhone());
        info.put("barrelCount", user.getBarrelCount());
        info.put("points", user.getPoints());
        return info;
    }

    private Map<String, Object> buildAdminInfo(AdminUser user, Set<String> permissions) {
        Map<String, Object> info = new HashMap<>();
        info.put("id", user.getId());
        info.put("username", user.getUsername());
        info.put("nickname", user.getNickname());
        info.put("permissions", permissions);
        info.put("roleIds", permissionService.getRoleIdsByAdminId(user.getId()));
        return info;
    }
}
