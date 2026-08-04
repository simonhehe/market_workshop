# 《AI Coding Workshop》课件 for PMSmart

> **项目**：`gdds-data-dashboard`
> 
> **时长**：2 \~ 2.5 小时（**讲练穿插，动手 ≥ 70 min**）
> 
> **讲师**：何德全

---

## 一、课程总流程（讲练一体）

每个 Phase **讲完即练；**。

| 时间 | 环节 | 合计 |
| --- | --- | --- |
| 0–10 min | 开场 + 定位 + 方法 | 10 min |
| 10–30 min | Phase 0：环境准备 | 20 min |
| 30–60 min | Phase 1：根据已知信息生成prd | 30 min |
| 60–120 min | Phase 2：创建看板变更 | 60 min |
| 120–150 min | Phase 3：项目反哺 | 30 min |
| 150–160 min | 复盘 + 答疑 | 10 min |

> **2h 压缩版**：
> 
> - Phase 0提前完成；
> - Phase 1或 Phase 3 选讲一个。

**环境前提**（课前完成）：已 clone 仓库；Node ≥20、pnpm、OpenSpec、JDK 21、Maven；Cursor 已登录。

---

## 二、开场 + 定位 + 方法

**今天要做什么**

- 案例：v7 原型 → 可演示 PC 看板（7 Tab + 下钻 + 钢筋真实数据）。
- 带走：四条 OpenSpec 对话指令 + superpowers核心技能 =\> \(看板 MVP + 0 严重bug\) + 熟悉框架方法 + 项目知识沉淀。

**工具分工**

| 工具 | 干什么 |
| --- | --- |
| Cursor Agent | 编程工具, 读项目、写代码、跑命令 |
| OpenSpec + superpowers | 通过框架所含命令与技能, 高质量交付\(SDD, TDD, BDD\) |
| Rules | 约束，减少跑偏 |
| skill | 提高执行质量 |

## 三、Phase 0：环境准备

### 1、主要工具更新

初始化Openspec环境：

```csharp
openspec init
```

确认Openspec环境就绪:

```bash
openspec --version
openspec list
```

- [ ]  能看到 OpenSpec 版本号
- [ ]  能列出 `add-pmsmart-bi-dashboard` 等 change

确认superpowers环境就绪:

手动输入 /br   cursor自动带出`/brainstorming` 

归档历史变更：

```bash
/opsx-archive @openspec/changes/add-floor-detail-endpoint @openspec/changes/add-rebar-optimize-apis 并将这两个变更中核心/重要的内容抽提到项目rules或skill
```

Tips：此处很重要

- 已经完成的变更及时归档，spec归位
- 抽提rules和skill

### 2、更新项目环境

1、创建变更

```bash
/opsx-propose 将@product/readme.md 的技术栈要求更新到当前项目配置/rule等文件中
- 更新项目相关配置信息
- 检查环境,并初始化
```

2、执行：

```bash
/opsx-apply
```

执行过程中，如果与AI有问答内容则按需答复

3、归档：

```bash
/opsx-archive 
将有效的信息整理成项目rule或skill
```

---

## 四、Phase 1：根据已知信息生成prd

### 1、当下有什么

技术栈要求

需求原型

### 2、是否需要PRD？

Q：为什么需要PRD，直接用原型不行吗？

A：

Q：谁对PRD负责？

A：

Q：PRD的规格要求，检查点是什么？

A：

### 3、探索需求，生成PRD

研发实现视角的prd

```
/opsx-explore 
目标: 实现@product/PMSmart项目数据分析决策看板-v7.html 效果. 前端效果100%一致, 未提供接口的数据可mock.
任务: 分析@product/PMSmart项目数据分析决策看板-v7.html ,帮我整理PRD文档(存到product/BI_prd.md)
- 后端
-- 根据@product/readme.md 及rule修改文档, 实现数据采集与入库(存入mysql数据),支持自动,手动更新数据
--- 考虑数据分析及加载性能, 数据库中除原始数据,考虑通过模型层和指标层抽提数据,减少页面加载时的调用耗时
--- 遵从DDD规范和Maven要求
- 前端: 
-- 整体布局, ,以及demo数据的整体实现风格,样式
-- 一级~末级页面效果
-- "钢筋优化率"卡片及下级页面,采用真实数据;其他使用测试数据
```

常见问题1

```
钢筋「创效金额」「实耗量」计算公式与数据来源
- 这是哪个卡片的指标? 先澄清后我再解答
3D 用 Three.js 还是 bimface-sdk
- bimface-sdk,看能否从原型中获取
默认演示 projectId
- 试试这个: curl --location 'http://localhost:8089/rebar-optimize/project-overview?projectId=800242952741376'
视频监控、导出 Excel 是否仅 UI 占位
- 占位
```

常见问题2

```makefile
Q1-A: 「创效金额」 =  (翻样量 - 预算量) * 钢筋市场价;其中"钢筋市场价"需要查询中国有影响力的钢筋价格网站信息
Q1-B:同Q1-A

Q2-A: 「实耗量」为用户填写, 本次先demo数据
Q2-B: 同源
```

常见问题3...

```makefile

```

### 4、review：使用superpowers进行对抗优化

```fortran
/brainstorming 根据本会话内容及product目录中的文件,分析@product/BI_prd.md 可以完善的内容
- 分析并给出优化建议,我确认后再逐一更新
```

根据分析结论，选择要在prd中完善的内容

最终生成**BI\_prd.md**

## 五、Phase 2：创建看板变更

### 1、生成变更

```bash
/opsx-propose @product/BI_prd.md
```

### 2、review变更文档

- 人肉review4项核心输出，识别要优化的内容…
- 使用技能review

```bash
/brainstorming 对@openspec/changes/add-pmsmart-bi-dashboard 进行对抗分析,提出改进建议
```

通过与AI互动，逐步完善变更内容

期间会因选择不同而产生分支，多轮沟通直到我们找不出问题为止

ps：至此意味着，还有问题也是我们无法驾驭的问题

- 常见AI对话一览

```
@openspec/changes/add-pmsmart-bi-dashboard/tasks.md 是否可以考虑先完成任务7(数仓与同步)?优点:
- 真实的钢筋优化率数据采集与清洗; 钢筋优化率相关看板使用真实数据
- 其他数据mock方案考虑后续的复用和功能迭代
```

```
确认下
1 按既定方案执行,最终钢筋优化率数据是真实数据吗?
2 其他mock数据的改造方法和改造成本高低?
```

```dockerfile
写入@openspec/changes/add-pmsmart-bi-dashboard/tasks.md 说明
```

### 3、生成详细执行计划

superpowers

```bash
/writing-plans @openspec/changes/add-pmsmart-bi-dashboard 根据变更内容,生成详细执行计划
```

目的：

1、核心实现逻辑检查

2、更小的工作颗粒与openspec的task形成支撑呼应

3、便于接下来使用superpowers技能执行代码生成和检查

### 4、生成代码

使用superpowers指令完成，如

/executing-plans 

/subagent-driven-development 

superpowers指令对opsx兼容较好…

### 5、联调&预览

1、tdd

```bash
/test-driven-development
```

数据库相关

```bash
实际连 MySQL 跑一遍 sync trigger 验证.
密码见@product/readme.md 提示
```

预览

```
全部能力已就绪的话,拉起预览
```

### 6、问题&修复

人肉自测，此时发现的多为业务未明确定义，前期设计遗漏的情况，需要耐心

问题修复（第一波）

```sql
/systematic-debugging 修复以下问题:
- <div> 使用3D 用 Three.js替代sdk, 先让我看到效果
- <div> 类似二级,三级窗口
-- 样式与原型不符,需修复,要求100%与原型一致且支持主题切换. 
-- 通过链接返回上层的效果实现有误(退回到了主页面), 需改为点及链接回到对应层级
以上, 如果涉及rule和skill,先刷新rules和skill, 再整体修复问题
```

问题修复（第二波）

```haskell
/systematic-debugging 
- <div> 与原型内容不一致,检查原因
- <div> 缺少左下角查看全部原始数据的按钮
- <div> 滚动条样式与界面不搭,优化成风格一致
同样的,先更新rule和skill,再修复所有同类项
```

### 7、验证&归档

openspec指令

```
/opsx:verify
/opsx:archive
```

superpowers指令

```bash
/requesting-code-review 
```

## 六、Phase 3：项目反哺

新建会话，继续……

以代码为第一事实，探索之前prd的不足

### 1、prd优化

```coffeescript
/brainstorming 对比实际代码和openspec变更, 以及@product/BI_prd.md @product/PMSmart项目数据分析决策看板-v7.html 识别@product/BI_prd.md 可以优化的内容
```

持续检查到满意为止……

```
生成V2版本的PRD文件,修正所有原prd未考虑到的问题
```

### 2、知识沉淀

```
/brainstorming 将优化prd会话中的有效信息整理到对应rule或skill
```

## 七、复盘 + 答疑

![](https://wdcdn.qpic.cn/MTY4ODg1MDA1NzAzMTQwMw_853464_peq2omZ3bb6oJY31_1782453563?w=1600&h=900)

| 问题 | 简答 |
| --- | --- |
|  |  |
|  |  |
|  |  |
|  |  |