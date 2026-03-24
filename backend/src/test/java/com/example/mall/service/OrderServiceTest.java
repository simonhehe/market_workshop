package com.example.mall.service;

import com.example.mall.entity.*;
import com.example.mall.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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

    private Cart selectedCart;
    private Product product;
    private User user;

    @BeforeEach
    void setUp() {
        selectedCart = new Cart();
        selectedCart.setId(1L);
        selectedCart.setUserId(10L);
        selectedCart.setProductId(20L);
        selectedCart.setQuantity(2);
        selectedCart.setIsSelected(1);

        product = new Product();
        product.setId(20L);
        product.setName("测试商品");
        product.setPointsRequired(50);
        product.setStock(100);
        product.setSales(0);

        user = new User();
        user.setId(10L);
        user.setPoints(500);
    }

    @Test
    @DisplayName("createOrder: 无选中商品则失败")
    void createOrder_failsWhenNoSelectedItems() {
        when(cartRepository.findByUserId(10L)).thenReturn(List.of());

        assertThatThrownBy(() -> orderService.createOrder(10L, null, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("购物车中没有选中的商品");

        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("createOrder: 商品不存在则失败")
    void createOrder_failsWhenProductMissing() {
        when(cartRepository.findByUserId(10L)).thenReturn(List.of(selectedCart));
        when(productRepository.findById(20L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createOrder(10L, null, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("商品不存在");
    }

    @Test
    @DisplayName("createOrder: 库存不足则失败")
    void createOrder_failsWhenInsufficientStock() {
        when(cartRepository.findByUserId(10L)).thenReturn(List.of(selectedCart));
        product.setStock(1);
        when(productRepository.findById(20L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> orderService.createOrder(10L, null, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("库存不足");
    }

    @Test
    @DisplayName("createOrder: 用户不存在则失败")
    void createOrder_failsWhenUserMissing() {
        when(cartRepository.findByUserId(10L)).thenReturn(List.of(selectedCart));
        when(productRepository.findById(20L)).thenReturn(Optional.of(product));
        when(userRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createOrder(10L, null, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("用户不存在");
    }

    @Test
    @DisplayName("createOrder: 积分不足则失败")
    void createOrder_failsWhenInsufficientPoints() {
        when(cartRepository.findByUserId(10L)).thenReturn(List.of(selectedCart));
        when(productRepository.findById(20L)).thenReturn(Optional.of(product));
        user.setPoints(50);
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> orderService.createOrder(10L, null, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("积分不足");
    }

    @Test
    @DisplayName("createOrder: addressId 非空但地址不存在则失败")
    void createOrder_failsWhenAddressMissing() {
        when(cartRepository.findByUserId(10L)).thenReturn(List.of(selectedCart));
        when(productRepository.findById(20L)).thenReturn(Optional.of(product));
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(addressRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createOrder(10L, 99L, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("收货地址不存在");
    }

    @Test
    @DisplayName("createOrder: 成功路径扣积分、写 spend 流水、删购物车")
    void createOrder_success() {
        when(cartRepository.findByUserId(10L)).thenReturn(List.of(selectedCart));
        when(productRepository.findById(20L)).thenReturn(Optional.of(product));
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(200L);
            return o;
        });

        Order result = orderService.createOrder(10L, null, "备注");

        assertThat(result.getId()).isEqualTo(200L);
        assertThat(result.getStatus()).isEqualTo("pending");
        verify(userService).updatePoints(10L, -100);

        ArgumentCaptor<PointsHistory> phCap = ArgumentCaptor.forClass(PointsHistory.class);
        verify(pointsHistoryRepository).save(phCap.capture());
        PointsHistory ph = phCap.getValue();
        assertThat(ph.getType()).isEqualTo("spend");
        assertThat(ph.getPoints()).isEqualTo(-100);
        assertThat(ph.getBalanceAfter()).isEqualTo(400);

        verify(cartRepository).delete(selectedCart);
    }

    @Test
    @DisplayName("cancelOrder: 订单不存在则失败")
    void cancelOrder_failsWhenOrderMissing() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.cancelOrder(1L, 10L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("订单不存在");
    }

    @Test
    @DisplayName("cancelOrder: 非本人则失败")
    void cancelOrder_failsWhenNotOwner() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(99L);
        order.setStatus("pending");
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelOrder(1L, 10L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("无权操作此订单");
    }

    @Test
    @DisplayName("cancelOrder: 非 pending 则失败（状态校验）")
    void cancelOrder_failsWhenNotPending() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(10L);
        order.setStatus("shipped");
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelOrder(1L, 10L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("只能取消待处理状态的订单");
    }

    @Test
    @DisplayName("cancelOrder: pending 成功退积分、refund 流水、还库存")
    void cancelOrder_success() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(10L);
        order.setStatus("pending");
        order.setTotalPoints(100);
        order.setOrderNo("ORD1");

        OrderItem item = new OrderItem();
        item.setProductId(20L);
        item.setQuantity(2);

        Product p = new Product();
        p.setId(20L);
        p.setStock(50);
        p.setSales(10);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(orderItemRepository.findByOrderId(1L)).thenReturn(List.of(item));
        when(productRepository.findById(20L)).thenReturn(Optional.of(p));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order saved = orderService.cancelOrder(1L, 10L);

        assertThat(saved.getStatus()).isEqualTo("cancelled");
        verify(userService).updatePoints(10L, 100);
        ArgumentCaptor<PointsHistory> phCap = ArgumentCaptor.forClass(PointsHistory.class);
        verify(pointsHistoryRepository).save(phCap.capture());
        assertThat(phCap.getValue().getType()).isEqualTo("refund");
        assertThat(p.getStock()).isEqualTo(52);
        assertThat(p.getSales()).isEqualTo(8);
    }

    @Test
    @DisplayName("cancelOrder: 用户记录偶发缺失时仍还库存并置 cancelled（退款分支跳过）")
    void cancelOrder_restoresStockWhenUserRecordMissing() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(10L);
        order.setStatus("pending");
        order.setTotalPoints(100);
        order.setOrderNo("ORD1");

        OrderItem item = new OrderItem();
        item.setProductId(20L);
        item.setQuantity(1);

        Product p = new Product();
        p.setId(20L);
        p.setStock(5);
        p.setSales(3);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(userRepository.findById(10L)).thenReturn(Optional.empty());
        when(orderItemRepository.findByOrderId(1L)).thenReturn(List.of(item));
        when(productRepository.findById(20L)).thenReturn(Optional.of(p));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order saved = orderService.cancelOrder(1L, 10L);

        assertThat(saved.getStatus()).isEqualTo("cancelled");
        verify(userService, never()).updatePoints(anyLong(), anyInt());
        verify(pointsHistoryRepository, never()).save(any());
        assertThat(p.getStock()).isEqualTo(6);
    }

    @Test
    @DisplayName("getUserOrders: null/空 status 委托全量查询")
    void getUserOrders_delegatesToRepositoryWhenNoStatus() {
        Order o = new Order();
        when(orderRepository.findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(10L)).thenReturn(List.of(o));

        assertThat(orderService.getUserOrders(10L, null)).containsExactly(o);
        assertThat(orderService.getUserOrders(10L, "  ")).containsExactly(o);
    }

    @Test
    @DisplayName("getUserOrders: 合法 status 按用户+状态查询（含 trim）")
    void getUserOrders_withStatus() {
        Order o = new Order();
        when(orderRepository.findByUserIdAndStatusAndDeletedAtIsNullOrderByCreatedAtDesc(10L, "shipped"))
                .thenReturn(List.of(o));

        assertThat(orderService.getUserOrders(10L, "shipped")).containsExactly(o);
        assertThat(orderService.getUserOrders(10L, "  shipped  ")).containsExactly(o);
    }

    @Test
    @DisplayName("getUserOrders: 非法 status 抛 IllegalArgumentException")
    void getUserOrders_invalidStatus() {
        assertThatThrownBy(() -> orderService.getUserOrders(10L, "invalid"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("无效的状态筛选参数");
    }

    @Test
    @DisplayName("confirmReceipt: 非 shipped 失败")
    void confirmReceipt_failsWhenNotShipped() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(10L);
        order.setStatus("pending");
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.confirmReceipt(1L, 10L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("只有已发货");
    }

    @Test
    @DisplayName("confirmReceipt: completed 幂等返回")
    void confirmReceipt_idempotentWhenCompleted() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(10L);
        order.setStatus("completed");
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        Order out = orderService.confirmReceipt(1L, 10L);

        assertThat(out.getStatus()).isEqualTo("completed");
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("confirmReceipt: shipped 成功置 completed")
    void confirmReceipt_success() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(10L);
        order.setStatus("shipped");
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order out = orderService.confirmReceipt(1L, 10L);

        assertThat(out.getStatus()).isEqualTo("completed");
        verify(orderRepository).save(order);
    }

    @Test
    @DisplayName("confirmReceipt: 非本人失败")
    void confirmReceipt_failsWhenNotOwner() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(99L);
        order.setStatus("shipped");
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.confirmReceipt(1L, 10L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("无权操作此订单");
    }
}
