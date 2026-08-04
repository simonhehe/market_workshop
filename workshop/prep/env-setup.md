# Phase 0：环境准备

> 课前完成；现场 180min 不含本段。风格对齐示例课件「Phase 0」。

---

## 1、主要工具更新

### 初始化 OpenSpec 环境

```bash
openspec init
```

确认 OpenSpec 环境就绪：

```bash
openspec --version
openspec list
```

- [ ] 能看到 OpenSpec 版本号  
- [ ] 能列出本场相关 change（干跑后常见 `order-checkout-hardening`；新班可能尚无，以 `openspec list` 为准）

### 确认 superpowers 环境就绪

手动输入 `/br`，Cursor 自动带出 `/brainstorming`。

- [ ] 能带出 `/brainstorming`  
- [ ] 能带出 `/opsx-propose`、`/opsx-apply`（或等价 skill）

### 初始化 GitNexus（安装 + 建索引）

**安装**（任选其一；推荐全局，避免 npm 11 + `npx` 安装崩溃）：

```bash
# 推荐：全局安装一次
npm i -g gitnexus

# 验证
gitnexus --version
```

若坚持用一次性拉取：

```bash
# 仓库根目录
npx gitnexus analyze
```

> npm 11.x 若 `npx` 报 `node.target is null`：改用全局安装，或  
> `pnpm --allow-build=@ladybugdb/core --allow-build=gitnexus --allow-build=tree-sitter dlx gitnexus@latest analyze`  
> （见 GitNexus [#1939](https://github.com/abhigyanpatwari/GitNexus/issues/1939)）

**对本仓建索引 / 刷新**（必须在仓库根目录执行）：

```bash
# 方式 A：全局 CLI
gitnexus analyze

# 方式 B：项目本地 runner（analyze 成功后会出现；gitignored）
node .gitnexus/run.cjs analyze

# 首次或 runner 缺失时，用 npx / 全局先跑一遍 analyze 生成 .gitnexus/run.cjs
npx gitnexus analyze
```

确认索引就绪：

```bash
gitnexus status
# 或
node .gitnexus/run.cjs status

gitnexus list
# MCP 侧：list_repos — 应出现本仓；名多为 market_workshop，以路径指向本目录为准
```

- [ ] `gitnexus` / `npx gitnexus` 可执行  
- [ ] `status` 显示本仓已索引（符号/关系计数非空）  
- [ ] `list_repos`（或 `gitnexus list`）路径指向本目录，不是无关仓  

Tips：索引陈旧或大改代码后重跑 `analyze`；MCP 读不到新索引时重启 Cursor。

### 归档历史变更（若有已完成 change）

```text
/opsx-archive @openspec/changes/<已完成-change>
并将核心/重要约定抽提到项目 rules 或 skill
```

Tips：此处很重要

- 已经完成的变更及时归档，spec 归位  
- 抽提 rules 和 skill  

---

## 2、更新项目环境

### 1、创建变更（对齐技术栈 / 课前环境）

```text
/opsx-propose 将本仓福利商城技术栈与 workshop 课前要求同步到项目配置 / rules：
- JDK 21+、Maven、Node 18+（建议 20）、后端 8080（context-path=/api）、前端 Vite 3000
- 确认 backend/database/ 目录须存在（SQLite）
- 检查环境并初始化（依赖安装、GitNexus 索引、validate-kit）
- 不扩展支付/优惠券等非目标
```

（若班次已有 `order-checkout-hardening` 且仅课前装环境，可跳过 propose，直接按下面命令手工初始化。）

### 2、执行

```bash
/opsx-apply
```

执行过程中，如果与 AI 有问答内容则按需答复。

**手工等价清单**（无 OpenSpec 变更时勾选）：

```bash
# 仓库根
New-Item -ItemType Directory -Force -Path backend\database | Out-Null   # bash: mkdir -p backend/database

cd backend && mvn -q -DskipTests package
cd ../frontend && npm install

cd .. && gitnexus analyze
node workshop/scripts/validate-kit.mjs
```

- [ ] `backend/database/` 存在  
- [ ] backend package / frontend `npm install` 成功  
- [ ] `validate-kit OK`  

端口约定：Backend `8080`（`/api`）；Frontend `3000`（非 5173）。

### 3、归档

```text
/opsx-archive
将有效的信息整理成项目 rule 或 skill
```

---

## 3、冒烟（可选但强烈建议）

```bash
# 终端 1
cd backend && mvn spring-boot:run

# 终端 2
cd frontend && npm run dev
```

浏览器打开 `http://localhost:3000`：

- [ ] 能登录种子账号（见 `DataInitializer`；干跑常用 `user1` / `password1`）  
- [ ] 购物车页可打开  

---

## 常见故障

| 症状 | 处理 |
|------|------|
| SQLite path does not exist | 创建 `backend/database/` |
| `npx gitnexus` 安装崩溃 | `npm i -g gitnexus` 后 `gitnexus analyze` |
| GitNexus 名对不上文件夹 | 用路径确认；MCP 名常为 `market_workshop` |
| MCP 仍是旧索引 | 重跑 analyze 后重启 Cursor |
| 前端连不上 API | 后端 8080 + Vite 代理 `/api` |
| validate-kit 失败 | 对照 `scripts/validate-kit.mjs` REQUIRED 清单 |

完整教练勾选见 [`dry-run-checklist.md`](dry-run-checklist.md)。
