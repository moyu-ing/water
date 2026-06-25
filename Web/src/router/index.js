import { createRouter, createWebHistory } from "vue-router";
import { ElMessage } from "element-plus";
import { pinia } from "../stores";
import { useAdminStore } from "../stores/admin";
import { useUserStore } from "../stores/user";
import { useDeliveryStore } from "../stores/delivery";

const routes = [
  {
    path: "/",
    component: () => import("../layouts/UserLayout.vue"),
    children: [
      {
        path: "",
        name: "home",
        component: () => import("../views/user/HomePage.vue"),
      },
      {
        path: "category",
        name: "category",
        component: () => import("../views/user/CategoryPage.vue"),
      },
      {
        path: "product/:id",
        name: "product",
        component: () => import("../views/user/ProductDetailPage.vue"),
      },
      {
        path: "cart",
        name: "cart",
        component: () => import("../views/user/CartPage.vue"),
        meta: { requiresUser: true },
      },
      {
        path: "orders",
        name: "orders",
        component: () => import("../views/user/OrdersPage.vue"),
        meta: { requiresUser: true },
      },
      {
        path: "profile",
        name: "profile",
        component: () => import("../views/user/ProfilePage.vue"),
        meta: { requiresUser: true },
      },
    ],
  },
  {
    path: "/login",
    name: "login",
    component: () => import("../views/user/AuthPage.vue"),
  },
  {
    path: "/admin/login",
    name: "admin-login",
    component: () => import("../views/admin/AdminLoginPage.vue"),
  },
  {
    path: "/admin",
    component: () => import("../layouts/AdminLayout.vue"),
    meta: { requiresAdmin: true },
    children: [
      {
        path: "dashboard",
        name: "admin-dashboard",
        component: () => import("../views/admin/DashboardPage.vue"),
        meta: { permission: "admin:dashboard" },
      },
      {
        path: "users",
        name: "admin-users",
        component: () => import("../views/admin/UsersPage.vue"),
        meta: { permission: "user:view" },
      },
      {
        path: "admins",
        name: "admin-admins",
        component: () => import("../views/admin/AdminsPage.vue"),
        meta: { permission: "admin:view" },
      },
      {
        path: "roles",
        name: "admin-roles",
        component: () => import("../views/admin/RolesPage.vue"),
        meta: { permission: "role:view" },
      },
      {
        path: "menus",
        name: "admin-menus",
        component: () => import("../views/admin/MenusPage.vue"),
        meta: { permission: "menu:view" },
      },
      {
        path: "categories",
        name: "admin-categories",
        component: () => import("../views/admin/CategoriesPage.vue"),
        meta: { permission: "category:view" },
      },
      {
        path: "products",
        name: "admin-products",
        component: () => import("../views/admin/ProductsPage.vue"),
        meta: { permission: "product:view" },
      },
      {
        path: "orders",
        name: "admin-orders",
        component: () => import("../views/admin/OrdersPage.vue"),
        meta: { permission: "order:view" },
      },
      {
        path: "delivery-staff",
        name: "admin-delivery-staff",
        component: () => import("../views/admin/DeliveryStaffPage.vue"),
        meta: { permission: "delivery:view" },
      },
      {
        path: "delivery-tasks",
        name: "admin-delivery-tasks",
        component: () => import("../views/admin/DeliveryTasksPage.vue"),
        meta: { permission: "delivery:view" },
      },
      {
        path: "coupons",
        name: "admin-coupons",
        component: () => import("../views/admin/CouponsPage.vue"),
        meta: { permission: "coupon:view" },
      },
      { path: "", redirect: "/admin/dashboard" },
    ],
  },
  {
    path: "/delivery/login",
    name: "delivery-login",
    component: () => import("../views/delivery/DeliveryLoginPage.vue"),
  },
  {
    path: "/delivery",
    component: () => import("../layouts/DeliveryLayout.vue"),
    meta: { requiresDelivery: true },
    children: [
      {
        path: "",
        name: "delivery-tasks",
        component: () => import("../views/delivery/DeliveryTasksPage.vue"),
      },
    ],
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 }),
});

router.beforeEach(async (to) => {
  const userStore = useUserStore(pinia);
  const adminStore = useAdminStore(pinia);
  const deliveryStore = useDeliveryStore(pinia);

  if (to.meta.requiresDelivery) {
    if (deliveryStore.token && !deliveryStore.profile) {
      await deliveryStore.bootstrap();
    }
    if (!deliveryStore.isLoggedIn) {
      return { path: "/delivery/login", query: { redirect: to.fullPath } };
    }
  }

  if (to.meta.requiresUser) {
    if (userStore.token && !userStore.profile) {
      await userStore.bootstrap();
    }
    if (!userStore.isLoggedIn) {
      return { path: "/login", query: { redirect: to.fullPath } };
    }
  }

  if (to.meta.requiresAdmin) {
    if (adminStore.token && !adminStore.profile) {
      await adminStore.bootstrap();
    }
    if (!adminStore.isLoggedIn) {
      return { path: "/admin/login", query: { redirect: to.fullPath } };
    }
    const permission = to.meta.permission;
    if (permission && !adminStore.permissions.includes(permission)) {
      ElMessage.error("当前账号没有页面访问权限");
      return "/admin/dashboard";
    }
  }

  return true;
});

export default router;
