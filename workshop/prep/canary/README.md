# Track 0 · 金丝雀（旁路）

## 用途

- 课前教练可在分支 `canary/order-checkout` 预跑完整参考实现（可选）。  
- 现场学员**只读对照**；超时/工具故障时按设计启用「金丝雀对照续跑」。  
- **禁止**用金丝雀结果代替人闸签字。

## 约定

| 项 | 值 |
|----|-----|
| 参考分支名 | `canary/order-checkout` |
| 样例报告 | `sample-quality-report.md` |
| 对照方式 | diff 关键文件 + 对比质量报告表格 |

## 教练课前检查

- [ ] 本仓 GitNexus 已索引  
- [ ] `validate-kit` OK  
- [ ] 样例报告与 `coverage-scope.md` 白名单一致  
- [ ] 若尚无参考分支：本场仍可开课，compass 允许 degraded，课件注明「无参考实现」
