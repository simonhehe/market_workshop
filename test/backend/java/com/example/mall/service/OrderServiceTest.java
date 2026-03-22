package com.example.mall.service;

import com.example.mall.entity.Address;
import com.example.mall.entity.Cart;
import com.example.mall.entity.Order;
import com.example.mall.entity.OrderItem;
import com.example.mall.entity.PointsHistory;
import com.example.mall.entity.Product;
import com.example.mall.entity.User;
import com.example.mall.repository.AddressRepository;
import com.example.mall.repository.CartRepository;
import com.example.mall.repository.OrderItemRepository;
import com.example.mall.repository.OrderRepository;
import com.example.mall.repository.PointsHistoryRepository;
import com.example.mall.repository.ProductRepository;
import com.example.mall.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    @Test
    void getUserOrders_nullStatus_returnsAll() {
        when(orderRepository.findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(1L)).thenReturn(List.of());
        orderService.getUserOrders(1L, null);
        verify(orderRepository).findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(1L);
        verify(orderRepository, never()).findOrders(anyLong(), any());
    }

    @Test
    void getUserOrders_allStatus_returnsAll() {
        when(orderRepository.findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(2L)).thenReturn(List.of());
        orderService.getUserOrders(2L, "  ALL ");
        verify(orderRepository).findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(2L);
    }

    @Test
    void getUserOrders_specificStatus_usesFindOrders() {
        Order o = new Order();
        when(orderRepository.findOrders(3L, "pending")).thenReturn(List.of(o));
        List<Order> result = orderService.getUserOrders(3L, "pending");
        assertEquals(1, result.size());
        verify(orderRepository).findOrders(3L, "pending");
        verify(orderRepository, never()).findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(anyLong());
    }

    @Test
    void createOrder_rejectsAddressOwnedByOtherUser() {
        Long userId = 1L;

        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setProductId(10L);
        cart.setQuantity(1);
        cart.setIsSelected(1);

        Product product = new Product();
        product.setId(10L);
        product.setName("p");
        product.setStock(5);
        product.setPointsRequired(10);
        product.setSales(0);

        User user = new User();
        user.setId(userId);
        user.setPoints(100);

        Address address = new Address();
        address.setId(99L);
        address.setUserId(2L);

        when(cartRepository.findByUserId(userId)).thenReturn(List.of(cart));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(addressRepository.findById(99L)).thenReturn(Optional.of(address));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.createOrder(userId, 99L, null));
        assertTrue(ex.getMessage().contains("无权使用该收货地址"));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrder_failsWhenNoSelectedCartItems() {
        when(cartRepository.findByUserId(1L)).thenReturn(List.of());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.createOrder(1L, null, null));
        assertTrue(ex.getMessage().contains("购物车中没有选中的商品"));
        verify(orderRepository, never()).save(any());
    }

    /** WB-O-02 */
    @Test
    void createOrder_failsWhenProductNotFound() {
        Cart cart = new Cart();
        cart.setUserId(1L);
        cart.setProductId(999L);
        cart.setQuantity(1);
        cart.setIsSelected(1);
        when(cartRepository.findByUserId(1L)).thenReturn(List.of(cart));
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.createOrder(1L, null, null));
        assertTrue(ex.getMessage().contains("商品不存在"));
        verify(orderRepository, never()).save(any());
    }

    /** WB-O-02 */
    @Test
    void createOrder_failsWhenInsufficientStock() {
        Cart cart = new Cart();
        cart.setUserId(1L);
        cart.setProductId(10L);
        cart.setQuantity(5);
        cart.setIsSelected(1);
        Product product = new Product();
        product.setId(10L);
        product.setName("P");
        product.setStock(2);
        product.setPointsRequired(10);
        when(cartRepository.findByUserId(1L)).thenReturn(List.of(cart));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.createOrder(1L, null, null));
        assertTrue(ex.getMessage().contains("库存不足"));
        verify(orderRepository, never()).save(any());
    }

    /** WB-O-03 */
    @Test
    void createOrder_failsWhenInsufficientPoints() {
        Cart cart = new Cart();
        cart.setUserId(1L);
        cart.setProductId(10L);
        cart.setQuantity(1);
        cart.setIsSelected(1);
        Product product = new Product();
        product.setId(10L);
        product.setName("P");
        product.setStock(10);
        product.setPointsRequired(100);
        product.setSales(0);
        User user = new User();
        user.setId(1L);
        user.setPoints(50);
        when(cartRepository.findByUserId(1L)).thenReturn(List.of(cart));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.createOrder(1L, null, null));
        assertTrue(ex.getMessage().contains("积分不足"));
        verify(orderRepository, never()).save(any());
    }

    /** WB-O-05 */
    @Test
    void createOrder_success_updatesStockPointsHistoryAndClearsCart() {
        Long userId = 1L;
        Cart cart = new Cart();
        cart.setId(77L);
        cart.setUserId(userId);
        cart.setProductId(10L);
        cart.setQuantity(2);
        cart.setIsSelected(1);

        Product product = new Product();
        product.setId(10L);
        product.setName("Item");
        product.setImages("[]");
        product.setStock(10);
        product.setPointsRequired(15);
        product.setSales(3);

        User user = new User();
        user.setId(userId);
        user.setPoints(200);

        when(cartRepository.findByUserId(userId)).thenReturn(List.of(cart));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(300L);
            return o;
        });

        Order result = orderService.createOrder(userId, null, null);

        assertEquals(300L, result.getId());
        verify(userService).updatePoints(userId, -30); // pointsRequired 15 * quantity 2
        verify(pointsHistoryRepository).save(any(PointsHistory.class));
        verify(cartRepository).delete(cart);
        assertEquals(8, product.getStock());
        assertEquals(5, product.getSales()); // was 3, +2 sold
        verify(productRepository, atLeastOnce()).save(product);
        verify(orderItemRepository, atLeastOnce()).save(any(OrderItem.class));
    }

    /** WB-O-09 */
    @Test
    void cancelOrder_success_refundsPointsAndRestoresStock() {
        Long orderId = 5L;
        Long userId = 1L;
        Order order = new Order();
        order.setId(orderId);
        order.setUserId(userId);
        order.setStatus("pending");
        order.setTotalPoints(40);
        order.setOrderNo("ORD5");

        User user = new User();
        user.setId(userId);
        user.setPoints(100);

        OrderItem oi = new OrderItem();
        oi.setOrderId(orderId);
        oi.setProductId(10L);
        oi.setQuantity(2);

        Product product = new Product();
        product.setId(10L);
        product.setStock(5);
        product.setSales(7);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(orderItemRepository.findByOrderId(orderId)).thenReturn(List.of(oi));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order out = orderService.cancelOrder(orderId, userId);

        assertEquals("cancelled", out.getStatus());
        verify(userService).updatePoints(userId, 40);
        verify(pointsHistoryRepository).save(any(PointsHistory.class));
        assertEquals(7, product.getStock());
        assertEquals(5, product.getSales());
        verify(productRepository).save(product);
    }

    /**
     * WB-O-10：用户记录在取消时已不存在时，当前实现仍回补库存并置 cancelled，但不退积分、不写退款流水（与矩阵 §1 说明一致）。
     */
    @Test
    void cancelOrder_whenUserRecordMissing_stillCancelsAndRestoresStockWithoutRefund() {
        Long orderId = 6L;
        Long userId = 1L;
        Order order = new Order();
        order.setId(orderId);
        order.setUserId(userId);
        order.setStatus("pending");
        order.setTotalPoints(40);

        OrderItem oi = new OrderItem();
        oi.setProductId(10L);
        oi.setQuantity(1);

        Product product = new Product();
        product.setId(10L);
        product.setStock(3);
        product.setSales(1);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        when(orderItemRepository.findByOrderId(orderId)).thenReturn(List.of(oi));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order out = orderService.cancelOrder(orderId, userId);

        assertEquals("cancelled", out.getStatus());
        verify(userService, never()).updatePoints(anyLong(), any());
        verify(pointsHistoryRepository, never()).save(any());
        assertEquals(4, product.getStock());
        verify(orderRepository).save(order);
    }

    @Test
    void cancelOrder_failsWhenNotOwner() {
        Order order = new Order();
        order.setId(10L);
        order.setUserId(2L);
        order.setStatus("pending");
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.cancelOrder(10L, 1L));
        assertTrue(ex.getMessage().contains("无权操作此订单"));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void cancelOrder_failsWhenNotPending() {
        Order order = new Order();
        order.setId(11L);
        order.setUserId(1L);
        order.setStatus("processing");
        when(orderRepository.findById(11L)).thenReturn(Optional.of(order));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.cancelOrder(11L, 1L));
        assertTrue(ex.getMessage().contains("只能取消待处理状态的订单"));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void confirmReceipt_succeedsFromShipped() {
        Order order = new Order();
        order.setId(20L);
        order.setUserId(1L);
        order.setStatus("shipped");
        when(orderRepository.findById(20L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order result = orderService.confirmReceipt(20L, 1L);

        assertEquals("completed", result.getStatus());
        verify(orderRepository).save(eq(order));
    }

    @Test
    void confirmReceipt_failsWhenNotOwner() {
        Order order = new Order();
        order.setId(21L);
        order.setUserId(2L);
        order.setStatus("shipped");
        when(orderRepository.findById(21L)).thenReturn(Optional.of(order));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.confirmReceipt(21L, 1L));
        assertTrue(ex.getMessage().contains("无权操作此订单"));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void confirmReceipt_failsWhenStatusNotShipped() {
        Order order = new Order();
        order.setId(22L);
        order.setUserId(1L);
        order.setStatus("pending");
        when(orderRepository.findById(22L)).thenReturn(Optional.of(order));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.confirmReceipt(22L, 1L));
        assertTrue(ex.getMessage().contains("只有已发货的订单可以确认收货"));
        verify(orderRepository, never()).save(any());
    }
}
