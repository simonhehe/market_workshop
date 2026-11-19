package com.example.mall.repository;

import com.example.mall.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProductRepositorySearchTest {

    private static final Path DB_FILE;

    static {
        try {
            Path dir = Files.createDirectories(Path.of("target", "test-db"));
            DB_FILE = Files.createTempFile(dir, "search-", ".db");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @DynamicPropertySource
    static void sqliteDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:sqlite:" + DB_FILE.toAbsolutePath());
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    private static final Long ELECTRONICS = 101L;
    private static final Long DAILY = 202L;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProductRepository productRepository;

    private Long iphoneId;
    private Long mouseId;
    private Long caseId;

    @BeforeEach
    void setUp() {
        iphoneId = persistProduct("iPhone 15 Pro", "最新款手机", ELECTRONICS, "active", 2, null);
        mouseId = persistProduct("无线鼠标", "蓝牙静音设计", ELECTRONICS, "active", 1, null);
        caseId = persistProduct("保护壳", "适配 iPhone Pro 机型", DAILY, "active", 3, null);
        persistProduct("iPhone 展示机", "旧款", ELECTRONICS, "inactive", 4, null);
        persistProduct("iPhone 翻新机", "旧款", ELECTRONICS, "active", 5, LocalDateTime.now());
        entityManager.flush();
    }

    @Test
    void keywordMatchesNameOrDescriptionAndSkipsInactiveAndDeleted() {
        List<Product> result = productRepository.searchProducts("Pro", null, null);

        assertThat(result).extracting(Product::getId).containsExactly(iphoneId, caseId);
    }

    @Test
    void keywordWithoutMatchReturnsEmpty() {
        List<Product> result = productRepository.searchProducts("不存在", null, null);

        assertThat(result).isEmpty();
    }

    @Test
    void categoryAndKeywordCombine() {
        List<Product> result = productRepository.searchProducts("Pro", ELECTRONICS, null);

        assertThat(result).extracting(Product::getId).containsExactly(iphoneId);
    }

    @Test
    void categoryWithoutKeywordReturnsAllActiveInCategory() {
        List<Product> result = productRepository.searchProducts(null, ELECTRONICS, null);

        assertThat(result).extracting(Product::getId).containsExactly(mouseId, iphoneId);
    }

    @Test
    void noFilterKeepsDefaultSortOrder() {
        List<Product> result = productRepository.searchProducts(null, null, null);

        assertThat(result).extracting(Product::getId).containsExactly(mouseId, iphoneId, caseId);
    }

    private Long persistProduct(String name, String description, Long categoryId,
                                String status, int sort, LocalDateTime deletedAt) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setCategoryId(categoryId);
        product.setPrice(0.0);
        product.setPointsRequired(100);
        product.setSales(0);
        product.setStatus(status);
        product.setSort(sort);
        product.setDeletedAt(deletedAt);
        return entityManager.persistAndGetId(product, Long.class);
    }
}