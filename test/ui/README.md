# UI 自动化（测试团队）

本目录预留用于 **Playwright / Cypress / Selenium** 等前端 E2E 或冒烟脚本，与开发目录 `frontend/` 分离。

## 建议约定

- 配置文件与用例仅放在 `test/ui/` 下（或通过子目录 `test/ui/playwright/` 等）。
- 本地开发服务默认 `http://localhost:3000`，通过 Vite 代理访问 `/api`。
- 与 **`doc/smoke-checkout-order-filter.md`** §B 人工步骤对齐的可自动化条目可逐步迁移至此。

## 运行

（待接入具体工具后补充，例如 `npx playwright test`。）
