package com.example.mall.service;

import com.example.mall.common.BusinessException;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OrderServiceIntegrationTest {

    @Autowired
    private OrderService orderService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;
    @Autowired
    private PointsHistoryRepository pointsHistoryRepository;

    @Test
    void createsOrderWithServerTotalsSnapshotsAndDeductions() {
        User user = createUser(1000);
        Product first = createProduct("积分水杯", 100, 5);
        Product second = createProduct("积分鼠标", 50, 5);
        Product unselected = createProduct("未选商品", 60, 5);
        Address address = createAddress(user.getId());
        createCart(user.getId(), first.getId(), 2, 1);
        createCart(user.getId(), second.getId(), 1, 1);
        createCart(user.getId(), unselected.getId(), 1, 0);

        Order order = orderService.createOrder(user.getId(), address.getId(), "请尽快发货");

        assertThat(order.getId()).isNotNull();
        assertThat(order.getOrderNo()).startsWith("ORD");
        assertThat(order.getStatus()).isEqualTo("pending");
        assertThat(order.getTotalPoints()).isEqualTo(250);
        assertThat(order.getReceiver()).isEqualTo("张三");
        assertThat(order.getReceiverPhone()).isEqualTo("13800000000");
        assertThat(order.getReceiverAddress()).contains("浙江省", "杭州市", "西湖区", "文一西路 1 号");
        assertThat(order.getRemark()).isEqualTo("请尽快发货");

        User refreshedUser = userRepository.findById(user.getId()).orElseThrow();
        assertThat(refreshedUser.getPoints()).isEqualTo(750);

        Product refreshedFirst = productRepository.findById(first.getId()).orElseThrow();
        Product refreshedSecond = productRepository.findById(second.getId()).orElseThrow();
        Product refreshedUnselected = productRepository.findById(unselected.getId()).orElseThrow();
        assertThat(refreshedFirst.getStock()).isEqualTo(3);
        assertThat(refreshedFirst.getSales()).isEqualTo(2);
        assertThat(refreshedSecond.getStock()).isEqualTo(4);
        assertThat(refreshedSecond.getSales()).isEqualTo(1);
        assertThat(refreshedUnselected.getStock()).isEqualTo(5);
        assertThat(refreshedUnselected.getSales()).isZero();

        List<Cart> remainingCart = cartRepository.findByUserId(user.getId());
        assertThat(remainingCart).hasSize(1);
        assertThat(remainingCart.get(0).getProductId()).isEqualTo(unselected.getId());

        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        assertThat(items).hasSize(2);
        assertThat(items).extracting(OrderItem::getProductName).containsExactlyInAnyOrder("积分水杯", "积分鼠标");
        assertThat(items).filteredOn(item -> item.getProductId().equals(first.getId()))
                .singleElement()
                .satisfies(item -> {
                    assertThat(item.getPointsRequired()).isEqualTo(100);
                    assertThat(item.getQuantity()).isEqualTo(2);
                    assertThat(item.getSubtotalPoints()).isEqualTo(200);
                });

        List<PointsHistory> spendHistory = pointsHistoryRepository
                .findByUserIdAndTypeOrderByCreatedAtDesc(user.getId(), "spend");
        assertThat(spendHistory).hasSize(1);
        assertThat(spendHistory.get(0).getPoints()).isEqualTo(-250);
        assertThat(spendHistory.get(0).getBalanceAfter()).isEqualTo(750);
        assertThat(spendHistory.get(0).getRelatedId()).isEqualTo(order.getId());
    }

    @Test
    void keepsOrderItemSnapshotWhenProductChangesLater() {
        User user = createUser(500);
        Product product = createProduct("下单时名称", 100, 5);
        Address address = createAddress(user.getId());
        createCart(user.getId(), product.getId(), 1, 1);

        Order order = orderService.createOrder(user.getId(), address.getId(), null);

        product.setName("修改后名称");
        product.setPointsRequired(999);
        productRepository.save(product);

        OrderItem item = orderItemRepository.findByOrderId(order.getId()).get(0);
        assertThat(item.getProductName()).isEqualTo("下单时名称");
        assertThat(item.getPointsRequired()).isEqualTo(100);
        assertThat(order.getTotalPoints()).isEqualTo(100);
    }

    @Test
    void rejectsWhenNoSelectedCartItems() {
        User user = createUser(500);
        Product product = createProduct("未选商品", 100, 5);
        Address address = createAddress(user.getId());
        createCart(user.getId(), product.getId(), 1, 0);
        long ordersBefore = orderRepository.count();

        BusinessException error = businessError(() ->
                orderService.createOrder(user.getId(), address.getId(), null));

        assertThat(error.getCode()).isEqualTo("CART_EMPTY");
        assertThat(orderRepository.count()).isEqualTo(ordersBefore);
        assertThat(userRepository.findById(user.getId()).orElseThrow().getPoints()).isEqualTo(500);
    }

    @Test
    void rejectsMissingAddressWithoutSideEffects() {
        User user = createUser(500);
        Product product = createProduct("积分水杯", 100, 5);
        createCart(user.getId(), product.getId(), 1, 1);

        BusinessException error = businessError(() -> orderService.createOrder(user.getId(), null, null));

        assertThat(error.getCode()).isEqualTo("ADDR_REQUIRED");
        assertThat(orderService.getUserOrders(user.getId())).isEmpty();
        assertThat(userRepository.findById(user.getId()).orElseThrow().getPoints()).isEqualTo(500);
        assertThat(productRepository.findById(product.getId()).orElseThrow().getStock()).isEqualTo(5);
        assertThat(cartRepository.findByUserId(user.getId())).hasSize(1);
    }

    @Test
    void rejectsAddressOwnedByAnotherUser() {
        User buyer = createUser(500);
        User other = createUser(500);
        Product product = createProduct("积分水杯", 100, 5);
        Address otherAddress = createAddress(other.getId());
        createCart(buyer.getId(), product.getId(), 1, 1);

        BusinessException error = businessError(() ->
                orderService.createOrder(buyer.getId(), otherAddress.getId(), null));

        assertThat(error.getCode()).isEqualTo("ADDR_NOT_FOUND");
        assertThat(orderService.getUserOrders(buyer.getId())).isEmpty();
        assertThat(userRepository.findById(buyer.getId()).orElseThrow().getPoints()).isEqualTo(500);
    }

    @Test
    void rejectsProductThatIsNotActive() {
        User user = createUser(500);
        Product product = createProduct("已下架商品", 100, 5);
        product.setStatus("inactive");
        productRepository.save(product);
        Address address = createAddress(user.getId());
        createCart(user.getId(), product.getId(), 1, 1);

        BusinessException error = businessError(() ->
                orderService.createOrder(user.getId(), address.getId(), null));

        assertThat(error.getCode()).isEqualTo("PRODUCT_UNAVAILABLE");
        assertThat(error.getMessage()).contains("已下架商品");
        assertThat(orderService.getUserOrders(user.getId())).isEmpty();
        assertThat(userRepository.findById(user.getId()).orElseThrow().getPoints()).isEqualTo(500);
        assertThat(productRepository.findById(product.getId()).orElseThrow().getStock()).isEqualTo(5);
        assertThat(cartRepository.findByUserId(user.getId())).hasSize(1);
    }

    @Test
    void rejectsInsufficientStockWithoutPartialDeduction() {
        User user = createUser(500);
        Product enough = createProduct("库存充足", 100, 5);
        Product scarce = createProduct("库存不足", 50, 1);
        Address address = createAddress(user.getId());
        createCart(user.getId(), enough.getId(), 1, 1);
        createCart(user.getId(), scarce.getId(), 2, 1);
        long ordersBefore = orderRepository.count();

        BusinessException error = businessError(() ->
                orderService.createOrder(user.getId(), address.getId(), null));

        assertThat(error.getCode()).isEqualTo("STOCK_INSUFFICIENT");
        assertThat(error.getMessage()).contains("库存不足").contains("仅剩 1").contains("需要 2");
        assertThat(orderRepository.count()).isEqualTo(ordersBefore);
        assertThat(userRepository.findById(user.getId()).orElseThrow().getPoints()).isEqualTo(500);
        assertThat(productRepository.findById(enough.getId()).orElseThrow().getStock()).isEqualTo(5);
        assertThat(productRepository.findById(scarce.getId()).orElseThrow().getStock()).isEqualTo(1);
        assertThat(cartRepository.findByUserId(user.getId())).hasSize(2);
        assertThat(pointsHistoryRepository.findByUserIdOrderByCreatedAtDesc(user.getId())).isEmpty();
    }

    @Test
    void rejectsInsufficientPointsWithShortfallMessage() {
        User user = createUser(30);
        Product product = createProduct("积分水杯", 50, 5);
        Address address = createAddress(user.getId());
        createCart(user.getId(), product.getId(), 1, 1);

        BusinessException error = businessError(() ->
                orderService.createOrder(user.getId(), address.getId(), null));

        assertThat(error.getCode()).isEqualTo("POINTS_INSUFFICIENT");
        assertThat(error.getMessage()).contains("当前 30").contains("需支付 50").contains("还差 20");
        assertThat(userRepository.findById(user.getId()).orElseThrow().getPoints()).isEqualTo(30);
        assertThat(productRepository.findById(product.getId()).orElseThrow().getStock()).isEqualTo(5);
        assertThat(cartRepository.findByUserId(user.getId())).hasSize(1);
    }

    @Test
    void generatesDistinctOrderNumbersForConsecutiveOrders() {
        User user = createUser(1000);
        Product product = createProduct("积分水杯", 100, 5);
        Address address = createAddress(user.getId());

        createCart(user.getId(), product.getId(), 1, 1);
        Order first = orderService.createOrder(user.getId(), address.getId(), null);
        createCart(user.getId(), product.getId(), 1, 1);
        Order second = orderService.createOrder(user.getId(), address.getId(), null);

        assertThat(first.getOrderNo()).isNotEqualTo(second.getOrderNo());
    }

    @Test
    void cancelsPendingOrderAndRestoresPointsStockAndLedger() {
        User user = createUser(1000);
        Product product = createProduct("积分水杯", 100, 5);
        Address address = createAddress(user.getId());
        createCart(user.getId(), product.getId(), 2, 1);
        Order order = orderService.createOrder(user.getId(), address.getId(), null);

        Order cancelled = orderService.cancelOrder(order.getId(), user.getId());

        assertThat(cancelled.getStatus()).isEqualTo("cancelled");
        assertThat(userRepository.findById(user.getId()).orElseThrow().getPoints()).isEqualTo(1000);
        Product restored = productRepository.findById(product.getId()).orElseThrow();
        assertThat(restored.getStock()).isEqualTo(5);
        assertThat(restored.getSales()).isZero();

        List<PointsHistory> refunds = pointsHistoryRepository
                .findByUserIdAndTypeOrderByCreatedAtDesc(user.getId(), "refund");
        assertThat(refunds).hasSize(1);
        assertThat(refunds.get(0).getPoints()).isEqualTo(200);
        assertThat(refunds.get(0).getBalanceAfter()).isEqualTo(1000);
        assertThat(refunds.get(0).getRelatedId()).isEqualTo(order.getId());
    }

    @Test
    void rejectsCancelWhenOrderIsNotPending() {
        User user = createUser(1000);
        Product product = createProduct("积分水杯", 100, 5);
        Address address = createAddress(user.getId());
        createCart(user.getId(), product.getId(), 1, 1);
        Order order = orderService.createOrder(user.getId(), address.getId(), null);
        order.setStatus("completed");
        orderRepository.save(order);

        BusinessException error = businessError(() ->
                orderService.cancelOrder(order.getId(), user.getId()));

        assertThat(error.getCode()).isEqualTo("ORDER_STATUS_INVALID");
        assertThat(orderRepository.findById(order.getId()).orElseThrow().getStatus()).isEqualTo("completed");
        assertThat(userRepository.findById(user.getId()).orElseThrow().getPoints()).isEqualTo(900);
        assertThat(productRepository.findById(product.getId()).orElseThrow().getStock()).isEqualTo(4);
    }

    @Test
    void rejectsCancelByAnotherUser() {
        User owner = createUser(1000);
        User intruder = createUser(1000);
        Product product = createProduct("积分水杯", 100, 5);
        Address address = createAddress(owner.getId());
        createCart(owner.getId(), product.getId(), 1, 1);
        Order order = orderService.createOrder(owner.getId(), address.getId(), null);

        BusinessException error = businessError(() ->
                orderService.cancelOrder(order.getId(), intruder.getId()));

        assertThat(error.getCode()).isEqualTo("ORDER_FORBIDDEN");
        assertThat(orderRepository.findById(order.getId()).orElseThrow().getStatus()).isEqualTo("pending");
        assertThat(userRepository.findById(owner.getId()).orElseThrow().getPoints()).isEqualTo(900);
        assertThat(productRepository.findById(product.getId()).orElseThrow().getStock()).isEqualTo(4);
    }

    private BusinessException businessError(org.assertj.core.api.ThrowableAssert.ThrowingCallable action) {
        Throwable throwable = catchThrowable(action);
        assertThat(throwable).isInstanceOf(BusinessException.class);
        return (BusinessException) throwable;
    }

    private User createUser(int points) {
        User user = new User();
        user.setUsername("test-" + UUID.randomUUID());
        user.setPassword("password");
        user.setEmail("test@example.com");
        user.setPoints(points);
        user.setRole("user");
        user.setStatus("active");
        return userRepository.save(user);
    }

    private Product createProduct(String name, int pointsRequired, int stock) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(name);
        product.setPrice(0.0);
        product.setPointsRequired(pointsRequired);
        product.setStock(stock);
        product.setSales(0);
        product.setStatus("active");
        product.setImages("[]");
        product.setSpecifications("{}");
        return productRepository.save(product);
    }

    private Address createAddress(Long userId) {
        Address address = new Address();
        address.setUserId(userId);
        address.setReceiver("张三");
        address.setPhone("13800000000");
        address.setProvince("浙江省");
        address.setCity("杭州市");
        address.setDistrict("西湖区");
        address.setDetailAddress("文一西路 1 号");
        address.setIsDefault(1);
        return addressRepository.save(address);
    }

    private Cart createCart(Long userId, Long productId, int quantity, int isSelected) {
        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setProductId(productId);
        cart.setQuantity(quantity);
        cart.setIsSelected(isSelected);
        return cartRepository.save(cart);
    }
}
