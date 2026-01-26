# Spring Boot 项目创建指南

本指南详细介绍如何从零开始创建一个适用于 CloudBase 部署的 Spring Boot 项目。

## 📋 目录导航

- [环境准备](#环境准备)
- [创建项目](#创建项目)
- [基础配置](#基础配置)
- [创建实体和控制器](#创建实体和控制器)
- [数据传输对象](#数据传输对象)
- [安装依赖](#安装依赖)
- [本地测试](#本地测试)
- [下一步](#下一步)

---

## 环境准备

### 1. 检查 Java 版本

```bash
# 检查 Java 版本（推荐 JDK 8+）
java -version
javac -version
```

### 2. 检查 Maven 版本

```bash
# 检查 Maven 版本
mvn -version
```

### 3. 安装开发工具（可选）

推荐使用以下 IDE：
- IntelliJ IDEA
- Eclipse
- Visual Studio Code (with Java Extension Pack)

## 创建项目

### 1. 使用 Spring Initializr 创建项目

访问 [Spring Initializr](https://start.spring.io/) 或使用命令行：

```bash
# 使用 Spring Boot CLI 创建项目
spring init --dependencies=web,actuator --name=cloudrun-springboot --package-name=com.tencent.cloudrun cloudrun-springboot

# 或者使用 Maven archetype
mvn archetype:generate \
  -DgroupId=com.tencent.cloudrun \
  -DartifactId=cloudrun-springboot \
  -DarchetypeArtifactId=maven-archetype-quickstart \
  -DinteractiveMode=false
```

### 2. 项目结构

```
cloudrun-springboot/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── tencent/
│   │   │           └── cloudrun/
│   │   │               ├── CloudrunApplication.java
│   │   │               ├── controller/
│   │   │               ├── entity/
│   │   │               └── dto/
│   │   └── resources/
│   │       └── application.properties
│   └── test/
├── pom.xml
└── target/
```

## 基础配置

### 1. 配置 `pom.xml`

创建或更新 `pom.xml` 文件：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.7.18</version>
        <relativePath/>
    </parent>
    
    <groupId>com.tencent.cloudrun</groupId>
    <artifactId>cloudrun-springboot</artifactId>
    <version>1.0-SNAPSHOT</version>
    <name>cloudrun-springboot</name>
    <description>Demo project for Spring Boot</description>
    
    <properties>
        <java.version>8</java.version>
        <maven.compiler.source>8</maven.compiler.source>
        <maven.compiler.target>8</maven.compiler.target>
    </properties>
    
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

### 2. 创建主应用类

创建 `src/main/java/com/tencent/cloudrun/CloudrunApplication.java`：

```java
package com.tencent.cloudrun;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CloudrunApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloudrunApplication.class, args);
    }
}
```

### 3. 配置应用属性

创建或更新 `src/main/resources/application.properties`：

```properties
# 服务器配置
server.port=${PORT:8080}
server.servlet.context-path=/

# 应用信息
spring.application.name=cloudrun-springboot
management.endpoints.web.exposure.include=health,info

# 日志配置
logging.level.com.tencent.cloudrun=INFO
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} - %msg%n

# 编码配置
server.servlet.encoding.charset=UTF-8
server.servlet.encoding.enabled=true
server.servlet.encoding.force=true
```

## 创建实体和控制器

### 1. 创建用户实体

创建 `src/main/java/com/tencent/cloudrun/entity/User.java`：

```java
package com.tencent.cloudrun.entity;

public class User {
    private Long id;
    private String name;
    private String email;

    public User() {}

    public User(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
```

### 2. 创建健康检查控制器

创建 `src/main/java/com/tencent/cloudrun/controller/HealthController.java`：

```java
package com.tencent.cloudrun.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "healthy");
        response.put("framework", "Spring Boot");
        response.put("version", "2.7.18");
        response.put("deployment", "CloudBase");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("java_version", System.getProperty("java.version"));
        return response;
    }
}
```

### 3. 创建根路径控制器

更新主应用类或创建单独的控制器：

```java
package com.tencent.cloudrun.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public Map<String, Object> home() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Hello from Spring Boot on CloudBase!");
        response.put("framework", "Spring Boot");
        response.put("version", "2.7.18");
        return response;
    }
}
```

## 数据传输对象

### 1. 创建 API 响应类

创建 `src/main/java/com/tencent/cloudrun/dto/ApiResponse.java`：

```java
package com.tencent.cloudrun.dto;

public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;

    public ApiResponse() {}

    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "操作成功", data);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
```

### 2. 创建用户控制器

创建 `src/main/java/com/tencent/cloudrun/controller/UserController.java`：

```java
package com.tencent.cloudrun.controller;

import com.tencent.cloudrun.dto.ApiResponse;
import com.tencent.cloudrun.entity.User;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final List<User> users = new ArrayList<>();
    private final AtomicLong counter = new AtomicLong();

    public UserController() {
        // 初始化测试数据
        users.add(new User(counter.incrementAndGet(), "张三", "zhangsan@example.com"));
        users.add(new User(counter.incrementAndGet(), "李四", "lisi@example.com"));
        users.add(new User(counter.incrementAndGet(), "王五", "wangwu@example.com"));
    }

    @GetMapping
    public ApiResponse<List<User>> getAllUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {
        
        int startIndex = (page - 1) * limit;
        int endIndex = Math.min(startIndex + limit, users.size());
        
        if (startIndex >= users.size()) {
            return ApiResponse.success(new ArrayList<>());
        }
        
        List<User> paginatedUsers = users.subList(startIndex, endIndex);
        return ApiResponse.success(paginatedUsers);
    }

    @GetMapping("/{id}")
    public ApiResponse<User> getUserById(@PathVariable Long id) {
        User user = users.stream()
                .filter(u -> u.getId().equals(id))
                .findFirst()
                .orElse(null);
        
        if (user == null) {
            return ApiResponse.error("用户不存在");
        }
        
        return ApiResponse.success(user);
    }

    @PostMapping
    public ApiResponse<User> createUser(@RequestBody User user) {
        if (user.getName() == null || user.getEmail() == null) {
            return ApiResponse.error("姓名和邮箱不能为空");
        }
        
        user.setId(counter.incrementAndGet());
        users.add(user);
        
        return ApiResponse.success(user);
    }
}
```

## 安装依赖

### 1. 基础依赖

项目的基础依赖已在 `pom.xml` 中配置：

- `spring-boot-starter-web`：Web 开发基础包
- `spring-boot-starter-actuator`：监控和管理端点
- `spring-boot-starter-test`：测试框架

### 2. 可选依赖

根据需要添加其他依赖：

```xml
<!-- 数据库支持 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <scope>runtime</scope>
</dependency>

<!-- 数据验证 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>

<!-- JSON 处理 -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>
```

### 3. 构建项目

```bash
# 编译项目
mvn compile

# 运行测试
mvn test

# 打包项目
mvn package

# 清理并重新构建
mvn clean package
```

## 本地测试

### 1. 启动应用

```bash
# 使用 Maven 启动
mvn spring-boot:run

# 或者运行打包后的 JAR
java -jar target/cloudrun-springboot-1.0-SNAPSHOT.jar

# 应用启动后，访问以下地址测试：
# http://localhost:8080/          - 首页
# http://localhost:8080/health    - 健康检查
```

### 2. API 测试

```bash
# 测试基础接口
curl http://localhost:8080/
# 返回: {"message": "Hello from Spring Boot on CloudBase!", "framework": "Spring Boot", "version": "2.7.18"}

curl http://localhost:8080/health
# 返回: {"status": "healthy", "framework": "Spring Boot", "version": "2.7.18", ...}

# 测试用户 API
# 获取用户列表
curl http://localhost:8080/api/users
curl "http://localhost:8080/api/users?page=1&limit=2"

# 创建用户
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name": "测试用户", "email": "test@example.com"}'

# 获取单个用户
curl http://localhost:8080/api/users/1
```

### 3. 监控端点

Spring Boot Actuator 提供了多个监控端点：

```bash
# 健康检查
curl http://localhost:8080/actuator/health

# 应用信息
curl http://localhost:8080/actuator/info

# 查看所有端点
curl http://localhost:8080/actuator
```

## 下一步

项目创建完成后，根据您的部署需求选择相应的部署指南：

### 🚀 部署选择

| 部署方式 | 适用场景 | 详细指南 |
|----------|----------|----------|
| **HTTP 云函数** | 轻量级 API、间歇性访问 | [HTTP 云函数部署指南](./http-function.md) |
| **云托管** | 企业应用、高并发、持续运行 | [云托管部署指南](./cloud-run.md) |

### 📚 相关文档

- [返回主文档](../README.md)
- [HTTP 云函数部署指南](./http-function.md)
- [云托管部署指南](./cloud-run.md)

### 🔧 进一步开发

1. **数据库集成**：集成 Spring Data JPA 和 MySQL/PostgreSQL
2. **用户认证**：添加 Spring Security 和 JWT 认证
3. **API 文档**：使用 Swagger/OpenAPI 生成 API 文档
4. **配置管理**：使用 Spring Cloud Config 管理配置
5. **缓存**：集成 Redis 缓存
6. **消息队列**：集成 RabbitMQ 或 Kafka
7. **测试**：编写单元测试和集成测试
8. **微服务**：使用 Spring Cloud 构建微服务架构

---

**提示**：Spring Boot 提供了强大的自动配置和丰富的生态系统，非常适合构建企业级 Java 应用。