package com.waterdelivery.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.waterdelivery.common.BizException;
import com.waterdelivery.dto.CartRequest;
import com.waterdelivery.dto.OrderCreateRequest;
import com.waterdelivery.dto.UserAddressRequest;
import com.waterdelivery.entity.*;
import com.waterdelivery.mapper.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserMallService {

    private final CategoryMapper categoryMapper;
    private final ProductMapper productMapper;
    private final UserAddressMapper userAddressMapper;
    private final CartItemMapper cartItemMapper;
    private final CustomerOrderMapper customerOrderMapper;
    private final OrderItemMapper orderItemMapper;

    public UserMallService(CategoryMapper categoryMapper,
                           ProductMapper productMapper,
                           UserAddressMapper userAddressMapper,
                           CartItemMapper cartItemMapper,
                           CustomerOrderMapper customerOrderMapper,
                           OrderItemMapper orderItemMapper) {
        this.categoryMapper = categoryMapper;
        this.productMapper = productMapper;
        this.userAddressMapper = userAddressMapper;
        this.cartItemMapper = cartItemMapper;
        this.customerOrderMapper = customerOrderMapper;
        this.orderItemMapper = orderItemMapper;
    }

    public List<Category> listActiveCategories() {
        return categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                .eq(Category::getStatus, 1)
                .orderByAsc(Category::getSortNum, Category::getId));
    }

    public List<Map<String, Object>> listProducts(Long categoryId, String keyword) {
        List<Product> products = productMapper.selectList(new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, 1)
                .eq(categoryId != null, Product::getCategoryId, categoryId)
                .like(keyword != null && !keyword.isBlank(), Product::getName, keyword)
                .orderByDesc(Product::getId));
        Map<Long, String> categoryMap = categoryMapper.selectList(null).stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));
        return products.stream().map(product -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", product.getId());
            item.put("name", product.getName());
            item.put("subTitle", product.getSubTitle());
            item.put("image", product.getImage());
            item.put("spec", product.getSpec());
            item.put("price", product.getPrice());
            item.put("stock", product.getStock());
            item.put("categoryId", product.getCategoryId());
            item.put("categoryName", categoryMap.get(product.getCategoryId()));
            return item;
        }).toList();
    }

    public Map<String, Object> getProductDetail(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null || product.getStatus() != 1) {
            throw new BizException("商品不存在或已下架");
        }
        Category category = categoryMapper.selectById(product.getCategoryId());
        Map<String, Object> item = new HashMap<>();
        item.put("id", product.getId());
        item.put("name", product.getName());
        item.put("subTitle", product.getSubTitle());
        item.put("image", product.getImage());
        item.put("description", product.getDescription());
        item.put("spec", product.getSpec());
        item.put("price", product.getPrice());
        item.put("stock", product.getStock());
        item.put("categoryId", product.getCategoryId());
        item.put("categoryName", category == null ? null : category.getName());
        return item;
    }

    public List<UserAddress> listAddresses(Long userId) {
        return userAddressMapper.selectList(new LambdaQueryWrapper<UserAddress>()
                .eq(UserAddress::getUserId, userId)
                .orderByDesc(UserAddress::getIsDefault, UserAddress::getId));
    }

    public void addAddress(Long userId, UserAddressRequest request) {
        if (request.getIsDefault() == 1) {
            clearDefaultAddress(userId);
        }
        UserAddress address = new UserAddress();
        address.setUserId(userId);
        address.setContactName(request.getContactName());
        address.setContactPhone(request.getContactPhone());
        address.setProvince(request.getProvince());
        address.setCity(request.getCity());
        address.setDistrict(request.getDistrict());
        address.setDetailAddress(request.getDetailAddress());
        address.setIsDefault(request.getIsDefault());
        address.setCreateTime(LocalDateTime.now());
        address.setUpdateTime(LocalDateTime.now());
        userAddressMapper.insert(address);
    }

    public void updateAddress(Long userId, Long id, UserAddressRequest request) {
        UserAddress address = userAddressMapper.selectById(id);
        if (address == null || !Objects.equals(address.getUserId(), userId)) {
            throw new BizException("地址不存在");
        }
        if (request.getIsDefault() == 1) {
            clearDefaultAddress(userId);
        }
        address.setContactName(request.getContactName());
        address.setContactPhone(request.getContactPhone());
        address.setProvince(request.getProvince());
        address.setCity(request.getCity());
        address.setDistrict(request.getDistrict());
        address.setDetailAddress(request.getDetailAddress());
        address.setIsDefault(request.getIsDefault());
        address.setUpdateTime(LocalDateTime.now());
        userAddressMapper.updateById(address);
    }

    public void deleteAddress(Long userId, Long id) {
        UserAddress address = userAddressMapper.selectById(id);
        if (address == null || !Objects.equals(address.getUserId(), userId)) {
            throw new BizException("地址不存在");
        }
        userAddressMapper.deleteById(id);
    }

    public List<Map<String, Object>> listCart(Long userId) {
        List<CartItem> items = cartItemMapper.selectList(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .orderByDesc(CartItem::getId));
        if (items.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, Product> productMap = productMapper.selectBatchIds(items.stream().map(CartItem::getProductId).toList())
                .stream().collect(Collectors.toMap(Product::getId, item -> item));
        return items.stream().map(item -> {
            Product product = productMap.get(item.getProductId());
            Map<String, Object> map = new HashMap<>();
            map.put("id", item.getId());
            map.put("productId", item.getProductId());
            map.put("quantity", item.getQuantity());
            map.put("checked", item.getChecked());
            map.put("product", product);
            return map;
        }).toList();
    }

    public void addCart(Long userId, CartRequest request) {
        Product product = productMapper.selectById(request.getProductId());
        if (product == null || product.getStatus() != 1) {
            throw new BizException("商品不存在或已下架");
        }
        CartItem existing = cartItemMapper.selectOne(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .eq(CartItem::getProductId, request.getProductId()));
        if (existing == null) {
            existing = new CartItem();
            existing.setUserId(userId);
            existing.setProductId(request.getProductId());
            existing.setQuantity(request.getQuantity());
            existing.setChecked(request.getChecked());
            existing.setCreateTime(LocalDateTime.now());
            existing.setUpdateTime(LocalDateTime.now());
            cartItemMapper.insert(existing);
            return;
        }
        existing.setQuantity(existing.getQuantity() + request.getQuantity());
        existing.setChecked(request.getChecked());
        existing.setUpdateTime(LocalDateTime.now());
        cartItemMapper.updateById(existing);
    }

    public void updateCart(Long userId, Long id, CartRequest request) {
        CartItem item = cartItemMapper.selectById(id);
        if (item == null || !Objects.equals(item.getUserId(), userId)) {
            throw new BizException("购物车记录不存在");
        }
        Product product = productMapper.selectById(request.getProductId());
        if (product == null || product.getStatus() != 1) {
            throw new BizException("商品不存在或已下架");
        }
        item.setProductId(request.getProductId());
        item.setQuantity(request.getQuantity());
        item.setChecked(request.getChecked());
        item.setUpdateTime(LocalDateTime.now());
        cartItemMapper.updateById(item);
    }

    public void deleteCart(Long userId, Long id) {
        CartItem item = cartItemMapper.selectById(id);
        if (item == null || !Objects.equals(item.getUserId(), userId)) {
            throw new BizException("购物车记录不存在");
        }
        cartItemMapper.deleteById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createOrder(Long userId, OrderCreateRequest request) {
        UserAddress address = userAddressMapper.selectById(request.getAddressId());
        if (address == null || !Objects.equals(address.getUserId(), userId)) {
            throw new BizException("收货地址不存在");
        }
        List<CartItem> checkedItems = cartItemMapper.selectList(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .eq(CartItem::getChecked, 1));
        if (checkedItems.isEmpty()) {
            throw new BizException("请选择购物车商品");
        }
        Map<Long, Product> productMap = productMapper.selectBatchIds(checkedItems.stream().map(CartItem::getProductId).toList())
                .stream().collect(Collectors.toMap(Product::getId, product -> product));
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : checkedItems) {
            Product product = productMap.get(item.getProductId());
            if (product == null || product.getStatus() != 1) {
                throw new BizException("包含失效商品，请刷新购物车");
            }
            if (product.getStock() < item.getQuantity()) {
                throw new BizException(product.getName() + " 库存不足");
            }
            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        CustomerOrder order = new CustomerOrder();
        order.setOrderNo("WD" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + userId);
        order.setUserId(userId);
        order.setTotalAmount(total);
        order.setStatus("PENDING_CONFIRM");
        order.setPayType("COD");
        order.setContactName(address.getContactName());
        order.setContactPhone(address.getContactPhone());
        order.setFullAddress(address.getProvince() + address.getCity() + address.getDistrict() + address.getDetailAddress());
        order.setRemark(request.getRemark());
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        customerOrderMapper.insert(order);
        for (CartItem cartItem : checkedItems) {
            Product product = productMap.get(cartItem.getProductId());
            product.setStock(product.getStock() - cartItem.getQuantity());
            product.setUpdateTime(LocalDateTime.now());
            productMapper.updateById(product);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setProductImage(product.getImage());
            orderItem.setProductSpec(product.getSpec());
            orderItem.setProductPrice(product.getPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setAmount(product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
            orderItem.setCreateTime(LocalDateTime.now());
            orderItem.setUpdateTime(LocalDateTime.now());
            orderItemMapper.insert(orderItem);
            cartItemMapper.deleteById(cartItem.getId());
        }
        return getOrderDetail(userId, order.getId());
    }

    public List<Map<String, Object>> listOrders(Long userId) {
        return customerOrderMapper.selectList(new LambdaQueryWrapper<CustomerOrder>()
                        .eq(CustomerOrder::getUserId, userId)
                        .orderByDesc(CustomerOrder::getId))
                .stream()
                .map(order -> buildOrderMap(order, loadOrderItems(order.getId())))
                .toList();
    }

    public Map<String, Object> getOrderDetail(Long userId, Long id) {
        CustomerOrder order = customerOrderMapper.selectById(id);
        if (order == null || !Objects.equals(order.getUserId(), userId)) {
            throw new BizException("订单不存在");
        }
        return buildOrderMap(order, loadOrderItems(order.getId()));
    }

    private List<OrderItem> loadOrderItems(Long orderId) {
        return orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, orderId));
    }

    private Map<String, Object> buildOrderMap(CustomerOrder order, List<OrderItem> items) {
        Map<String, Object> result = new HashMap<>();
        result.put("id", order.getId());
        result.put("orderNo", order.getOrderNo());
        result.put("status", order.getStatus());
        result.put("payType", order.getPayType());
        result.put("totalAmount", order.getTotalAmount());
        result.put("contactName", order.getContactName());
        result.put("contactPhone", order.getContactPhone());
        result.put("fullAddress", order.getFullAddress());
        result.put("remark", order.getRemark());
        result.put("createTime", order.getCreateTime());
        result.put("items", items);
        return result;
    }

    private void clearDefaultAddress(Long userId) {
        List<UserAddress> addresses = userAddressMapper.selectList(new LambdaQueryWrapper<UserAddress>()
                .eq(UserAddress::getUserId, userId)
                .eq(UserAddress::getIsDefault, 1));
        for (UserAddress item : addresses) {
            item.setIsDefault(0);
            item.setUpdateTime(LocalDateTime.now());
            userAddressMapper.updateById(item);
        }
    }
}
