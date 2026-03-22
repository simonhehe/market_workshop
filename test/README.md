# 自动化测试资产（测试团队）

与 **`backend/`、`frontend/` 开发目录分离**：本目录集中存放白盒、冒烟、UI 自动化脚本及说明，供测试团队维护；开发团队仅通过约定接口（如 `mvn test`、本目录脚本）协作。

## 目录

| 路径 | 说明 |
|------|------|
| **`backend/java/`** | JUnit / Spring 白盒测试源码（包名与主工程一致 `com.example.mall.*`） |
| **`backend/resources/`** | 测试资源（如 `application-test.yml`），按需添加 |
| **`smoke/`** | API 冒烟脚本（PowerShell / 可扩展 curl 封装）与步骤说明 |
| **`ui/`** | 前端 UI 自动化占位（Playwright / Cypress 等，由测试团队选型） |

## 运行方式

### 白盒（JUnit）

测试源码在 **`test/backend/java`**，由 **`backend/pom.xml`** 配置 `testSourceDirectory` 指向该路径。在仓库根目录执行：

```bash
cd backend
mvn test
```

或仅跑指定类：

```bash
mvn test -Dtest=OrderServiceTest
```

### 冒烟

见 **`smoke/README.md`** 与 `smoke-checkout-api.ps1`（需本机已启动后端，默认 `http://localhost:8080/api`）。

### UI

见 **`ui/README.md`**（待接入具体框架）。

## 与 OpenSpec 的对应

订单相关白盒矩阵：`openspec/changes/add-user-checkout-and-order-filter/whitebox-matrix.md`。
