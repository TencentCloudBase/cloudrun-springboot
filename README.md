# 快速部署 Spring Boot 应用

一个完整的 Spring Boot 应用模板，支持快速部署到 CloudBase 平台。

## 🚀 快速开始

### 前置条件

- [Java 8](https://www.oracle.com/java/technologies/downloads/) 或更高版本
- [Maven 3.6+](https://maven.apache.org/download.cgi) 构建工具
- 腾讯云账号并开通了 CloudBase 服务
- 基本的 Java 和 Spring Boot 开发知识

### 创建应用

> **📋 详细指南**：完整的项目创建步骤请参考 [Spring Boot 项目创建指南](./docs/project-setup.md)

```bash
# 快速创建（基础步骤）
mvn archetype:generate -DgroupId=com.tencent.cloudrun \
  -DartifactId=cloudrun-springboot \
  -DarchetypeArtifactId=maven-archetype-quickstart \
  -DinteractiveMode=false
cd cloudrun-springboot
```

### 本地测试

```bash
# 编译项目
mvn clean compile

# 启动开发服务器
mvn spring-boot:run

# 访问应用
open http://localhost:8080
```

## 📦 项目结构

```
cloudrun-springboot/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/tencent/cloudrun/
│       │       ├── CloudrunApplication.java    # 主应用类
│       │       ├── controller/                 # 控制器层
│       │       │   ├── HealthController.java
│       │       │   └── UserController.java
│       │       ├── entity/                     # 实体类
│       │       │   └── User.java
│       │       └── dto/                        # 数据传输对象
│       │           └── ApiResponse.java
│       └── resources/
│           └── application.properties          # 应用配置
├── target/                                     # 编译输出目录
├── pom.xml                                     # Maven 配置文件
├── scf_bootstrap                              # HTTP 云函数启动脚本
├── Dockerfile                                 # 云托管容器配置
└── settings.xml                               # Maven 设置文件
```

## 🎯 部署方式

### 部署方式对比

| 特性 | HTTP 云函数 | 云托管 |
|------|------------|--------|
| **计费方式** | 按请求次数和执行时间 | 按资源使用量（CPU/内存） |
| **启动方式** | 冷启动，按需启动 | 持续运行 |
| **适用场景** | API 服务、轻量级应用 | 企业级应用、复杂 Web 应用 |
| **端口要求** | 固定 9000 端口 | 可自定义端口（默认 8080） |
| **扩缩容** | 自动按请求扩缩 | 支持自动扩缩容配置 |
| **Java 环境** | 预配置 Java 运行时 | 完全自定义 Java 环境 |

### 选择部署方式

- **选择 HTTP 云函数**：轻量级 API 服务、间歇性访问、成本敏感
- **选择云托管**：企业级应用、复杂 Web 应用、需要更多控制权

## 📚 详细部署指南

### 🔥 HTTP 云函数部署

适合轻量级应用和 API 服务，按请求计费，冷启动快。

**快速部署步骤：**
1. 编译打包 JAR 文件
2. 创建 `scf_bootstrap` 启动脚本
3. 通过 CloudBase 控制台上传部署

[📖 查看详细的 HTTP 云函数部署指南](./docs/http-function.md)

### 🐳 云托管部署

适合企业级应用，支持更复杂的部署需求，容器化部署。

**快速部署步骤：**
1. 创建 `Dockerfile` 容器配置
2. 配置 `.dockerignore` 文件
3. 通过 CloudBase 控制台或 CLI 部署

[📖 查看详细的云托管部署指南](./docs/cloud-run.md)

## 🔧 API 接口

本模板包含以下 RESTful API 接口：

### 基础接口
```bash
GET /                        # 欢迎页面
GET /health                  # 健康检查
```

### 用户管理
```bash
GET /api/users               # 获取用户列表（支持分页）
GET /api/users/{id}          # 获取单个用户
POST /api/users              # 创建用户
```

### 示例请求

```bash
# 健康检查
curl https://your-app-url/health

# 获取用户列表（分页）
curl "https://your-app-url/api/users?page=1&limit=5"

# 创建新用户
curl -X POST https://your-app-url/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"测试用户","email":"test@example.com"}'
```

## ❓ 常见问题

### 端口配置
- **HTTP 云函数**：必须使用 9000 端口
- **云托管**：推荐使用 8080 端口，支持自定义

### 文件要求
- **HTTP 云函数**：需要编译后的 JAR 文件和 `scf_bootstrap` 启动脚本
- **云托管**：需要 `Dockerfile` 和 `.dockerignore`

### 数据存储
- 当前使用内存存储（重启后数据丢失）
- 生产环境建议集成数据库（PostgreSQL、MySQL 等）

### 如何选择部署方式？
- **轻量级应用**：选择 HTTP 云函数
- **企业级应用**：选择云托管
- **成本敏感**：选择 HTTP 云函数
- **需要持续运行**：选择云托管

## 🛠️ 开发工具

### 推荐的开发依赖

```xml
<!-- 核心 Web 框架 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- 数据库支持 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- 数据验证 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>

<!-- 监控端点 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

### 环境变量配置

在 `application.properties` 中配置：

```properties
# 服务器配置
server.port=${PORT:8080}

# 数据库配置（可选）
spring.datasource.url=${DATABASE_URL:jdbc:h2:mem:testdb}
spring.datasource.username=${DB_USERNAME:sa}
spring.datasource.password=${DB_PASSWORD:}

# JPA 配置
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

## 📖 进阶功能

- **依赖注入**：强大的 IoC 容器
- **AOP 支持**：面向切面编程
- **数据访问**：Spring Data JPA 集成
- **安全框架**：Spring Security 支持
- **缓存抽象**：多种缓存实现支持
- **消息队列**：Spring Cloud Stream 支持
- **微服务**：Spring Cloud 生态系统
- **监控指标**：Spring Boot Actuator

## 🔗 相关链接

### 📚 项目文档
- [Spring Boot 项目创建指南](./docs/project-setup.md) - 从零开始创建项目
- [HTTP 云函数部署指南](./docs/http-function.md) - 云函数部署详细步骤
- [云托管部署指南](./docs/cloud-run.md) - 云托管部署详细步骤

### 🌐 官方文档
- [CloudBase 官方文档](https://docs.cloudbase.net/)
- [Spring Boot 官方文档](https://spring.io/projects/spring-boot)
- [Java 官方文档](https://docs.oracle.com/javase/)

## 📄 许可证

本项目采用 MIT 许可证。详情请查看 [LICENSE](./LICENSE) 文件。

---

**需要帮助？** 

- 查看 [HTTP 云函数部署指南](./docs/http-function.md)
- 查看 [云托管部署指南](./docs/cloud-run.md)
- 访问 [CloudBase 官方文档](https://docs.cloudbase.net/)