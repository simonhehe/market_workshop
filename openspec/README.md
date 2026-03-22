# OpenSpec

本目录用于 [OpenSpec](https://github.com/Fission-AI/OpenSpec) 的变更提案与归档（`openspec/changes/`）。

## 与本项目 `spec/` 的关系

- **`openspec/`**：面向「单次变更」的提案、设计、任务与归档，由 OpenSpec CLI / Cursor 命令驱动。

二者可同时使用，职责不同。

## 在 Cursor 里使用

重启 IDE 后可用斜杠命令（若已配置）：

- `/opsx:propose` — 新建变更提案  
- `/opsx:apply` — 按任务实现  
- `/opsx:archive` — 归档并完成同步  

详见 `.cursor/commands/` 与 `.cursor/skills/` 下的 `opsx-*` / `openspec-*`。

## 命令行（需 Node.js ≥ 20.19）

```bash
npx @fission-ai/openspec@latest new change "<change-name>"
openspec list
openspec status --change "<change-name>" --json
```

全局安装（可选）：`npm install -g @fission-ai/openspec@latest`

## 更新 OpenSpec 集成文件

在项目根目录执行：

```bash
npx @fission-ai/openspec@latest update
```

## 目录说明

| 路径 | 说明 |
|------|------|
| `changes/` | 进行中的变更；完成后可归档到 `changes/archive/` |
| `changes/archive/` | 已归档变更 |

主规格能力文档若使用 OpenSpec 管理，通常位于 `openspec/specs/`（在创建/同步规格后会生成）。
