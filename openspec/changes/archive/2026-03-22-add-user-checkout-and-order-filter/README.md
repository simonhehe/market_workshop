# Change: `add-user-checkout-and-order-filter`

**合并/发布简报**：见 **`APPLY_BRIEF.md`**（需求验收结论 + 质量活动与结果）。

## 执行顺序（建议）

1. **`proposal.md`** — 背景、范围、风险摘要  
2. **`design.md`** — API/前端/验收 §5；**§8 白盒执行入口**  
3. **`tasks.md`** — Phase A–D 实施清单；**Phase E** 白盒补测勾选  
4. **`whitebox-matrix.md`** — 白盒分支、风险、WB 编号矩阵与 T1～T5 待办  

## 与全局配置

`openspec/config.yaml` → `changes.add_user_checkout_and_order_filter` 指向本目录及 `whitebox-matrix.md`，便于工具或检索触发。

## 测试资产位置（开发与测试团队分离）

- **白盒 JUnit**：`test/backend/java`（`mvn test` 在 `backend/` 下执行）
- **冒烟脚本**：`test/smoke/`
- **UI 自动化占位**：`test/ui/`

## Smoke（黑盒主路径）

仓库内：`doc/smoke-checkout-order-filter.md`（与本变更白盒矩阵互补）。
