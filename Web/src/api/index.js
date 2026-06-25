import { request } from './http'
import axios from 'axios'

const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

export function uploadFile(file) {
  const formData = new FormData()
  formData.append('file', file)
  const token = localStorage.getItem('admin_token')
  return axios.post(`${baseURL}/api/admin/upload`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    timeout: 30000,
  }).then(res => {
    if (res.data?.code !== 200) {
      throw new Error(res.data?.message || '上传失败')
    }
    return res.data.data
  })
}

export const publicApi = {
  categories: () => request({ url: '/api/public/categories' }),
  products: (params) => request({ url: '/api/public/products', params }),
  productDetail: (id) => request({ url: `/api/public/products/${id}` }),
}

export const userApi = {
  register: (data) => request({ url: '/api/user/auth/register', method: 'post', data }),
  login: (data) => request({ url: '/api/user/auth/login', method: 'post', data }),
  profile: () => request({ url: '/api/user/auth/me', area: 'user' }),
  addresses: () => request({ url: '/api/user/addresses', area: 'user' }),
  saveAddress: (data) => request({ url: '/api/user/addresses', method: 'post', data, area: 'user' }),
  updateAddress: (id, data) => request({ url: `/api/user/addresses/${id}`, method: 'put', data, area: 'user' }),
  deleteAddress: (id) => request({ url: `/api/user/addresses/${id}`, method: 'delete', area: 'user' }),
  cart: () => request({ url: '/api/user/cart', area: 'user' }),
  addCart: (data) => request({ url: '/api/user/cart', method: 'post', data, area: 'user' }),
  updateCart: (id, data) => request({ url: `/api/user/cart/${id}`, method: 'put', data, area: 'user' }),
  deleteCart: (id) => request({ url: `/api/user/cart/${id}`, method: 'delete', area: 'user' }),
  createOrder: (data) => request({ url: '/api/user/orders', method: 'post', data, area: 'user' }),
  orders: () => request({ url: '/api/user/orders', area: 'user' }),
  orderDetail: (id) => request({ url: `/api/user/orders/${id}`, area: 'user' }),
  // 优惠券
  coupons: () => request({ url: '/api/user/coupons', area: 'user' }),
  availableCoupons: () => request({ url: '/api/user/coupons/available', area: 'user' }),
  receiveCoupon: (templateId) => request({ url: `/api/user/coupons/receive/${templateId}`, method: 'post', area: 'user' }),
  // 积分
  pointsBalance: () => request({ url: '/api/user/points', area: 'user' }),
  pointsHistory: () => request({ url: '/api/user/points/history', area: 'user' }),
}

export const adminApi = {
  login: (data) => request({ url: '/api/admin/auth/login', method: 'post', data }),
  profile: () => request({ url: '/api/admin/auth/me', area: 'admin' }),
  users: () => request({ url: '/api/admin/users', area: 'admin' }),
  adminUsers: () => request({ url: '/api/admin/admin-users', area: 'admin' }),
  saveAdminUser: (data) => request({ url: '/api/admin/admin-users', method: 'post', data, area: 'admin' }),
  updateAdminUser: (id, data) => request({ url: `/api/admin/admin-users/${id}`, method: 'put', data, area: 'admin' }),
  deleteAdminUser: (id) => request({ url: `/api/admin/admin-users/${id}`, method: 'delete', area: 'admin' }),
  roles: () => request({ url: '/api/admin/roles', area: 'admin' }),
  saveRole: (data) => request({ url: '/api/admin/roles', method: 'post', data, area: 'admin' }),
  updateRole: (id, data) => request({ url: `/api/admin/roles/${id}`, method: 'put', data, area: 'admin' }),
  deleteRole: (id) => request({ url: `/api/admin/roles/${id}`, method: 'delete', area: 'admin' }),
  menus: () => request({ url: '/api/admin/menus', area: 'admin' }),
  saveMenu: (data) => request({ url: '/api/admin/menus', method: 'post', data, area: 'admin' }),
  updateMenu: (id, data) => request({ url: `/api/admin/menus/${id}`, method: 'put', data, area: 'admin' }),
  deleteMenu: (id) => request({ url: `/api/admin/menus/${id}`, method: 'delete', area: 'admin' }),
  categories: () => request({ url: '/api/admin/categories', area: 'admin' }),
  saveCategory: (data) => request({ url: '/api/admin/categories', method: 'post', data, area: 'admin' }),
  updateCategory: (id, data) => request({ url: `/api/admin/categories/${id}`, method: 'put', data, area: 'admin' }),
  deleteCategory: (id) => request({ url: `/api/admin/categories/${id}`, method: 'delete', area: 'admin' }),
  products: () => request({ url: '/api/admin/products', area: 'admin' }),
  saveProduct: (data) => request({ url: '/api/admin/products', method: 'post', data, area: 'admin' }),
  updateProduct: (id, data) => request({ url: `/api/admin/products/${id}`, method: 'put', data, area: 'admin' }),
  deleteProduct: (id) => request({ url: `/api/admin/products/${id}`, method: 'delete', area: 'admin' }),
  orders: () => request({ url: '/api/admin/orders', area: 'admin' }),
  updateOrderStatus: (id, data) => request({ url: `/api/admin/orders/${id}/status`, method: 'put', data, area: 'admin' }),
  statistics: () => request({ url: '/api/admin/statistics', area: 'admin' }),
  // 配送管理
  deliveryStaff: () => request({ url: '/api/admin/delivery/staff', area: 'admin' }),
  saveDeliveryStaff: (data) => request({ url: '/api/admin/delivery/staff', method: 'post', data, area: 'admin' }),
  updateDeliveryStaff: (id, data) => request({ url: `/api/admin/delivery/staff/${id}`, method: 'put', data, area: 'admin' }),
  deleteDeliveryStaff: (id) => request({ url: `/api/admin/delivery/staff/${id}`, method: 'delete', area: 'admin' }),
  deliveryTasks: () => request({ url: '/api/admin/delivery/tasks', area: 'admin' }),
  assignDeliveryTask: (data) => request({ url: '/api/admin/delivery/tasks/assign', method: 'post', data, area: 'admin' }),
  // 优惠券管理
  coupons: () => request({ url: '/api/admin/coupons', area: 'admin' }),
  saveCoupon: (data) => request({ url: '/api/admin/coupons', method: 'post', data, area: 'admin' }),
  updateCoupon: (id, data) => request({ url: `/api/admin/coupons/${id}`, method: 'put', data, area: 'admin' }),
  deleteCoupon: (id) => request({ url: `/api/admin/coupons/${id}`, method: 'delete', area: 'admin' }),
  distributeCoupon: (id, data) => request({ url: `/api/admin/coupons/${id}/distribute`, method: 'post', data, area: 'admin' }),
}

export const deliveryApi = {
  login: (data) => request({ url: '/api/delivery/login', method: 'post', data }),
  profile: () => request({ url: '/api/delivery/me', area: 'delivery' }),
  tasks: () => request({ url: '/api/delivery/tasks', area: 'delivery' }),
  updateTaskStatus: (id, data) => request({ url: `/api/delivery/tasks/${id}/status`, method: 'put', data, area: 'delivery' }),
  completeTask: (id, data) => request({ url: `/api/delivery/tasks/${id}/complete`, method: 'put', data, area: 'delivery' }),
}
