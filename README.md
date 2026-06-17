# 桶装水配送商城

前后端分离的桶装水配送商城示例项目，包含：

* `backend`：Spring Boot 3 + MyBatis-Plus + MySQL
* `web`：Vue 3 + Vite + Pinia + Vue Router + Element Plus 

## 已实现能力

* 用户注册、登录、收货地址管理
* 商品分类、商品列表、商品详情
* 购物车、提交订单、订单查询
* 后台管理员登录
* 后台商品、分类、订单、管理员、角色、菜单管理
* RBAC 菜单/权限控制
* 初始化分类、商品、超级管理员演示数据

## 默认账号

* 后台管理员：`admin`
* 密码：`admin123`

## 启动方式

### 1\. 启动后端

先确认本机 MySQL 已启动，并且 `root / 123456` 可用。如果你的账号密码不同，请修改 [application.yml](/E:/bottled_water_delivery/backend/src/main/resources/application.yml:1)。

```bash
cd backend
mvn spring-boot:run
```

后端默认地址：`http://localhost:8080`

接口文档：

* `http://localhost:8080/doc.html`
* `http://localhost:8080/swagger-ui.html`

### 2\. 启动前端

```bash
cd web
npm install
npm run dev
```

前端默认地址：`http://localhost:5173`

## 初始化数据

项目启动后会自动执行：

* [schema.sql](/E:/bottled_water_delivery/backend/src/main/resources/schema.sql:1)
* [data.sql](/E:/bottled_water_delivery/backend/src/main/resources/data.sql:1)

已内置 5 个分类和 10 个商品，前台可直接浏览并下单。

