# API 冒烟（测试团队）

脚本与 **`doc/smoke-checkout-order-filter.md`** 步骤一致；此处提供可重复执行的 PowerShell 示例。

## 前置

- 后端已启动：`http://localhost:8080/api`
- Windows PowerShell 5+

## 运行

```powershell
cd test/smoke
.\smoke-checkout-api.ps1
```

可通过环境变量覆盖基址：

```powershell
$env:API_BASE = "http://127.0.0.1:8080/api"
.\smoke-checkout-api.ps1
```

失败时脚本以非零退出码结束（便于 CI）。
