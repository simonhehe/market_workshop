# 项目文档索引（`doc/`）

仓库内 **Markdown 说明类文档** 按主题放在本目录；**工具链专属**说明（如 `.cursor/` 下 OpenSpec 指令）仍保留在原位置。

## 目录规则（后续请遵守）

| 位置 | 用途 |
|------|------|
| **`doc/baseline/`** | 产品/设计/领域**基线**：需求、总体设计、订单域基线等，适合作为评审与变更对照的「主版本」。 |
| **`doc/analysis/`** | **分析、梳理、清单**类材料：初始化摘要、链路分析、技术附录、缺口清单、测试建议等。 |
| **`doc/`**（本目录根下） | **暂无法归入** `baseline` / `analysis` 的文档；新文档若不确定分类，**先放此处**，再在评审后移入子目录。 |
| **仓库根目录** | 优先只保留 **`README.md`**（项目入口说明）；其它说明性 `.md` 应迁入 `doc/`。 |
| **`openspec/`** | OpenSpec 配置与变更产物（如 `config.yaml`、`changes/`），**不**与 `doc/` 混放。 |
| **`.cursor/`** | Cursor / OpenSpec **commands & skills**，不迁入 `doc/`。 |

## 当前文件一览

### `doc/baseline/`

| 文件 | 说明 |
|------|------|
| [baseline-requirements.md](baseline/baseline-requirements.md) | 基线需求 |
| [baseline-design.md](baseline/baseline-design.md) | 基线设计 |
| [order-domain-baseline.md](baseline/order-domain-baseline.md) | 订单域基线 |

### `doc/analysis/`

| 文件 | 说明 |
|------|------|
| [项目初始化摘要.md](analysis/项目初始化摘要.md) | 系统全貌 |
| [订单链路基线分析.md](analysis/订单链路基线分析.md) | 订单链（业务向） |
| [单链路基线分析_技术.md](analysis/单链路基线分析_技术.md) | 订单/技术附录（图、契约、测试矩阵等） |
| [文档缺口清单.md](analysis/文档缺口清单.md) | 文档完备性与缺口 |
| [测试基线建议.md](analysis/测试基线建议.md) | 测试与质量基线建议 |

## 文档间引用

文内互相引用时请使用 **相对路径**（例如从 `baseline/` 指向 `../analysis/单链路基线分析_技术.md`），避免移动目录后链接失效。

## AI / Cursor 约定

- **规则**（硬性）：`.cursor/rules/` 下 `*.mdc` — 正文为**中文**（YAML 元数据键名仍为英文）；证据与 N/A、`doc/` 目录规范、后端订单/购物车相关编辑提示。
- **技能**（流程）：`.cursor/skills/workshop-baseline-sync/SKILL.md` — **中文**说明探索或变更后如何同步基线、`openspec/config.yaml` 与交叉引用。
