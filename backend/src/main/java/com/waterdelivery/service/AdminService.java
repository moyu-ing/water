package com.waterdelivery.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.waterdelivery.common.BizException;
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
public class AdminService {

    private final AppUserMapper appUserMapper;
    private final AdminUserMapper adminUserMapper;
    private final AdminRoleMapper adminRoleMapper;
    private final AdminMenuMapper adminMenuMapper;
    private final AdminUserRoleMapper adminUserRoleMapper;
    private final AdminRoleMenuMapper adminRoleMenuMapper;
    private final CategoryMapper categoryMapper;
    private final ProductMapper productMapper;
    private final CustomerOrderMapper customerOrderMapper;
    private final OrderItemMapper orderItemMapper;
    private final PasswordUtil passwordUtil;

    public AdminService(AppUserMapper appUserMapper,
                        AdminUserMapper adminUserMapper,
                        AdminRoleMapper adminRoleMapper,
                        AdminMenuMapper adminMenuMapper,
                        AdminUserRoleMapper adminUserRoleMapper,
                        AdminRoleMenuMapper adminRoleMenuMapper,
                        CategoryMapper categoryMapper,
                        ProductMapper productMapper,
                        CustomerOrderMapper customerOrderMapper,
                        OrderItemMapper orderItemMapper,
                        PasswordUtil passwordUtil) {
        this.appUserMapper = appUserMapper;
        this.adminUserMapper = adminUserMapper;
        this.adminRoleMapper = adminRoleMapper;
        this.adminMenuMapper = adminMenuMapper;
        this.adminUserRoleMapper = adminUserRoleMapper;
        this.adminRoleMenuMapper = adminRoleMenuMapper;
        this.categoryMapper = categoryMapper;
        this.productMapper = productMapper;
        this.customerOrderMapper = customerOrderMapper;
        this.orderItemMapper = orderItemMapper;
        this.passwordUtil = passwordUtil;
    }

    public List<AppUser> listUsers() {
        return appUserMapper.selectList(new LambdaQueryWrapper<AppUser>().orderByDesc(AppUser::getId));
    }

    public List<Map<String, Object>> listAdminUsers() {
        Map<Long, AdminRole> roleMap = adminRoleMapper.selectList(null).stream()
                .collect(Collectors.toMap(AdminRole::getId, role -> role));
        Map<Long, List<Long>> adminRoleMap = adminUserRoleMapper.selectList(null).stream()
                .collect(Collectors.groupingBy(AdminUserRole::getAdminUserId,
                        Collectors.mapping(AdminUserRole::getRoleId, Collectors.toList())));
        return adminUserMapper.selectList(new LambdaQueryWrapper<AdminUser>().orderByDesc(AdminUser::getId))
                .stream()
                .map(admin -> {
                    List<Long> roleIds = adminRoleMap.getOrDefault(admin.getId(), new ArrayList<>());
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", admin.getId());
                    item.put("username", admin.getUsername());
                    item.put("nickname", admin.getNickname());
                    item.put("status", admin.getStatus());
                    item.put("roleIds", roleIds);
                    item.put("roleNames", roleIds.stream().map(roleMap::get).filter(Objects::nonNull).map(AdminRole::getName).toList());
                    item.put("createTime", admin.getCreateTime());
                    return item;
                }).toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveAdminUser(AdminUserRequest request) {
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new BizException("管理员账号不能为空");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new BizException("管理员密码不能为空");
        }
        AdminUser existing = adminUserMapper.selectOne(new LambdaQueryWrapper<AdminUser>()
                .eq(AdminUser::getUsername, request.getUsername()));
        if (existing != null) {
            throw new BizException("管理员账号已存在");
        }
        AdminUser adminUser = new AdminUser();
        adminUser.setUsername(request.getUsername());
        adminUser.setPassword(passwordUtil.encode(request.getPassword()));
        adminUser.setNickname(request.getNickname());
        adminUser.setStatus(request.getStatus());
        adminUser.setCreateTime(LocalDateTime.now());
        adminUser.setUpdateTime(LocalDateTime.now());
        adminUserMapper.insert(adminUser);
        saveAdminRoles(adminUser.getId(), request.getRoleIds());
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateAdminUser(Long id, AdminUserRequest request) {
        AdminUser adminUser = adminUserMapper.selectById(id);
        if (adminUser == null) {
            throw new BizException("管理员不存在");
        }
        adminUser.setNickname(request.getNickname());
        adminUser.setStatus(request.getStatus());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            adminUser.setPassword(passwordUtil.encode(request.getPassword()));
        }
        adminUser.setUpdateTime(LocalDateTime.now());
        adminUserMapper.updateById(adminUser);
        saveAdminRoles(id, request.getRoleIds());
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteAdminUser(Long id) {
        adminUserMapper.deleteById(id);
        adminUserRoleMapper.delete(new LambdaQueryWrapper<AdminUserRole>().eq(AdminUserRole::getAdminUserId, id));
    }

    public List<Map<String, Object>> listRoles() {
        Map<Long, List<Long>> roleMenuMap = adminRoleMenuMapper.selectList(null).stream()
                .collect(Collectors.groupingBy(AdminRoleMenu::getRoleId, Collectors.mapping(AdminRoleMenu::getMenuId, Collectors.toList())));
        return adminRoleMapper.selectList(new LambdaQueryWrapper<AdminRole>().orderByDesc(AdminRole::getId))
                .stream()
                .map(role -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", role.getId());
                    item.put("name", role.getName());
                    item.put("code", role.getCode());
                    item.put("description", role.getDescription());
                    item.put("status", role.getStatus());
                    item.put("menuIds", roleMenuMap.getOrDefault(role.getId(), new ArrayList<>()));
                    return item;
                }).toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveRole(RoleRequest request) {
        AdminRole role = new AdminRole();
        role.setName(request.getName());
        role.setCode(request.getCode());
        role.setDescription(request.getDescription());
        role.setStatus(request.getStatus());
        role.setCreateTime(LocalDateTime.now());
        role.setUpdateTime(LocalDateTime.now());
        adminRoleMapper.insert(role);
        saveRoleMenus(role.getId(), request.getMenuIds());
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateRole(Long id, RoleRequest request) {
        AdminRole role = adminRoleMapper.selectById(id);
        if (role == null) {
            throw new BizException("角色不存在");
        }
        role.setName(request.getName());
        role.setCode(request.getCode());
        role.setDescription(request.getDescription());
        role.setStatus(request.getStatus());
        role.setUpdateTime(LocalDateTime.now());
        adminRoleMapper.updateById(role);
        saveRoleMenus(id, request.getMenuIds());
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long id) {
        adminRoleMapper.deleteById(id);
        adminRoleMenuMapper.delete(new LambdaQueryWrapper<AdminRoleMenu>().eq(AdminRoleMenu::getRoleId, id));
        adminUserRoleMapper.delete(new LambdaQueryWrapper<AdminUserRole>().eq(AdminUserRole::getRoleId, id));
    }

    public List<AdminMenu> listMenus() {
        return adminMenuMapper.selectList(new LambdaQueryWrapper<AdminMenu>()
                .orderByAsc(AdminMenu::getSortNum, AdminMenu::getId));
    }

    public void saveMenu(MenuRequest request) {
        AdminMenu menu = new AdminMenu();
        fillMenu(menu, request);
        menu.setCreateTime(LocalDateTime.now());
        menu.setUpdateTime(LocalDateTime.now());
        adminMenuMapper.insert(menu);
    }

    public void updateMenu(Long id, MenuRequest request) {
        AdminMenu menu = adminMenuMapper.selectById(id);
        if (menu == null) {
            throw new BizException("菜单不存在");
        }
        fillMenu(menu, request);
        menu.setUpdateTime(LocalDateTime.now());
        adminMenuMapper.updateById(menu);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteMenu(Long id) {
        adminMenuMapper.deleteById(id);
        adminRoleMenuMapper.delete(new LambdaQueryWrapper<AdminRoleMenu>().eq(AdminRoleMenu::getMenuId, id));
    }

    public List<Category> listCategories() {
        return categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                .orderByAsc(Category::getSortNum, Category::getId));
    }

    public void saveCategory(CategoryRequest request) {
        Category category = new Category();
        category.setParentId(request.getParentId());
        category.setName(request.getName());
        category.setImage(request.getImage());
        category.setSortNum(request.getSortNum());
        category.setStatus(request.getStatus());
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());
        categoryMapper.insert(category);
    }

    public void updateCategory(Long id, CategoryRequest request) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BizException("分类不存在");
        }
        category.setParentId(request.getParentId());
        category.setName(request.getName());
        category.setImage(request.getImage());
        category.setSortNum(request.getSortNum());
        category.setStatus(request.getStatus());
        category.setUpdateTime(LocalDateTime.now());
        categoryMapper.updateById(category);
    }

    public void deleteCategory(Long id) {
        categoryMapper.deleteById(id);
    }

    public List<Map<String, Object>> listProducts() {
        Map<Long, String> categoryMap = categoryMapper.selectList(null).stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));
        return productMapper.selectList(new LambdaQueryWrapper<Product>().orderByDesc(Product::getId))
                .stream()
                .map(product -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", product.getId());
                    item.put("name", product.getName());
                    item.put("subTitle", product.getSubTitle());
                    item.put("image", product.getImage());
                    item.put("description", product.getDescription());
                    item.put("spec", product.getSpec());
                    item.put("price", product.getPrice());
                    item.put("stock", product.getStock());
                    item.put("status", product.getStatus());
                    item.put("categoryId", product.getCategoryId());
                    item.put("categoryName", categoryMap.get(product.getCategoryId()));
                    return item;
                }).toList();
    }

    public void saveProduct(ProductRequest request) {
        Product product = new Product();
        fillProduct(product, request);
        product.setCreateTime(LocalDateTime.now());
        product.setUpdateTime(LocalDateTime.now());
        productMapper.insert(product);
    }

    public void updateProduct(Long id, ProductRequest request) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BizException("商品不存在");
        }
        fillProduct(product, request);
        product.setUpdateTime(LocalDateTime.now());
        productMapper.updateById(product);
    }

    public void deleteProduct(Long id) {
        productMapper.deleteById(id);
    }

    public List<Map<String, Object>> listOrders() {
        return customerOrderMapper.selectList(new LambdaQueryWrapper<CustomerOrder>().orderByDesc(CustomerOrder::getId))
                .stream()
                .map(order -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", order.getId());
                    item.put("orderNo", order.getOrderNo());
                    item.put("userId", order.getUserId());
                    item.put("status", order.getStatus());
                    item.put("payType", order.getPayType());
                    item.put("totalAmount", order.getTotalAmount());
                    item.put("contactName", order.getContactName());
                    item.put("contactPhone", order.getContactPhone());
                    item.put("fullAddress", order.getFullAddress());
                    item.put("remark", order.getRemark());
                    item.put("createTime", order.getCreateTime());
                    item.put("items", orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId())));
                    return item;
                }).toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateOrderStatus(Long id, String status) {
        CustomerOrder order = customerOrderMapper.selectById(id);
        if (order == null) {
            throw new BizException("订单不存在");
        }
        List<String> allowed = List.of("PENDING_CONFIRM", "TO_DELIVER", "COMPLETED", "CANCELLED");
        if (!allowed.contains(status)) {
            throw new BizException("订单状态不正确");
        }
        order.setStatus(status);
        order.setUpdateTime(LocalDateTime.now());
        customerOrderMapper.updateById(order);
    }

    private void fillProduct(Product product, ProductRequest request) {
        product.setCategoryId(request.getCategoryId());
        product.setName(request.getName());
        product.setSubTitle(request.getSubTitle());
        product.setImage(request.getImage());
        product.setDescription(request.getDescription());
        product.setSpec(request.getSpec());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setStatus(request.getStatus());
    }

    private void fillMenu(AdminMenu menu, MenuRequest request) {
        menu.setParentId(request.getParentId());
        menu.setName(request.getName());
        menu.setPath(request.getPath());
        menu.setComponent(request.getComponent());
        menu.setPermissionCode(request.getPermissionCode());
        menu.setType(request.getType());
        menu.setSortNum(request.getSortNum());
        menu.setStatus(request.getStatus());
    }

    private void saveAdminRoles(Long adminUserId, List<Long> roleIds) {
        adminUserRoleMapper.delete(new LambdaQueryWrapper<AdminUserRole>().eq(AdminUserRole::getAdminUserId, adminUserId));
        for (Long roleId : roleIds) {
            AdminUserRole relation = new AdminUserRole();
            relation.setAdminUserId(adminUserId);
            relation.setRoleId(roleId);
            adminUserRoleMapper.insert(relation);
        }
    }

    private void saveRoleMenus(Long roleId, List<Long> menuIds) {
        adminRoleMenuMapper.delete(new LambdaQueryWrapper<AdminRoleMenu>().eq(AdminRoleMenu::getRoleId, roleId));
        for (Long menuId : menuIds) {
            AdminRoleMenu relation = new AdminRoleMenu();
            relation.setRoleId(roleId);
            relation.setMenuId(menuId);
            adminRoleMenuMapper.insert(relation);
        }
    }
}
