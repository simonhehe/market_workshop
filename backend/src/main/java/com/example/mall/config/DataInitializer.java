package com.example.mall.config;

import com.example.mall.entity.*;
import com.example.mall.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final AddressRepository addressRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) {
        // 初始化用户数据
        if (userRepository.count() == 0) {
            initializeUsers();
        }
        
        // 初始化分类数据
        if (categoryRepository.count() == 0) {
            initializeCategories();
        }
        
        // 初始化商品数据
        if (productRepository.count() == 0) {
            initializeProducts();
        }
        
        initializeAddresses();
    }
    
    private void initializeAddresses() {
        User user1 = userRepository.findByUsername("user1").orElse(null);
        if (user1 == null) {
            return;
        }
        
        List<Address> existing = addressRepository
                .findByUserIdAndDeletedAtIsNullOrderByIsDefaultDesc(user1.getId());
        if (!existing.isEmpty()) {
            return;
        }
        
        Address first = new Address();
        first.setUserId(user1.getId());
        first.setReceiver("张三");
        first.setPhone("13800138001");
        first.setProvince("浙江省");
        first.setCity("杭州市");
        first.setDistrict("西湖区");
        first.setDetailAddress("文一西路 1 号");
        first.setPostalCode("310000");
        first.setIsDefault(1);
        
        Address second = new Address();
        second.setUserId(user1.getId());
        second.setReceiver("李四");
        second.setPhone("13900139001");
        second.setProvince("上海市");
        second.setCity("上海市");
        second.setDistrict("浦东新区");
        second.setDetailAddress("世纪大道 100 号");
        second.setPostalCode("200000");
        second.setIsDefault(0);
        
        addressRepository.saveAll(Arrays.asList(first, second));
    }
    
    private void initializeUsers() {
        // 创建测试用户（使用明文密码，便于测试）
        User user1 = new User();
        user1.setUsername("user1");
        user1.setPassword("password1");
        user1.setEmail("user1@example.com");
        user1.setPhone("13800138001");
        user1.setPoints(100);
        user1.setRole("user");
        user1.setDepartment("技术部");
        user1.setPosition("软件工程师");
        user1.setStatus("active");
        userRepository.save(user1);
        
        User user2 = new User();
        user2.setUsername("user2");
        user2.setPassword("password2");
        user2.setEmail("user2@example.com");
        user2.setPhone("13800138002");
        user2.setPoints(200);
        user2.setRole("user");
        user2.setDepartment("市场部");
        user2.setPosition("市场专员");
        user2.setStatus("active");
        userRepository.save(user2);
        
        User user3 = new User();
        user3.setUsername("user3");
        user3.setPassword("password3");
        user3.setEmail("user3@example.com");
        user3.setPhone("13800138003");
        user3.setPoints(150);
        user3.setRole("user");
        user3.setDepartment("人事部");
        user3.setPosition("人事专员");
        user3.setStatus("active");
        userRepository.save(user3);
        
        // 创建管理员（使用加密密码）
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setEmail("admin@example.com");
        admin.setPhone("13800138000");
        admin.setPoints(0);
        admin.setRole("admin");
        admin.setDepartment("IT部");
        admin.setPosition("系统管理员");
        admin.setStatus("active");
        userRepository.save(admin);
    }
    
    private void initializeCategories() {
        List<Category> categories = Arrays.asList(
            createCategory("电子产品", "各类电子设备", 1),
            createCategory("生活用品", "日常生活用品", 2),
            createCategory("办公用品", "办公相关用品", 3),
            createCategory("运动健康", "运动健康相关产品", 4),
            createCategory("图书音像", "图书和音像制品", 5)
        );
        
        categoryRepository.saveAll(categories);
    }
    
    private Category createCategory(String name, String description, Integer sort) {
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        category.setSort(sort);
        category.setStatus("active");
        return category;
    }
    
    private void initializeProducts() {
        List<Category> categories = categoryRepository.findAll();
        
        if (categories.isEmpty()) {
            return;
        }
        
        Category electronics = categories.stream()
                .filter(c -> "电子产品".equals(c.getName()))
                .findFirst()
                .orElse(categories.get(0));
        
        Category daily = categories.stream()
                .filter(c -> "生活用品".equals(c.getName()))
                .findFirst()
                .orElse(categories.get(0));
        
        Category office = categories.stream()
                .filter(c -> "办公用品".equals(c.getName()))
                .findFirst()
                .orElse(categories.get(0));
        
        // 创建示例商品
        List<Product> products = Arrays.asList(
            createProduct("iPhone 15 Pro", "最新款iPhone，128GB存储", electronics.getId(), 5000, 100, 50),
            createProduct("MacBook Pro", "13英寸MacBook Pro，M2芯片", electronics.getId(), 8000, 150, 30),
            createProduct("AirPods Pro", "主动降噪无线耳机", electronics.getId(), 1500, 80, 100),
            createProduct("咖啡杯套装", "精美陶瓷咖啡杯，4件套", daily.getId(), 200, 20, 200),
            createProduct("保温杯", "不锈钢保温杯，500ml", daily.getId(), 150, 15, 150),
            createProduct("办公椅", "人体工学办公椅，舒适耐用", office.getId(), 800, 60, 50),
            createProduct("无线鼠标", "蓝牙无线鼠标，静音设计", office.getId(), 100, 10, 300),
            createProduct("键盘", "机械键盘，RGB背光", office.getId(), 300, 25, 200)
        );
        
        productRepository.saveAll(products);
    }
    
    private Product createProduct(String name, String description, Long categoryId, 
                                   Integer pointsRequired, Integer stock, Integer sales) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setCategoryId(categoryId);
        product.setPrice(0.0); // 积分商城，价格为0
        product.setPointsRequired(pointsRequired);
        product.setStock(stock);
        product.setSales(sales);
        product.setStatus("active");
        product.setImages("[]");
        product.setSpecifications("{}");
        return product;
    }
}
