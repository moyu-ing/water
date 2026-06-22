INSERT IGNORE INTO admin_role (id, name, code, description, status, create_time, update_time) VALUES
(1, '超级管理员', 'SUPER_ADMIN', '拥有系统全部管理权限', 1, NOW(), NOW());

INSERT INTO admin_user (id, username, password, nickname, status, create_time, update_time) VALUES
(1, 'admin', '$2a$10$3zHWRm5NT/MhETCXlOC3wu.zoXb0axnJ9/QWZ4e8s/5dHjbSz/4kq', '系统管理员', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE
password = VALUES(password),
nickname = VALUES(nickname),
status = VALUES(status),
update_time = NOW();

INSERT IGNORE INTO admin_user_role (id, admin_user_id, role_id) VALUES
(1, 1, 1);

INSERT IGNORE INTO admin_menu (id, parent_id, name, path, component, permission_code, type, sort_num, status, create_time, update_time) VALUES
(1, 0, '后台首页', '/admin/dashboard', 'admin/DashboardPage', 'admin:dashboard', 1, 1, 1, NOW(), NOW()),
(2, 0, '用户管理', '/admin/users', 'admin/UsersPage', 'user:view', 1, 2, 1, NOW(), NOW()),
(3, 0, '管理员管理', '/admin/admins', 'admin/AdminsPage', 'admin:view', 1, 3, 1, NOW(), NOW()),
(4, 0, '角色管理', '/admin/roles', 'admin/RolesPage', 'role:view', 1, 4, 1, NOW(), NOW()),
(5, 0, '菜单管理', '/admin/menus', 'admin/MenusPage', 'menu:view', 1, 5, 1, NOW(), NOW()),
(6, 0, '分类管理', '/admin/categories', 'admin/CategoriesPage', 'category:view', 1, 6, 1, NOW(), NOW()),
(7, 0, '商品管理', '/admin/products', 'admin/ProductsPage', 'product:view', 1, 7, 1, NOW(), NOW()),
(8, 0, '订单管理', '/admin/orders', 'admin/OrdersPage', 'order:view', 1, 8, 1, NOW(), NOW()),
(9, 0, '管理员编辑', '', '', 'admin:edit', 2, 9, 1, NOW(), NOW()),
(10, 0, '角色编辑', '', '', 'role:edit', 2, 10, 1, NOW(), NOW()),
(11, 0, '菜单编辑', '', '', 'menu:edit', 2, 11, 1, NOW(), NOW()),
(12, 0, '分类编辑', '', '', 'category:edit', 2, 12, 1, NOW(), NOW()),
(13, 0, '商品编辑', '', '', 'product:edit', 2, 13, 1, NOW(), NOW()),
(14, 0, '订单编辑', '', '', 'order:edit', 2, 14, 1, NOW(), NOW()),
(15, 0, '配送人员管理', '/admin/delivery-staff', 'admin/DeliveryStaffPage', 'delivery:view', 1, 9, 1, NOW(), NOW()),
(16, 0, '配送任务管理', '/admin/delivery-tasks', 'admin/DeliveryTasksPage', 'delivery:view', 1, 10, 1, NOW(), NOW()),
(17, 0, '配送管理编辑', '', '', 'delivery:edit', 2, 15, 1, NOW(), NOW()),
(18, 0, '优惠券管理', '/admin/coupons', 'admin/CouponsPage', 'coupon:view', 1, 11, 1, NOW(), NOW()),
(19, 0, '优惠券编辑', '', '', 'coupon:edit', 2, 16, 1, NOW(), NOW());

INSERT IGNORE INTO admin_role_menu (id, role_id, menu_id) VALUES
(1, 1, 1),(2, 1, 2),(3, 1, 3),(4, 1, 4),(5, 1, 5),(6, 1, 6),(7, 1, 7),(8, 1, 8),
(9, 1, 9),(10, 1, 10),(11, 1, 11),(12, 1, 12),(13, 1, 13),(14, 1, 14),
(15, 1, 15),(16, 1, 16),(17, 1, 17),(18, 1, 18),(19, 1, 19);

-- 样本配送员（密码 delivery123 的 BCrypt 哈希）
INSERT INTO delivery_staff (id, username, password, name, phone, status, create_time, update_time) VALUES
(1, 'delivery01', '$2a$10$3zHWRm5NT/MhETCXlOC3wu.zoXb0axnJ9/QWZ4e8s/5dHjbSz/4kq', '张师傅', '13800001111', 1, NOW(), NOW()),
(2, 'delivery02', '$2a$10$3zHWRm5NT/MhETCXlOC3wu.zoXb0axnJ9/QWZ4e8s/5dHjbSz/4kq', '李师傅', '13800002222', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE name = VALUES(name), phone = VALUES(phone);

-- 样本优惠券模板
INSERT IGNORE INTO coupon_template (id, name, type, discount_value, min_amount, total_quantity, received_quantity, valid_days, status, create_time, update_time) VALUES
(1, '新用户专享券', 'FULL_REDUCTION', 10.00, 50.00, 100, 0, 30, 1, NOW(), NOW()),
(2, '满100减20', 'FULL_REDUCTION', 20.00, 100.00, 200, 0, 60, 1, NOW(), NOW()),
(3, '9折优惠券', 'DISCOUNT', 10.00, 0, 150, 0, 30, 1, NOW(), NOW());

INSERT IGNORE INTO category (id, parent_id, name, image, sort_num, status, create_time, update_time) VALUES
(1, 0, '天然饮用水', 'https://dummyimage.com/640x420/c7e8ff/0f172a&text=Natural+Water', 1, 1, NOW(), NOW()),
(2, 0, '矿泉水', 'https://dummyimage.com/640x420/b7f0d8/0f172a&text=Mineral+Water', 2, 1, NOW(), NOW()),
(3, 0, '纯净水', 'https://dummyimage.com/640x420/dbeafe/0f172a&text=Pure+Water', 3, 1, NOW(), NOW()),
(4, 0, '一次性包装水', 'https://dummyimage.com/640x420/fef3c7/0f172a&text=Packaged+Water', 4, 1, NOW(), NOW()),
(5, 0, '饮水配件', 'https://dummyimage.com/640x420/fce7f3/0f172a&text=Accessories', 5, 1, NOW(), NOW());

INSERT IGNORE INTO product (id, category_id, name, sub_title, image, description, spec, price, stock, status, create_time, update_time) VALUES
(1, 1, '18.9L天然饮用水', '家庭常备桶装水，口感清爽', 'https://dummyimage.com/640x420/c7e8ff/0f172a&text=18.9L+Natural', '适合家庭和办公室日常饮用，开盖即享自然清甜。', '18.9L/桶', 26.00, 120, 1, NOW(), NOW()),
(2, 1, '15L天然山泉水', '适合中小办公场景', 'https://dummyimage.com/640x420/c7e8ff/0f172a&text=15L+Spring', '轻量桶型，搬运方便，适合小型会议室与家庭使用。', '15L/桶', 22.00, 80, 1, NOW(), NOW()),
(3, 2, '18.9L矿泉水', '富含多种矿物质', 'https://dummyimage.com/640x420/b7f0d8/0f172a&text=18.9L+Mineral', '甄选矿物质配比，适合长期饮用和接待用水。', '18.9L/桶', 30.00, 96, 1, NOW(), NOW()),
(4, 2, '550ml*24矿泉水', '整箱配送，会议活动常用', 'https://dummyimage.com/640x420/b7f0d8/0f172a&text=24+Bottle+Mineral', '整箱小瓶装，适合公司会议、活动和临时补水。', '550ml*24瓶', 36.00, 150, 1, NOW(), NOW()),
(5, 3, '18.9L纯净水', '性价比之选，日常配送', 'https://dummyimage.com/640x420/dbeafe/0f172a&text=18.9L+Pure', '过滤净化工艺，适合学校、宿舍、工地等日常饮水。', '18.9L/桶', 18.00, 160, 1, NOW(), NOW()),
(6, 3, '12L纯净水', '轻桶便携款', 'https://dummyimage.com/640x420/dbeafe/0f172a&text=12L+Pure', '小容量更适合单人租房与小家庭短期饮用。', '12L/桶', 15.00, 90, 1, NOW(), NOW()),
(7, 4, '380ml*24便携饮用水', '出行和活动补水', 'https://dummyimage.com/640x420/fef3c7/0f172a&text=380ml*24', '小瓶便携，适用于车载、户外和活动补给。', '380ml*24瓶', 28.00, 200, 1, NOW(), NOW()),
(8, 4, '5L家庭装饮用水', '厨房备水更省心', 'https://dummyimage.com/640x420/fef3c7/0f172a&text=5L+Home', '提手设计，方便日常烹饪和短期储水。', '5L*4桶', 42.00, 100, 1, NOW(), NOW()),
(9, 5, '手压式压水器', '适配常见桶装水口径', 'https://dummyimage.com/640x420/fce7f3/0f172a&text=Water+Pump', '安装简单，适合家庭与宿舍临时取水使用。', '通用款', 19.90, 180, 1, NOW(), NOW()),
(10, 5, '桶装水支架', '稳固放置，接水更方便', 'https://dummyimage.com/640x420/fce7f3/0f172a&text=Water+Stand', '加厚支撑设计，适配 18.9L 桶装水，稳固耐用。', '加固款', 39.90, 88, 1, NOW(), NOW());
