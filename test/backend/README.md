# `test/backend` — 白盒（Java）

- **源码路径**：`test/backend/java/com/example/mall/...`
- **编译与执行**：必须在 **`backend/`** 目录执行 Maven（`pom.xml` 已将 `testSourceDirectory` 指向此处）。
- **依赖**：测试代码依赖 `backend` 主源码中的 `com.example.mall` 类，无需复制业务代码。

请勿在 `backend/src/test` 下新增测试；统一在本目录维护。
