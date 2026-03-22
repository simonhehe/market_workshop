#Requires -Version 5.0
<#
.SYNOPSIS
    结算/订单筛选相关 API 冒烟（依赖 doc/smoke-checkout-order-filter.md）。
    测试团队维护；默认 API_BASE=http://localhost:8080/api
#>
$ErrorActionPreference = "Stop"
$ApiBase = if ($env:API_BASE) { $env:API_BASE.TrimEnd('/') } else { "http://localhost:8080/api" }

function Invoke-Json($Method, $Uri, $Headers = @{}, $Body = $null) {
    $params = @{ Uri = $Uri; Method = $Method; Headers = $Headers; UseBasicParsing = $true }
    if ($Body) { $params.ContentType = "application/json"; $params.Body = ($Body | ConvertTo-Json -Compress) }
    return Invoke-WebRequest @params
}

Write-Host "API_BASE=$ApiBase"

# S1 health
$r = Invoke-Json GET "$ApiBase/health"
if ($r.StatusCode -ne 200) { throw "health expected 200" }
$j = $r.Content | ConvertFrom-Json
if (-not $j.success) { throw "health success=false" }
Write-Host "OK health"

# S4 login
$loginBody = @{ username = "user1"; password = "password1" }
$lr = Invoke-Json POST "$ApiBase/auth/login" -Body $loginBody
if ($lr.StatusCode -ne 200) { throw "login expected 200" }
$lj = $lr.Content | ConvertFrom-Json
if (-not $lj.success) { throw "login success=false" }
$token = $lj.data.token
if (-not $token) { throw "no token in data.token" }
Write-Host "OK login"

$auth = @{ Authorization = "Bearer $token" }

# S5 cart
$cr = Invoke-Json GET "$ApiBase/cart" -Headers $auth
if ($cr.StatusCode -ne 200) { throw "cart expected 200" }
Write-Host "OK cart"

# S6 orders filtered
$or = Invoke-Json GET "$ApiBase/orders?status=pending" -Headers $auth
if ($or.StatusCode -ne 200) { throw "orders expected 200" }
Write-Host "OK orders?status=pending"

Write-Host "Smoke API passed."
