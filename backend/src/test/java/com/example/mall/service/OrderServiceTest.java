package com.example.mall.service;

import com.example.mall.entity.*;
import com.example.mall.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private CartRepository cartRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AddressRepository addressRepository;
    @Mock
    private PointsHistoryRepository pointsHistoryRepository;
    @Mock
    private UserService userService;

    @InjectMocks
    private OrderService orderService;

    private static final Long USER_ID = 1L;
    private static final Long ADDRESS_ID = 10L;
    private static final Long PRODUCT_ID = 100L;

    private Cart cartItem;
    private Product product;
    private User user;
    private Address address;

    @BeforeEach
    void setUp() {
        cartItem = new Cart();
        cartItem.setId(1L);
        cartItem.setUserId(USER_ID);
        cartItem.setProductId(PRODUCT_ID);
        cartItem.setQuantity(2);
        cartItem.setIsSelected(1);

        product = new Product();
        product.setId(PRODUCT_ID);
        product.setName("测试商品");
        product.setPointsRequired(50);
        product.setStock(10);
        product.setSales(0);
        product.setImages("[]");

        user = new User();
        user.setId(USER_ID);
        user.setPoints(500);

        address = new Address();
        address.setId(ADDRESS_ID);
        address.setUserId(USER_ID);
        address.setReceiver("张三");
        address.setPhone("13800138000");
        address.setProvince("北京市");
        address.setCity("北京市");
        address.setDistrict("朝阳区");
        address.setDetailAddress("示例路 1 号");
    }

    @Test
    void createOrder_success_decrementsStockUpdatesPointsAndDeletesCart() {
        when(cartRepository.findByUserId(USER_ID)).thenReturn(List.of(cartItem));
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(addressRepository.findById(ADDRESS_ID)).thenReturn(Optional.of(address));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(99L);
            order.setOrderNo("ORD999");
            return order;
        });
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order result = orderService.createOrder(USER_ID, ADDRESS_ID, "备注");

        assertNotNull(result);
        assertEquals(99L, result.getId());
        assertEquals(100, result.getTotalPoints());

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository, atLeastOnce()).save(productCaptor.capture());
        assertEquals(8, productCaptor.getValue().getStock());
        assertEquals(2, productCaptor.getValue().getSales());

        verify(userService).updatePoints(USER_ID, -100);
        verify(cartRepository).delete(cartItem);
        verify(orderItemRepository).save(any(OrderItem.class));
        verify(pointsHistoryRepository).save(any(PointsHistory.class));
    }

    @Test
    void createOrder_throwsWhenStockInsufficient() {
        product.setStock(1);

        when(cartRepository.findByUserId(USER_ID)).thenReturn(List.of(cartItem));
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.createOrder(USER_ID, ADDRESS_ID, null));

        assertTrue(ex.getMessage().contains("库存不足"));
        verify(orderRepository, never()).save(any());
        verify(userService, never()).updatePoints(anyLong(), anyInt());
    }

    @Test
    void createOrder_throwsWhenPointsInsufficient() {
        user.setPoints(50);

        when(cartRepository.findByUserId(USER_ID)).thenReturn(List.of(cartItem));
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.createOrder(USER_ID, ADDRESS_ID, null));

        assertTrue(ex.getMessage().contains("积分不足"));
        verify(orderRepository, never()).save(any());
        verify(userService, never()).updatePoints(anyLong(), anyInt());
    }

    @Test
    void createOrder_throwsWhenAddressIdNull() {
        when(cartRepository.findByUserId(USER_ID)).thenReturn(List.of(cartItem));
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.createOrder(USER_ID, null, null));

        assertEquals("请先选择收货地址", ex.getMessage());
        verify(addressRepository, never()).findById(anyLong());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void cancelOrder_refundsPoints() {
        Order order = new Order();
        order.setId(5L);
        order.setUserId(USER_ID);
        order.setStatus("pending");
        order.setTotalPoints(120);
        order.setOrderNo("ORD5");

        OrderItem orderItem = new OrderItem();
        orderItem.setProductId(PRODUCT_ID);
        orderItem.setQuantity(1);

        when(orderRepository.findById(5L)).thenReturn(Optional.of(order));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(orderItemRepository.findByOrderId(5L)).thenReturn(List.of(orderItem));
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order cancelled = orderService.cancelOrder(5L, USER_ID);

        assertEquals("cancelled", cancelled.getStatus());
        verify(userService).updatePoints(USER_ID, 120);
        verify(pointsHistoryRepository).save(any(PointsHistory.class));
    }
}
