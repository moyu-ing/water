import { request } from './http'

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
}
