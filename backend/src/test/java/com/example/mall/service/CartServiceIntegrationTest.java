package com.example.mall.service;

import com.example.mall.common.BusinessException;
import com.example.mall.entity.Cart;
import com.example.mall.entity.Product;
import com.example.mall.entity.User;
import com.example.mall.repository.CartRepository;
import com.example.mall.repository.ProductRepository;
import com.example.mall.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CartServiceIntegrationTest {

    @Autowired
    private CartService cartService;
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserRepository userRepository;

    @Test
    void persistsSelectionState() {
        User user = createUser();
        Product product = createProduct(5);
        Cart cart = createCart(user.getId(), product.getId(), 1, 1);

        Cart updated = cartService.selectItem(cart.getId(), user.getId(), 0);

        assertThat(updated.getIsSelected()).isZero();
        assertThat(cartRepository.findById(cart.getId()).orElseThrow().getIsSelected()).isZero();
    }

    @Test
    void rejectsSelectionOfAnotherUsersCartItem() {
        User owner = createUser();
        User intruder = createUser();
        Product product = createProduct(5);
        Cart cart = createCart(owner.getId(), product.getId(), 1, 1);

        BusinessException error = businessError(() ->
                cartService.selectItem(cart.getId(), intruder.getId(), 0));

        assertThat(error.getCode()).isEqualTo("CART_ITEM_FORBIDDEN");
        assertThat(cartRepository.findById(cart.getId()).orElseThrow().getIsSelected()).isEqualTo(1);
    }

    @Test
    void rejectsQuantityUpdateOfAnotherUsersCartItem() {
        User owner = createUser();
        User intruder = createUser();
        Product product = createProduct(5);
        Cart cart = createCart(owner.getId(), product.getId(), 1, 1);

        BusinessException error = businessError(() ->
                cartService.updateQuantity(cart.getId(), intruder.getId(), 3));

        assertThat(error.getCode()).isEqualTo("CART_ITEM_FORBIDDEN");
        assertThat(cartRepository.findById(cart.getId()).orElseThrow().getQuantity()).isEqualTo(1);
    }

    @Test
    void rejectsRemovalOfAnotherUsersCartItem() {
        User owner = createUser();
        User intruder = createUser();
        Product product = createProduct(5);
        Cart cart = createCart(owner.getId(), product.getId(), 1, 1);

        BusinessException error = businessError(() ->
                cartService.removeFromCart(cart.getId(), intruder.getId()));

        assertThat(error.getCode()).isEqualTo("CART_ITEM_FORBIDDEN");
        assertThat(cartRepository.findById(cart.getId())).isPresent();
    }

    @Test
    void rejectsCumulativeAddQuantityAboveStock() {
        User user = createUser();
        Product product = createProduct(3);
        createCart(user.getId(), product.getId(), 2, 1);

        BusinessException error = businessError(() ->
                cartService.addToCart(user.getId(), product.getId(), 2));

        assertThat(error.getCode()).isEqualTo("STOCK_INSUFFICIENT");
        assertThat(cartRepository.findByUserIdAndProductId(user.getId(), product.getId())
                .orElseThrow().getQuantity()).isEqualTo(2);
    }

    @Test
    void rejectsQuantityAboveStock() {
        User user = createUser();
        Product product = createProduct(5);
        Cart cart = createCart(user.getId(), product.getId(), 2, 1);

        BusinessException error = businessError(() ->
                cartService.updateQuantity(cart.getId(), user.getId(), 6));

        assertThat(error.getCode()).isEqualTo("STOCK_INSUFFICIENT");
        assertThat(cartRepository.findById(cart.getId()).orElseThrow().getQuantity()).isEqualTo(2);
    }

    @Test
    void updatesQuantityWithinStock() {
        User user = createUser();
        Product product = createProduct(5);
        Cart cart = createCart(user.getId(), product.getId(), 2, 1);

        Cart updated = cartService.updateQuantity(cart.getId(), user.getId(), 5);

        assertThat(updated.getQuantity()).isEqualTo(5);
        assertThat(cartRepository.findById(cart.getId()).orElseThrow().getQuantity()).isEqualTo(5);
    }

    @Test
    void removesItemWhenQuantityIsZero() {
        User user = createUser();
        Product product = createProduct(5);
        Cart cart = createCart(user.getId(), product.getId(), 2, 1);

        Cart removed = cartService.updateQuantity(cart.getId(), user.getId(), 0);

        assertThat(removed).isNull();
        assertThat(cartRepository.findById(cart.getId())).isEmpty();
    }

    @Test
    void summarizesOnlySelectedItems() {
        User user = createUser();
        Product first = createProduct(5);
        Product second = createProduct(5);
        Product unselected = createProduct(5);
        createCart(user.getId(), first.getId(), 2, 1);
        createCart(user.getId(), second.getId(), 1, 1);
        createCart(user.getId(), unselected.getId(), 4, 0);

        java.util.Map<String, Object> summary = cartService.getCartSummary(user.getId());

        assertThat(summary.get("totalCount")).isEqualTo(3);
        assertThat(summary.get("selectedCount")).isEqualTo(2);
        assertThat(summary.get("totalQuantity")).isEqualTo(3);
        assertThat(summary.get("totalPoints")).isEqualTo(30);
    }

    @Test
    void clearsAllItemsForUser() {
        User user = createUser();
        Product first = createProduct(5);
        Product second = createProduct(5);
        createCart(user.getId(), first.getId(), 1, 1);
        createCart(user.getId(), second.getId(), 1, 0);

        cartService.clearCart(user.getId());

        assertThat(cartRepository.findByUserId(user.getId())).isEmpty();
    }

    @Test
    void addsNewItemAsSelected() {
        User user = createUser();
        Product product = createProduct(5);

        Cart cart = cartService.addToCart(user.getId(), product.getId(), 2);

        assertThat(cart.getId()).isNotNull();
        assertThat(cart.getQuantity()).isEqualTo(2);
        assertThat(cart.getIsSelected()).isEqualTo(1);
    }

    @Test
    void accumulatesQuantityWithinStock() {
        User user = createUser();
        Product product = createProduct(3);
        createCart(user.getId(), product.getId(), 1, 1);

        Cart cart = cartService.addToCart(user.getId(), product.getId(), 2);

        assertThat(cart.getQuantity()).isEqualTo(3);
    }

    @Test
    void rejectsNewItemAboveStock() {
        User user = createUser();
        Product product = createProduct(2);

        BusinessException error = businessError(() ->
                cartService.addToCart(user.getId(), product.getId(), 3));

        assertThat(error.getCode()).isEqualTo("STOCK_INSUFFICIENT");
        assertThat(cartRepository.findByUserId(user.getId())).isEmpty();
    }

    @Test
    void rejectsInvalidSelectionValue() {
        User user = createUser();
        Product product = createProduct(5);
        Cart cart = createCart(user.getId(), product.getId(), 1, 1);

        BusinessException error = businessError(() ->
                cartService.selectItem(cart.getId(), user.getId(), 2));

        assertThat(error.getCode()).isEqualTo("CART_SELECTION_INVALID");
        assertThat(cartRepository.findById(cart.getId()).orElseThrow().getIsSelected()).isEqualTo(1);
    }

    @Test
    void removesOwnItemSuccessfully() {
        User user = createUser();
        Product product = createProduct(5);
        Cart cart = createCart(user.getId(), product.getId(), 1, 1);

        cartService.removeFromCart(cart.getId(), user.getId());

        assertThat(cartRepository.findById(cart.getId())).isEmpty();
    }

    @Test
    void returnsCartItemsForOwner() {
        User user = createUser();
        Product first = createProduct(5);
        Product second = createProduct(5);
        createCart(user.getId(), first.getId(), 1, 1);
        createCart(user.getId(), second.getId(), 2, 0);

        assertThat(cartService.getCartByUserId(user.getId())).hasSize(2);
    }

    @Test
    void rejectsQuantityUpdateForMissingItem() {
        User user = createUser();

        BusinessException error = businessError(() ->
                cartService.updateQuantity(999999L, user.getId(), 2));

        assertThat(error.getCode()).isEqualTo("CART_404");
    }

    private BusinessException businessError(org.assertj.core.api.ThrowableAssert.ThrowingCallable action) {
        Throwable throwable = catchThrowable(action);
        assertThat(throwable).isInstanceOf(BusinessException.class);
        return (BusinessException) throwable;
    }

    private User createUser() {
        User user = new User();
        user.setUsername("cart-" + UUID.randomUUID());
        user.setPassword("password");
        user.setPoints(100);
        user.setRole("user");
        user.setStatus("active");
        return userRepository.save(user);
    }

    private Product createProduct(int stock) {
        Product product = new Product();
        product.setName("购物车商品-" + UUID.randomUUID());
        product.setPrice(0.0);
        product.setPointsRequired(10);
        product.setStock(stock);
        product.setSales(0);
        product.setStatus("active");
        product.setImages("[]");
        product.setSpecifications("{}");
        return productRepository.save(product);
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
