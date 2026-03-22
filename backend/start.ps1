# 后端服务启动脚本
$ErrorActionPreference = "Stop"

Write-Host "=== 启动后端服务 ===" -ForegroundColor Green

# 设置工作目录
Set-Location $PSScriptRoot

# 检查Java版本
Write-Host "检查Java环境..." -ForegroundColor Yellow
$java17 = "C:\Program Files\Eclipse Adoptium\jdk-17.0.16.8-hotspot"
$java11 = "C:\Program Files\Microsoft\jdk-11.0.28.6-hotspot"

if (Test-Path $java17) {
    $env:JAVA_HOME = $java17
    Write-Host "使用 Java 17: $java17" -ForegroundColor Green
} elseif (Test-Path $java11) {
    $env:JAVA_HOME = $java11
    Write-Host "使用 Java 11: $java11 (注意: 项目需要Java 21)" -ForegroundColor Yellow
} else {
    Write-Host "错误: 未找到Java安装" -ForegroundColor Red
    exit 1
}

# 检查Maven Wrapper
if (-not (Test-Path "mvnw.cmd")) {
    Write-Host "错误: 未找到Maven Wrapper (mvnw.cmd)" -ForegroundColor Red
    exit 1
}

# 检查数据库目录
if (-not (Test-Path "database")) {
    New-Item -ItemType Directory -Path "database" | Out-Null
    Write-Host "创建数据库目录" -ForegroundColor Yellow
}

Write-Host "`n启动Spring Boot应用..." -ForegroundColor Green
Write-Host "服务将在 http://localhost:8080/api 启动" -ForegroundColor Cyan
Write-Host "按 Ctrl+C 停止服务`n" -ForegroundColor Yellow

# 启动服务
# 确保JAVA_HOME在PATH中，这样mvnw.cmd可以找到java
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

# 使用cmd来运行mvnw.cmd，确保环境变量正确传递
$mvnwPath = Join-Path $PSScriptRoot "mvnw.cmd"
cmd /c "`"$mvnwPath`" spring-boot:run"
