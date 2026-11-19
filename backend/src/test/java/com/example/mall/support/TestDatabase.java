package com.example.mall.support;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Creates a unique temporary SQLite database per test class so tests never touch
 * backend/database/mall.db and never share data across classes.
 */
public final class TestDatabase {

    private TestDatabase() {
    }

    public static String newUniqueSqliteUrl() {
        try {
            Path dir = Files.createTempDirectory("mall-test-");
            Path dbFile = dir.resolve("mall.db");
            return "jdbc:sqlite:" + dbFile.toAbsolutePath().toString().replace('\\', '/');
        } catch (IOException e) {
            throw new UncheckedIOException("无法创建测试临时数据库目录", e);
        }
    }
}
