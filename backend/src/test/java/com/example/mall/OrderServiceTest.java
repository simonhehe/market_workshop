package com.example.mall;

import com.example.mall.entity.*;
import com.example.mall.repository.*;
import com.example.mall.service.OrderService;
import com.example.mall.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock OrderRepository orderRepository;
    @Mock OrderItemRepository orderItemRepository;
    @Mock CartRepository cartRepository;
    @Mock ProductRepository productRepository;
    @Mock UserRepository userRepository;
    @Mock AddressRepository addressRepository;
    @Mock PointsHistoryRepository pointsHistoryRepository;
    @Mock UserService userService;

    @InjectMocks OrderService orderService;

    Cart cart;
    Product product;
    User user;
    Address address;

    @BeforeEach
    void setUp() {
        cart = new Cart();
        cart.setId(1L);
        cart.setUserId(10L);
        cart.setProductId(20L);
        cart.setQuantity(2);
        cart.setIsSelected(1);

        product = new Product();
        product.setId(20L);
        product.setName("unit-product");
        product.setPointsRequired(50);
        product.setStock(10);
        product.setSales(0);

        user = new User();
        user.setId(10L);
        user.setPoints(500);

        address = new Address();
        address.setId(30L);
        address.setUserId(10L);
        address.setReceiver("tester");
        address.setPhone("13800000000");
        address.setProvince("P");
        address.setCity("C");
        address.setDistrict("D");
        address.setDetailAddress("detail");
    }

    @Test
    void cancelOrder_skipsRefundWhenUserMissing() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(10L);
        order.setStatus("pending");
        order.setTotalPoints(100);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(userRepository.findById(10L)).thenReturn(Optional.empty());
        when(orderItemRepository.findByOrderId(1L)).thenReturn(List.of());
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Order cancelled = orderService.cancelOrder(1L, 10L);
        assertEquals("cancelled", cancelled.getStatus());
        verify(userService, never()).updatePoints(anyLong(), anyInt());
    }

    @Test
    void createOrder_success_deductsPointsAndClearsCart() {
        when(cartRepository.findByUserId(10L)).thenReturn(List.of(cart));
        when(productRepository.findById(20L)).thenReturn(Optional.of(product));
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(addressRepository.findById(30L)).thenReturn(Optional.of(address));
        Order saved = new Order();
        saved.setId(100L);
        saved.setOrderNo("NO100");
        when(orderRepository.save(any())).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(100L);
            o.setOrderNo("NO100");
            return o;
        });
        when(productRepository.findById(20L)).thenReturn(Optional.of(product));

        Order order = orderService.createOrder(10L, 30L, "remark");
        assertEquals(100, order.getId());
        verify(userService).updatePoints(10L, -100);
        verify(cartRepository).delete(cart);
    }

    @Test
    void cancelOrder_restoresStockAndRefunds() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(10L);
        order.setStatus("pending");
        order.setTotalPoints(100);
        order.setOrderNo("NO1");
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));

        OrderItem item = new OrderItem();
        item.setProductId(20L);
        item.setQuantity(2);
        when(orderItemRepository.findByOrderId(1L)).thenReturn(List.of(item));
        when(productRepository.findById(20L)).thenReturn(Optional.of(product));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Order cancelled = orderService.cancelOrder(1L, 10L);
        assertEquals("cancelled", cancelled.getStatus());
        assertEquals(12, product.getStock());
        verify(userService).updatePoints(10L, 100);
    }

    @Test
    void createOrder_skipsMissingProductWhenAdjustingStock() {
        when(cartRepository.findByUserId(10L)).thenReturn(List.of(cart));
        when(productRepository.findById(20L)).thenReturn(Optional.of(product), Optional.empty());
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(addressRepository.findById(30L)).thenReturn(Optional.of(address));
        when(orderRepository.save(any())).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(100L);
            o.setOrderNo("NO100");
            return o;
        });

        assertDoesNotThrow(() -> orderService.createOrder(10L, 30L, null));
    }

    @Test
    void createOrder_rejectsWhenProductMissing() {
        when(cartRepository.findByUserId(10L)).thenReturn(List.of(cart));
        when(productRepository.findById(20L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.createOrder(10L, 30L, null));
        assertTrue(ex.getMessage().contains("商品不存在"));
    }

    @Test
    void createOrder_rejectsDeletedAddress() {
        when(cartRepository.findByUserId(10L)).thenReturn(List.of(cart));
        when(productRepository.findById(20L)).thenReturn(Optional.of(product));
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        address.setDeletedAt(LocalDateTime.now());
        when(addressRepository.findById(30L)).thenReturn(Optional.of(address));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.createOrder(10L, 30L, null));
        assertTrue(ex.getMessage().contains("收货地址不存在"));
    }

    @Test
    void getUserOrders_delegatesToRepository() {
        when(orderRepository.findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(10L))
                .thenReturn(List.of(new Order()));
        assertEquals(1, orderService.getUserOrders(10L).size());
    }

    @Test
    void getOrderById_delegatesToRepository() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(new Order()));
        assertTrue(orderService.getOrderById(1L).isPresent());
    }

    @Test
    void cancelOrder_rejectsForeignOrder() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(99L);
        order.setStatus("pending");
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.cancelOrder(1L, 10L));
        assertTrue(ex.getMessage().contains("无权"));
    }

    @Test
    void cancelOrder_rejectsWhenOrderMissing() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> orderService.cancelOrder(1L, 10L));
    }
}
