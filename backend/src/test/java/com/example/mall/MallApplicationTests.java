package com.example.mall;

import com.example.mall.support.TestDatabase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
@ActiveProfiles("test")
class MallApplicationTests {

    private static final String DB_URL = TestDatabase.newUniqueSqliteUrl();

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> DB_URL);
    }

    @Test
    void contextLoads() {
    }
}
