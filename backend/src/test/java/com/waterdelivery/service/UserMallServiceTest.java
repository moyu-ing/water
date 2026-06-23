package com.waterdelivery.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.waterdelivery.common.BizException;
import com.waterdelivery.dto.CartRequest;
import com.waterdelivery.dto.OrderCreateRequest;
import com.waterdelivery.dto.UserAddressRequest;
import com.waterdelivery.entity.*;
import com.waterdelivery.mapper.*;
import org.junit.jupiter.api.BeforeEach;
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
 * 商品浏览、购物车、下单结算模块测试 — 覆盖 TC-PROD / TC-CART / TC-ORDER
 */
@ExtendWith(MockitoExtension.class)
public class UserMallServiceTest {

    @Mock private CategoryMapper categoryMapper;
    @Mock private ProductMapper productMapper;
    @Mock private UserAddressMapper userAddressMapper;
    @Mock private CartItemMapper cartItemMapper;
    @Mock private CustomerOrderMapper customerOrderMapper;
    @Mock private OrderItemMapper orderItemMapper;
    @Mock private AppUserMapper appUserMapper;
    @Mock private CouponService couponService;
    @Mock private PointsService pointsService;

    @InjectMocks
    private UserMallService userMallService;

    // ==================== 商品浏览 ====================

    // TC-PROD-01: 获取活跃分类列表
    @Test
    void testListActiveCategories() {
        Category cat1 = new Category();
        cat1.setId(1L);
        cat1.setName("桶装水");
        cat1.setStatus(1);
        cat1.setSortNum(1);

        Category cat2 = new Category();
        cat2.setId(2L);
        cat2.setName("瓶装水");
        cat2.setStatus(1);
        cat2.setSortNum(2);

        when(categoryMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(cat1, cat2));

        List<Category> result = userMallService.listActiveCategories();
        assertEquals(2, result.size());
        assertEquals("桶装水", result.get(0).getName());
        assertEquals("瓶装水", result.get(1).getName());
    }

    // TC-PROD-02: 按分类获取商品列表
    @Test
    void testListProductsByCategory() {
        Product p1 = new Product();
        p1.setId(1L);
        p1.setName("农夫山泉");
        p1.setPrice(new BigDecimal("10.50"));
        p1.setStock(100);
        p1.setCategoryId(1L);
        p1.setStatus(1);

        Category cat = new Category();
        cat.setId(1L);
        cat.setName("桶装水");

        when(productMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(p1));
        when(categoryMapper.selectList(isNull())).thenReturn(List.of(cat));

        List<Map<String, Object>> result = userMallService.listProducts(1L, null);
        assertEquals(1, result.size());
        assertEquals("农夫山泉", result.get(0).get("name"));
        assertEquals("桶装水", result.get(0).get("categoryName"));
    }

    // TC-PROD-03: 按关键词搜索商品
    @Test
    void testListProductsByKeyword() {
        Product p1 = new Product();
        p1.setId(1L);
        p1.setName("农夫山泉");
        p1.setPrice(new BigDecimal("10.50"));
        p1.setStock(100);
        p1.setCategoryId(1L);
        p1.setStatus(1);

        when(productMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(p1));
        when(categoryMapper.selectList(isNull())).thenReturn(Collections.emptyList());

        List<Map<String, Object>> result = userMallService.listProducts(null, "农夫");
        assertEquals(1, result.size());
        assertEquals("农夫山泉", result.get(0).get("name"));
    }

    // TC-PROD-04: 获取商品详情
    @Test
    void testGetProductDetail() {
        Product p = new Product();
        p.setId(1L);
        p.setName("怡宝桶装水");
        p.setSubTitle("18.9L");
        p.setImage("/uploads/pic.jpg");
        p.setDescription("高品质桶装水");
        p.setSpec("18.9L/桶");
        p.setPrice(new BigDecimal("20.00"));
        p.setStock(50);
        p.setBarrelDeposit(new BigDecimal("30.00"));
        p.setIsBarrel(1);
        p.setStatus(1);
        p.setCategoryId(1L);

        Category cat = new Category();
        cat.setId(1L);
        cat.setName("桶装水");

        when(productMapper.selectById(1L)).thenReturn(p);
        when(categoryMapper.selectById(1L)).thenReturn(cat);

        Map<String, Object> detail = userMallService.getProductDetail(1L);
        assertEquals("怡宝桶装水", detail.get("name"));
        assertEquals("桶装水", detail.get("categoryName"));
        assertEquals(new BigDecimal("30.00"), detail.get("barrelDeposit"));
        assertEquals(1, detail.get("isBarrel"));
    }

    // TC-PROD-05: 获取不存在商品详情
    @Test
    void testGetProductDetailNotFound() {
        when(productMapper.selectById(999L)).thenReturn(null);
        BizException ex = assertThrows(BizException.class, () -> userMallService.getProductDetail(999L));
        assertEquals("商品不存在或已下架", ex.getMessage());
    }

    // TC-PROD-06: 获取已下架商品详情
    @Test
    void testGetProductDetailOffline() {
        Product p = new Product();
        p.setId(1L);
        p.setStatus(0); // 已下架

        when(productMapper.selectById(1L)).thenReturn(p);
        BizException ex = assertThrows(BizException.class, () -> userMallService.getProductDetail(1L));
        assertEquals("商品不存在或已下架", ex.getMessage());
    }

    // ==================== 购物车 ====================

    // TC-CART-01: 添加商品到购物车（新增）
    @Test
    void testAddCartNew() {
        Product p = new Product();
        p.setId(1L);
        p.setName("农夫山泉");
        p.setStatus(1);
        p.setStock(100);

        CartRequest request = new CartRequest();
        request.setProductId(1L);
        request.setQuantity(2);
        request.setChecked(1);

        when(productMapper.selectById(1L)).thenReturn(p);
        when(cartItemMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(cartItemMapper.insert(any(CartItem.class))).thenReturn(1);

        userMallService.addCart(1L, request);

        ArgumentCaptor<CartItem> captor = ArgumentCaptor.forClass(CartItem.class);
        verify(cartItemMapper).insert(captor.capture());
        CartItem saved = captor.getValue();
        assertEquals(1L, saved.getUserId());
        assertEquals(1L, saved.getProductId());
        assertEquals(2, saved.getQuantity());
        assertEquals(1, saved.getChecked());
    }

    // TC-CART-02: 添加已存在商品到购物车（累加数量）
    @Test
    void testAddCartExisting() {
        Product p = new Product();
        p.setId(1L);
        p.setStatus(1);
        p.setStock(100);

        CartItem existing = new CartItem();
        existing.setId(10L);
        existing.setUserId(1L);
        existing.setProductId(1L);
        existing.setQuantity(3);

        CartRequest request = new CartRequest();
        request.setProductId(1L);
        request.setQuantity(2);
        request.setChecked(1);

        when(productMapper.selectById(1L)).thenReturn(p);
        when(cartItemMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existing);
        when(cartItemMapper.updateById(any(CartItem.class))).thenReturn(1);

        userMallService.addCart(1L, request);

        assertEquals(5, existing.getQuantity()); // 3 + 2
        verify(cartItemMapper, never()).insert(any());
        verify(cartItemMapper).updateById(existing);
    }

    // TC-CART-03: 更新购物车
    @Test
    void testUpdateCart() {
        CartItem item = new CartItem();
        item.setId(10L);
        item.setUserId(1L);
        item.setProductId(1L);
        item.setQuantity(2);

        Product p = new Product();
        p.setId(2L);
        p.setStatus(1);

        CartRequest request = new CartRequest();
        request.setProductId(2L);
        request.setQuantity(5);
        request.setChecked(0);

        when(cartItemMapper.selectById(10L)).thenReturn(item);
        when(productMapper.selectById(2L)).thenReturn(p);
        when(cartItemMapper.updateById(any(CartItem.class))).thenReturn(1);

        userMallService.updateCart(1L, 10L, request);

        assertEquals(2L, item.getProductId());
        assertEquals(5, item.getQuantity());
        assertEquals(0, item.getChecked());
    }

    // TC-CART-04: 更新购物车 — 记录不存在
    @Test
    void testUpdateCartNotBelong() {
        CartItem item = new CartItem();
        item.setId(10L);
        item.setUserId(2L); // 属于另一用户

        when(cartItemMapper.selectById(10L)).thenReturn(item);

        CartRequest request = new CartRequest();
        request.setProductId(1L);
        request.setQuantity(1);

        BizException ex = assertThrows(BizException.class, () -> userMallService.updateCart(1L, 10L, request));
        assertEquals("购物车记录不存在", ex.getMessage());
    }

    // TC-CART-05: 删除购物车
    @Test
    void testDeleteCart() {
        CartItem item = new CartItem();
        item.setId(10L);
        item.setUserId(1L);

        when(cartItemMapper.selectById(10L)).thenReturn(item);
        when(cartItemMapper.deleteById(10L)).thenReturn(1);

        userMallService.deleteCart(1L, 10L);
        verify(cartItemMapper).deleteById(10L);
    }

    // TC-CART-06: 删除购物车 — 记录不存在
    @Test
    void testDeleteCartNotBelong() {
        when(cartItemMapper.selectById(10L)).thenReturn(null);

        BizException ex = assertThrows(BizException.class, () -> userMallService.deleteCart(1L, 10L));
        assertEquals("购物车记录不存在", ex.getMessage());
    }

    // ==================== 收货地址 ====================

    @Test
    void testListAddresses() {
        UserAddress addr = new UserAddress();
        addr.setId(1L);
        addr.setUserId(1L);
        addr.setContactName("张三");
        addr.setIsDefault(1);

        when(userAddressMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(addr));

        List<UserAddress> result = userMallService.listAddresses(1L);
        assertEquals(1, result.size());
        assertEquals("张三", result.get(0).getContactName());
    }

    @Test
    void testAddAddress() {
        UserAddressRequest request = new UserAddressRequest();
        request.setContactName("张三");
        request.setContactPhone("13800138000");
        request.setProvince("广东省");
        request.setCity("深圳市");
        request.setDistrict("南山区");
        request.setDetailAddress("科技园路1号");
        request.setIsDefault(1);

        when(userAddressMapper.insert(any(UserAddress.class))).thenReturn(1);

        userMallService.addAddress(1L, request);

        ArgumentCaptor<UserAddress> captor = ArgumentCaptor.forClass(UserAddress.class);
        verify(userAddressMapper).insert(captor.capture());
        UserAddress saved = captor.getValue();
        assertEquals("张三", saved.getContactName());
        assertEquals("13800138000", saved.getContactPhone());
        assertEquals("科技园路1号", saved.getDetailAddress());
        assertEquals(1, saved.getIsDefault());
    }

    // ==================== 下单结算 ====================

    // TC-ORDER-01: 下单成功（基础）
    @Test
    void testCreateOrderBasic() {
        Long userId = 1L;

        UserAddress address = new UserAddress();
        address.setId(1L);
        address.setUserId(userId);
        address.setContactName("张三");
        address.setContactPhone("13800138000");
        address.setProvince("广东省");
        address.setCity("深圳市");
        address.setDistrict("南山区");
        address.setDetailAddress("科技园路1号");

        CartItem cartItem = new CartItem();
        cartItem.setId(1L);
        cartItem.setUserId(userId);
        cartItem.setProductId(1L);
        cartItem.setQuantity(2);
        cartItem.setChecked(1);

        Product product = new Product();
        product.setId(1L);
        product.setName("农夫山泉");
        product.setPrice(new BigDecimal("10.00"));
        product.setStock(100);
        product.setStatus(1);

        AppUser appUser = new AppUser();
        appUser.setId(userId);
        appUser.setBarrelCount(0);

        when(userAddressMapper.selectById(1L)).thenReturn(address);
        when(cartItemMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(cartItem));
        when(productMapper.selectBatchIds(anyList())).thenReturn(List.of(product));
        // 使用 doAnswer 设置 order ID，后续 selectById(orderId) 才能匹配
        doAnswer(inv -> {
            CustomerOrder o = inv.getArgument(0);
            o.setId(1L);
            return 1;
        }).when(customerOrderMapper).insert(any(CustomerOrder.class));
        when(orderItemMapper.insert(any(OrderItem.class))).thenReturn(1);
        when(cartItemMapper.deleteById(anyLong())).thenReturn(1);
        when(productMapper.updateById(any(Product.class))).thenReturn(1);
        when(customerOrderMapper.selectById(1L)).thenAnswer(inv -> {
            CustomerOrder o = new CustomerOrder();
            o.setId(1L);
            o.setUserId(userId);
            o.setOrderNo("WD202606221200001");
            o.setStatus("PENDING_CONFIRM");
            o.setTotalAmount(new BigDecimal("20.00"));
            return o;
        });
        when(orderItemMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        OrderCreateRequest request = new OrderCreateRequest();
        request.setAddressId(1L);

        Map<String, Object> result = userMallService.createOrder(userId, request);

        assertNotNull(result);
        assertEquals("PENDING_CONFIRM", result.get("status"));
        verify(customerOrderMapper).insert(any(CustomerOrder.class));

        // 验证库存扣减
        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productMapper).updateById(productCaptor.capture());
        assertEquals(98, productCaptor.getValue().getStock()); // 100 - 2
    }

    // TC-ORDER-02: 下单 — 地址不存在
    @Test
    void testCreateOrderAddressNotFound() {
        when(userAddressMapper.selectById(999L)).thenReturn(null);

        OrderCreateRequest request = new OrderCreateRequest();
        request.setAddressId(999L);

        BizException ex = assertThrows(BizException.class, () -> userMallService.createOrder(1L, request));
        assertEquals("收货地址不存在", ex.getMessage());
    }

    // TC-ORDER-03: 下单 — 无选中商品
    @Test
    void testCreateOrderNoCheckedItems() {
        UserAddress address = new UserAddress();
        address.setId(1L);
        address.setUserId(1L);

        when(userAddressMapper.selectById(1L)).thenReturn(address);
        when(cartItemMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        OrderCreateRequest request = new OrderCreateRequest();
        request.setAddressId(1L);

        BizException ex = assertThrows(BizException.class, () -> userMallService.createOrder(1L, request));
        assertEquals("请选择购物车商品", ex.getMessage());
    }

    // TC-ORDER-04: 下单 — 库存不足
    @Test
    void testCreateOrderStockInsufficient() {
        Long userId = 1L;

        UserAddress address = new UserAddress();
        address.setId(1L);
        address.setUserId(userId);

        CartItem cartItem = new CartItem();
        cartItem.setId(1L);
        cartItem.setUserId(userId);
        cartItem.setProductId(1L);
        cartItem.setQuantity(10);
        cartItem.setChecked(1);

        Product product = new Product();
        product.setId(1L);
        product.setName("农夫山泉");
        product.setPrice(new BigDecimal("10.00"));
        product.setStock(5); // 库存只有5
        product.setStatus(1);

        when(userAddressMapper.selectById(1L)).thenReturn(address);
        when(cartItemMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(cartItem));
        when(productMapper.selectBatchIds(anyList())).thenReturn(List.of(product));

        OrderCreateRequest request = new OrderCreateRequest();
        request.setAddressId(1L);

        BizException ex = assertThrows(BizException.class, () -> userMallService.createOrder(userId, request));
        assertEquals("农夫山泉 库存不足", ex.getMessage());
    }

    // TC-ORDER-05: 含桶押金商品下单
    @Test
    void testCreateOrderWithBarrelDeposit() {
        Long userId = 1L;

        UserAddress address = new UserAddress();
        address.setId(1L);
        address.setUserId(userId);
        address.setContactName("张三");
        address.setContactPhone("13800138000");
        address.setProvince("广东省");
        address.setCity("深圳市");
        address.setDistrict("南山区");
        address.setDetailAddress("科技园路1号");

        CartItem cartItem = new CartItem();
        cartItem.setId(1L);
        cartItem.setUserId(userId);
        cartItem.setProductId(1L);
        cartItem.setQuantity(2);
        cartItem.setChecked(1);

        Product product = new Product();
        product.setId(1L);
        product.setName("桶装水");
        product.setPrice(new BigDecimal("20.00"));
        product.setStock(100);
        product.setStatus(1);
        product.setIsBarrel(1);
        product.setBarrelDeposit(new BigDecimal("30.00"));

        AppUser appUser = new AppUser();
        appUser.setId(userId);
        appUser.setBarrelCount(0);

        when(userAddressMapper.selectById(1L)).thenReturn(address);
        when(cartItemMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(cartItem));
        when(productMapper.selectBatchIds(anyList())).thenReturn(List.of(product));
        doAnswer(inv -> {
            CustomerOrder o = inv.getArgument(0);
            o.setId(1L);
            return 1;
        }).when(customerOrderMapper).insert(any(CustomerOrder.class));
        when(orderItemMapper.insert(any(OrderItem.class))).thenReturn(1);
        when(cartItemMapper.deleteById(anyLong())).thenReturn(1);
        when(productMapper.updateById(any(Product.class))).thenReturn(1);
        when(appUserMapper.selectById(userId)).thenReturn(appUser);
        when(appUserMapper.updateById(any(AppUser.class))).thenReturn(1);
        when(customerOrderMapper.selectById(1L)).thenAnswer(inv -> {
            CustomerOrder o = new CustomerOrder();
            o.setId(1L);
            o.setUserId(userId);
            o.setOrderNo("WD202606221200001");
            o.setStatus("PENDING_CONFIRM");
            o.setTotalAmount(new BigDecimal("100.00"));
            return o;
        });
        when(orderItemMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        OrderCreateRequest request = new OrderCreateRequest();
        request.setAddressId(1L);

        Map<String, Object> result = userMallService.createOrder(userId, request);

        // 验证用户桶持有数增加
        verify(appUserMapper).selectById(userId);
        verify(appUserMapper).updateById(any(AppUser.class));
    }

    // TC-ORDER-06: 使用优惠券下单
    @Test
    void testCreateOrderWithCoupon() {
        Long userId = 1L;

        UserAddress address = new UserAddress();
        address.setId(1L);
        address.setUserId(userId);
        address.setContactName("张三");
        address.setContactPhone("13800138000");
        address.setProvince("广东省");
        address.setCity("深圳市");
        address.setDistrict("南山区");
        address.setDetailAddress("科技园路1号");

        CartItem cartItem = new CartItem();
        cartItem.setId(1L);
        cartItem.setUserId(userId);
        cartItem.setProductId(1L);
        cartItem.setQuantity(1);
        cartItem.setChecked(1);

        Product product = new Product();
        product.setId(1L);
        product.setName("农夫山泉");
        product.setPrice(new BigDecimal("50.00"));
        product.setStock(100);
        product.setStatus(1);

        when(userAddressMapper.selectById(1L)).thenReturn(address);
        when(cartItemMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(cartItem));
        when(productMapper.selectBatchIds(anyList())).thenReturn(List.of(product));
        doAnswer(inv -> {
            CustomerOrder o = inv.getArgument(0);
            o.setId(1L);
            return 1;
        }).when(customerOrderMapper).insert(any(CustomerOrder.class));
        when(orderItemMapper.insert(any(OrderItem.class))).thenReturn(1);
        when(cartItemMapper.deleteById(anyLong())).thenReturn(1);
        when(productMapper.updateById(any(Product.class))).thenReturn(1);
        when(couponService.validateAndUseCoupon(100L, new BigDecimal("50.00"))).thenReturn(new BigDecimal("10.00"));
        when(customerOrderMapper.selectById(1L)).thenAnswer(inv -> {
            CustomerOrder o = new CustomerOrder();
            o.setId(1L);
            o.setUserId(userId);
            o.setOrderNo("WD202606221200001");
            o.setStatus("PENDING_CONFIRM");
            o.setTotalAmount(new BigDecimal("40.00"));
            return o;
        });
        when(orderItemMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        OrderCreateRequest request = new OrderCreateRequest();
        request.setAddressId(1L);
        request.setUserCouponId(100L);

        Map<String, Object> result = userMallService.createOrder(userId, request);

        verify(couponService).validateAndUseCoupon(100L, new BigDecimal("50.00"));
        verify(couponService).markCouponUsed(100L, 1L);
    }

    // TC-ORDER-07: 使用积分下单
    @Test
    void testCreateOrderWithPoints() {
        Long userId = 1L;

        UserAddress address = new UserAddress();
        address.setId(1L);
        address.setUserId(userId);
        address.setContactName("张三");
        address.setContactPhone("13800138000");
        address.setProvince("广东省");
        address.setCity("深圳市");
        address.setDistrict("南山区");
        address.setDetailAddress("科技园路1号");

        CartItem cartItem = new CartItem();
        cartItem.setId(1L);
        cartItem.setUserId(userId);
        cartItem.setProductId(1L);
        cartItem.setQuantity(1);
        cartItem.setChecked(1);

        Product product = new Product();
        product.setId(1L);
        product.setName("农夫山泉");
        product.setPrice(new BigDecimal("30.00"));
        product.setStock(100);
        product.setStatus(1);

        when(userAddressMapper.selectById(1L)).thenReturn(address);
        when(cartItemMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(cartItem));
        when(productMapper.selectBatchIds(anyList())).thenReturn(List.of(product));
        doAnswer(inv -> {
            CustomerOrder o = inv.getArgument(0);
            o.setId(1L);
            return 1;
        }).when(customerOrderMapper).insert(any(CustomerOrder.class));
        when(orderItemMapper.insert(any(OrderItem.class))).thenReturn(1);
        when(cartItemMapper.deleteById(anyLong())).thenReturn(1);
        when(productMapper.updateById(any(Product.class))).thenReturn(1);
        when(pointsService.getBalance(userId)).thenReturn(200);
        when(customerOrderMapper.selectById(1L)).thenAnswer(inv -> {
            CustomerOrder o = new CustomerOrder();
            o.setId(1L);
            o.setUserId(userId);
            o.setOrderNo("WD202606221200001");
            o.setStatus("PENDING_CONFIRM");
            o.setTotalAmount(new BigDecimal("28.00"));
            return o;
        });
        when(orderItemMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        OrderCreateRequest request = new OrderCreateRequest();
        request.setAddressId(1L);
        request.setUsePoints(200); // 200积分 = 2元

        Map<String, Object> result = userMallService.createOrder(userId, request);

        verify(pointsService).deductPoints(userId, 200);
    }

    // TC-ORDER-08: 获取订单列表
    @Test
    void testListOrders() {
        CustomerOrder order = new CustomerOrder();
        order.setId(1L);
        order.setOrderNo("WD202606221200001");
        order.setUserId(1L);
        order.setStatus("PENDING_CONFIRM");
        order.setTotalAmount(new BigDecimal("50.00"));

        when(customerOrderMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(order));
        when(orderItemMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        List<Map<String, Object>> result = userMallService.listOrders(1L);
        assertEquals(1, result.size());
        assertEquals("WD202606221200001", result.get(0).get("orderNo"));
    }

    // TC-ORDER-09: 获取订单详情
    @Test
    void testGetOrderDetail() {
        CustomerOrder order = new CustomerOrder();
        order.setId(1L);
        order.setOrderNo("WD202606221200001");
        order.setUserId(1L);
        order.setStatus("COMPLETED");
        order.setTotalAmount(new BigDecimal("50.00"));

        when(customerOrderMapper.selectById(1L)).thenReturn(order);
        when(orderItemMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        Map<String, Object> result = userMallService.getOrderDetail(1L, 1L);
        assertEquals("COMPLETED", result.get("status"));
    }

    // TC-ORDER-10: 获取订单详情 — 不存在
    @Test
    void testGetOrderDetailNotFound() {
        when(customerOrderMapper.selectById(999L)).thenReturn(null);

        BizException ex = assertThrows(BizException.class, () -> userMallService.getOrderDetail(1L, 999L));
        assertEquals("订单不存在", ex.getMessage());
    }
}
