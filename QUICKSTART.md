# 快速启动指南

## 环境准备

### 检查JDK版本
```bash
java -version
```
确保显示 JDK 21 或更高版本

### 检查Maven版本
```bash
mvn -version
```
确保 Maven 3.6+

### 检查Node.js版本
```bash
node -version
```
确保 Node.js 16+

## 启动步骤

### 1. 启动后端服务

打开终端，进入后端目录：
```bash
cd backend
```

首次运行需要编译依赖（可能需要几分钟）：
```bash
mvn clean install
```

启动Spring Boot服务：
```bash
mvn spring-boot:run
```

看到以下信息表示启动成功：
```
Started MallApplication in X.XXX seconds
```

后端服务运行在：`http://localhost:8080`

### 2. 启动前端服务

打开**新的终端窗口**，进入前端目录：
```bash
cd frontend
```

安装依赖（首次运行）：
```bash
npm install
```

启动开发服务器：
```bash
npm run dev
```

看到以下信息表示启动成功：
```
  VITE v5.x.x  ready in xxx ms

  ➜  Local:   http://localhost:3000/
  ➜  Network: use --host to expose
```

前端服务运行在：`http://localhost:3000`

### 3. 测试连通性

1. 打开浏览器访问：`http://localhost:3000`
2. 点击页面上的"测试健康检查接口"按钮
3. 如果看到"✓ 成功"和返回的数据，说明前后端连通正常

## 常见问题

### 后端启动失败

1. **端口被占用**：修改 `backend/src/main/resources/application.yml` 中的 `server.port`
2. **JDK版本不对**：确保使用 JDK 21
3. **Maven依赖下载失败**：检查网络连接，或配置Maven镜像

### 前端启动失败

1. **端口被占用**：修改 `frontend/vite.config.js` 中的 `server.port`
2. **npm install 失败**：删除 `node_modules` 文件夹后重新运行 `npm install`
3. **无法连接后端**：确保后端服务已启动，检查 `vite.config.js` 中的代理配置

### 前后端无法连通

1. 确保后端服务已启动（访问 `http://localhost:8080/api/health` 应该有响应）
2. 确保前端服务已启动
3. 检查浏览器控制台是否有错误信息
4. 检查后端CORS配置是否正确

## API测试

### 直接测试后端API

使用curl或Postman测试：

```bash
# 健康检查
curl http://localhost:8080/api/health

# 测试接口
curl http://localhost:8080/api/health/test
```

预期返回JSON格式的响应。
