package com.example.mall.support;

import com.example.mall.entity.Address;
import com.example.mall.entity.Cart;
import com.example.mall.entity.Product;
import com.example.mall.entity.User;
import com.example.mall.repository.AddressRepository;
import com.example.mall.repository.CartRepository;
import com.example.mall.repository.ProductRepository;
import com.example.mall.repository.UserRepository;
import com.example.mall.util.JwtUtil;
import org.springframework.stereotype.Component;

@Component
public class TestDataHelper {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final AddressRepository addressRepository;
    private final JwtUtil jwtUtil;

    public TestDataHelper(UserRepository userRepository,
                          ProductRepository productRepository,
                          CartRepository cartRepository,
                          AddressRepository addressRepository,
                          JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.cartRepository = cartRepository;
        this.addressRepository = addressRepository;
        this.jwtUtil = jwtUtil;
    }

    public User createUser(String username, int points) {
        User user = new User();
        user.setUsername(username);
        user.setPassword("pass");
        user.setPoints(points);
        user.setRole("user");
        user.setStatus("active");
        return userRepository.save(user);
    }

    public Product createProduct(String name, int points, int stock) {
        Product product = new Product();
        product.setName(name);
        product.setPrice(99.0);
        product.setPointsRequired(points);
        product.setStock(stock);
        product.setStatus("active");
        product.setSales(0);
        return productRepository.save(product);
    }

    public Cart addCartItem(Long userId, Long productId, int qty, int selected) {
        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setProductId(productId);
        cart.setQuantity(qty);
        cart.setIsSelected(selected);
        return cartRepository.save(cart);
    }

    public Address createAddress(Long userId, boolean isDefault) {
        Address address = new Address();
        address.setUserId(userId);
        address.setReceiver("张三");
        address.setPhone("13800000000");
        address.setProvince("北京市");
        address.setCity("北京市");
        address.setDistrict("朝阳区");
        address.setDetailAddress("测试路1号");
        address.setIsDefault(isDefault ? 1 : 0);
        return addressRepository.save(address);
    }

    public String bearerToken(User user) {
        return "Bearer " + jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
    }
}
