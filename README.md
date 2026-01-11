# 企业内部福利商城 - JDK 21 + Spring Boot 3.X + Vue 3 Demo

这是一个使用 JDK 21、Spring Boot 3.X 和 Vue 3 构建的前后端分离项目演示。

## 技术栈

### 后端
- **JDK**: 21
- **框架**: Spring Boot 3.2.0
- **构建工具**: Maven
- **数据库**: SQLite
- **端口**: 8080

### 前端
- **框架**: Vue 3
- **构建工具**: Vite
- **HTTP客户端**: Axios
- **端口**: 3000

## 快速开始

### 前置要求
- JDK 21 或更高版本
- Maven 3.6+
- Node.js 16+ 和 npm

### 启动后端服务

1. 进入后端目录：
```bash
cd backend
```

2. 编译并运行：
```bash
mvn clean install
mvn spring-boot:run
```

或者使用IDE直接运行 `MallApplication.java`

后端服务将在 `http://localhost:8080` 启动

### 启动前端服务

1. 进入前端目录：
```bash
cd frontend
```

2. 安装依赖：
```bash
npm install
```

3. 启动开发服务器：
```bash
npm run dev
```

前端服务将在 `http://localhost:3000` 启动

## 测试

1. 打开浏览器访问 `http://localhost:3000`
2. 点击"测试健康检查接口"或"测试API接口"按钮
3. 查看测试结果，确认前后端连通性

## API接口

### 健康检查
- `GET /api/health` - 服务健康检查
- `GET /api/health/test` - 前后端连通测试接口

## 项目结构

```
.
├── backend/              # Spring Boot 后端
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       │   └── com/example/mall/
│   │       └── resources/
│   │           └── application.yml
│   └── pom.xml
├── frontend/             # Vue 3 前端
│   ├── src/
│   │   ├── App.vue
│   │   └── main.js
│   ├── index.html
│   ├── package.json
│   └── vite.config.js
└── README.md
```

## 开发说明

- 后端API统一使用 `/api` 作为上下文路径
- 前端通过Vite代理将 `/api` 请求转发到后端
- 数据库文件存储在 `backend/database/mall.db`
