# 快速部署 Spring Boot 应用

## 📋 目录导航

- [部署方式对比](#部署方式对比)
- [前置条件](#前置条件)
- [第一步：创建 Spring Boot 应用](#第一步创建-spring-boot-应用)
- [第二步：添加 API 接口](#第二步添加-api-接口)
- [第三步：本地测试](#第三步本地测试)
- [第四步：准备部署文件](#第四步准备部署文件)
- [第五步：项目结构](#第五步项目结构)
- [第六步：部署应用](#第六步部署应用)
- [第七步：访问您的应用](#第七步访问您的应用)
- [常见问题](#常见问题)
- [最佳实践](#最佳实践)
- [进阶功能](#进阶功能)

---

[Spring Boot](https://spring.io/projects/spring-boot) 是基于 Spring 框架的快速开发脚手架，它简化了 Spring 应用的创建和部署过程。Spring Boot 提供了自动配置、内嵌服务器、生产就绪的特性监控等功能，让开发者能够快速构建独立的、生产级别的 Spring 应用程序。

本指南介绍如何在 CloudBase 上部署 Spring Boot 应用程序，支持两种部署方式：

- **HTTP 云函数**：适合轻量级应用和 API 服务，按请求计费，冷启动快
- **云托管**：适合企业级应用，支持更复杂的部署需求，容器化部署

## 部署方式对比

| 特性 | HTTP 云函数 | 云托管 |
|------|------------|--------|
| **计费方式** | 按请求次数和执行时间 | 按资源使用量（CPU/内存） |
| **启动方式** | 冷启动，按需启动 | 持续运行 |
| **适用场景** | API 服务、轻量级应用 | 企业级应用、微服务架构 |
| **部署文件** | 需要 `scf_bootstrap` 启动脚本 | 需要 `Dockerfile` 容器配置 |
| **端口要求** | 固定 9000 端口（通过环境变量） | 可自定义端口（默认 8080） |
| **扩缩容** | 自动按请求扩缩 | 支持自动扩缩容配置 |
| **JVM 优化** | 受限于冷启动 | 支持完整 JVM 调优 |

## 前置条件

在开始之前，请确保您已经：

- 安装了 [JDK 8](https://www.oracle.com/java/technologies/downloads/) 或更高版本
- 安装了 [Maven 3.6+](https://maven.apache.org/download.cgi) 或 [Gradle](https://gradle.org/install/)
- 拥有腾讯云账号并开通了 CloudBase 服务
- 了解基本的 Java 和 Spring Boot 开发知识

## 第一步：创建 Spring Boot 应用

> 💡 **提示**：如果您已经有一个 Spring Boot 应用，可以跳过此步骤。

### 使用 Spring Initializr 创建项目

1. 访问 [start.spring.io](https://start.spring.io/)
2. 选择以下配置：

```
Project: Maven
Language: Java
Spring Boot: 2.7.18 (或最新稳定版)
Project Metadata:
  - Group: com.tencent
  - Artifact: cloudrun-springboot
  - Name: cloudrun-springboot
  - Description: Demo project for Spring Boot
  - Package name: com.tencent.cloudrun
  - Packaging: Jar
  - Java: 8
Dependencies:
  - Spring Web
  - Spring Boot Actuator (健康检查)
```

3. 点击 **GENERATE** 下载项目压缩包
4. 解压到本地目录

### 使用 Maven 命令创建（可选）

```bash
mvn archetype:generate \
  -DgroupId=com.tencent.cloudrun \
  -DartifactId=cloudrun-springboot \
  -DarchetypeArtifactId=maven-archetype-quickstart \
  -DinteractiveMode=false

cd cloudrun-springboot
```

### 配置 pom.xml 文件

如果使用 Maven 命令创建项目，需要手动配置 `pom.xml` 文件以支持 Spring Boot：

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

### 创建主应用类

如果使用 Maven 命令创建项目，还需要创建 Spring Boot 主应用类。

在 `src/main/java/com/tencent/cloudrun` 目录下创建 `CloudrunApplication.java`：

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

### 本地测试应用

进入项目目录并启动应用：

```bash
cd cloudrun-springboot
mvn spring-boot:run
```

打开浏览器访问 `http://localhost:8080`，您应该能看到 Spring Boot 默认页面。

## 第二步：添加 API 接口

让我们创建一些 RESTful API 来演示 Spring Boot 的功能。

### 创建用户实体类

在 `src/main/java/com/tencent/cloudrun/entity` 目录下创建 `User.java`：

```java
package com.tencent.cloudrun.entity;

public class User {
    private Long id;
    private String name;
    private String email;

    // 构造函数
    public User() {}

    public User(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    // Getter 和 Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
```

### 创建响应包装类

在 `src/main/java/com/tencent/cloudrun/dto` 目录下创建 `ApiResponse.java`：

```java
package com.tencent.cloudrun.dto;

public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;

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

    // Getter 和 Setter
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
}
```

### 创建用户控制器

在 `src/main/java/com/tencent/cloudrun/controller` 目录下创建 `UserController.java`：

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

### 创建健康检查控制器

在 `src/main/java/com/tencent/cloudrun/controller` 目录下创建 `HealthController.java`：

```java
package com.tencent.cloudrun.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "healthy");
        health.put("timestamp", LocalDateTime.now());
        health.put("framework", "Spring Boot");
        health.put("version", getClass().getPackage().getImplementationVersion());
        health.put("java_version", System.getProperty("java.version"));
        
        return health;
    }

    @GetMapping("/")
    public Map<String, String> home() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "欢迎使用 Spring Boot CloudBase 应用！");
        response.put("status", "running");
        
        return response;
    }
}
```

### 配置应用属性

编辑 `src/main/resources/application.properties`：

```properties
# 服务器配置
server.port=${PORT:8080}
server.servlet.context-path=/

# 应用配置
spring.application.name=cloudrun-springboot
management.endpoints.web.exposure.include=health,info

# 日志配置
logging.level.com.tencent.cloudrun=INFO
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} - %msg%n
```

## 第三步：本地测试

### 启动应用

```bash
mvn spring-boot:run
```

### 测试 API 接口

```bash
# 测试健康检查
curl http://localhost:8080/health

# 测试首页
curl http://localhost:8080/

# 测试用户列表
curl http://localhost:8080/api/users

# 测试分页
curl "http://localhost:8080/api/users?page=1&limit=2"

# 测试获取单个用户
curl http://localhost:8080/api/users/1

# 测试创建用户
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"新用户","email":"newuser@example.com"}'
```

## 第四步：准备部署文件

根据您选择的部署方式，需要准备不同的配置文件：

### 📋 选择部署方式

<details>
<summary><strong>🔥 HTTP 云函数部署配置</strong></summary>

HTTP 云函数需要 `scf_bootstrap` 启动脚本和特定的端口配置。

#### 1. 修改应用配置

编辑 `src/main/resources/application.properties`，确保云函数环境使用 9000 端口：

```properties
# 云函数端口配置（默认 8080，云函数环境使用 9000）
server.port=${PORT:8080}
server.servlet.context-path=/

# 应用配置
spring.application.name=cloudrun-springboot
management.endpoints.web.exposure.include=health,info
```

> ⚠️ **重要提示**：CloudBase HTTP 云函数要求应用监听 9000 端口，通过环境变量 `PORT=9000` 来控制。

#### 2. 创建启动脚本

创建 `scf_bootstrap` 文件（无扩展名）：

```bash
#!/bin/bash
export PORT=9000
export JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC"
java $JAVA_OPTS -jar *.jar
```

> 💡 **说明**：
> - 使用 `*.jar` 通配符来匹配当前目录下的 JAR 包
> - CloudBase 云函数会将上传的文件解压到工作目录，JAR 包直接位于当前目录下

为启动脚本添加执行权限：

```bash
chmod +x scf_bootstrap
```

#### 3. 构建 JAR 包

```bash
mvn clean package -DskipTests
```

#### 4. 项目结构

```
cloudrun-springboot/
├── src/
│   └── main/
│       ├── java/
│       └── resources/
├── target/
│   └── *.jar  # 构建产物（如：cloudrun-springboot-1.0-SNAPSHOT.jar）
├── pom.xml
├── scf_bootstrap                  # 🔑 云函数启动脚本
└── README.md
```

> 💡 **说明**：
> - `scf_bootstrap` 是 CloudBase 云函数的启动脚本
> - 云函数会将上传的文件解压到工作目录，JAR 包直接位于当前目录下
> - 设置 `PORT=9000` 环境变量确保应用监听正确端口
> - 配置 JVM 参数优化内存使用

</details>

<details>
<summary><strong>🐳 云托管部署配置</strong></summary>

云托管使用 Docker 容器化部署，需要 `Dockerfile` 配置文件。

#### 1. 创建 Maven 配置文件

创建 `settings.xml` 文件（用于加速依赖下载）：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 
          http://maven.apache.org/xsd/settings-1.0.0.xsd">
  <mirrors>
    <mirror>
      <id>tencent</id>
      <mirrorOf>central</mirrorOf>
      <name>Tencent Maven Mirror</name>
      <url>https://mirrors.cloud.tencent.com/nexus/repository/maven-public/</url>
    </mirror>
  </mirrors>
</settings>
```

#### 2. 创建 Dockerfile

创建 `Dockerfile` 文件：

```dockerfile
# 构建阶段
FROM maven:3.9.4-openjdk-8-slim as build

# 指定构建过程中的工作目录
WORKDIR /app

# 将src目录下所有文件，拷贝到工作目录中src目录下
COPY src /app/src

# 将pom.xml和settings.xml文件，拷贝到工作目录下
COPY settings.xml pom.xml /app/

# 执行代码编译命令
# 自定义settings.xml, 选用国内镜像源以提高下载速度
RUN mvn -s /app/settings.xml -f /app/pom.xml clean package -DskipTests

# 运行阶段
FROM alpine:3.18

# 安装依赖包，选用国内镜像源以提高下载速度
RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.tencent.com/g' /etc/apk/repositories \
    && apk add --update --no-cache openjdk8-jre \
    && rm -f /var/cache/apk/*

# 容器默认时区为UTC，如需使用上海时间请启用以下时区设置命令
RUN apk add tzdata && cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo Asia/Shanghai > /etc/timezone

# 使用 HTTPS 协议访问容器云调用证书安装
RUN apk add ca-certificates

# 指定运行时的工作目录
WORKDIR /app

# 将构建产物jar包拷贝到运行时目录中
COPY --from=build /app/target/*.jar app.jar

# 暴露端口
EXPOSE 8080

# 设置环境变量
ENV PORT=8080
ENV JAVA_OPTS="-Xmx1g -Xms512m -XX:+UseG1GC"

# 执行启动命令
CMD ["sh", "-c", "java $JAVA_OPTS -Dserver.port=$PORT -jar app.jar"]
```

#### 3. 创建 .dockerignore 文件

创建 `.dockerignore` 文件以优化构建性能：

```
target/
.mvn/
*.iml
.idea/
.git/
.gitignore
README.md
.env
scf_bootstrap
```

#### 4. 配置应用端口

编辑 `src/main/resources/application.properties`：

```properties
# 云托管端口配置（支持环境变量，默认 8080）
server.port=${PORT:8080}
server.servlet.context-path=/

# 应用配置
spring.application.name=cloudrun-springboot
management.endpoints.web.exposure.include=health,info
```

#### 5. 项目结构

```
cloudrun-springboot/
├── src/
│   └── main/
│       ├── java/
│       └── resources/
├── target/                        # 构建产物目录
├── pom.xml                       # Maven 配置
├── settings.xml                  # 🔑 Maven 镜像配置
├── Dockerfile                    # 🔑 容器配置文件
├── .dockerignore                 # Docker 忽略文件
└── README.md
```

> 💡 **说明**：
> - 云托管支持自定义端口，默认使用 80 端口
> - 使用多阶段构建优化镜像大小
> - 支持完整的 JVM 调优和监控

</details>

## 第五步：项目结构

确保您的项目目录结构包含必要的文件。根据部署方式的不同，某些文件是可选的：

```
cloudrun-springboot/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/tencent/cloudrun/
│       │       ├── CloudrunApplication.java
│       │       ├── controller/
│       │       │   ├── UserController.java
│       │       │   └── HealthController.java
│       │       ├── entity/
│       │       │   └── User.java
│       │       └── dto/
│       │           └── ApiResponse.java
│       └── resources/
│           └── application.properties
├── target/                        # 构建产物目录
├── pom.xml                       # Maven 配置
├── settings.xml                  # Maven 镜像配置 (仅云托管需要)
├── scf_bootstrap                 # HTTP 云函数启动脚本 (仅云函数需要)
├── Dockerfile                    # 云托管容器配置 (仅云托管需要)
├── .dockerignore                 # Docker 忽略文件 (仅云托管需要)
└── README.md
```

## 第六步：部署应用

选择您需要的部署方式：

### 🚀 部署方式选择

<details>
<summary><strong>🔥 部署到 HTTP 云函数</strong></summary>

#### 通过控制台部署

1. 登录 [CloudBase 控制台](https://console.cloud.tencent.com/tcb)
2. 选择您的环境，进入「云函数」页面
3. 点击「新建云函数」
4. 选择「HTTP 云函数」
5. 填写函数名称（如：`springboot-app`）
7. 选择运行时：**Java 8**（或其他支持的版本）
7. 提交方法选择：**本地上传代码包**
8. 上传构建后的 JAR 包和 `scf_bootstrap` 文件
9. **自动安装依赖**：关闭（Java 应用无需此选项）
10. 点击「创建」按钮等待部署完成

#### 通过 CLI 部署(敬请期待)

#### 打包部署

如果需要手动打包：

```bash
# 构建应用
mvn clean package -DskipTests

# 创建部署包（使用 -j 参数避免目录结构）
zip -j springboot-app.zip target/*.jar scf_bootstrap
```

</details>

<details>
<summary><strong>🐳 部署到云托管</strong></summary>

#### 通过控制台部署

1. 登录 [CloudBase 控制台](https://console.cloud.tencent.com/tcb)
2. 选择您的环境，进入「云托管」页面
3. 点击「新建服务」
4. 填写服务名称（如：`springboot-service`）
5. 选择「本地代码」上传方式
6. 上传包含 `Dockerfile` 的项目目录
7. 配置服务参数：
   - **端口**：8080（或您在应用中配置的端口）
   - **CPU**：0.5 核
   - **内存**：1 GB
   - **实例数量**：1-10（根据需求调整）
8. 点击「创建」按钮等待部署完成

#### 通过 CLI 部署

```bash
# 安装 CloudBase CLI
npm install -g @cloudbase/cli

# 登录
tcb login

# 部署云托管服务
tcb run deploy --port 8080
```

#### 模板部署（快速开始）

1. 登录 [腾讯云托管控制台](https://tcb.cloud.tencent.com/dev#/platform-run/service/create?type=image)
2. 点击「通过模板部署」，选择 **Spring Boot 模板**
3. 输入自定义服务名称，点击部署
4. 等待部署完成后，点击左上角箭头，返回到服务详情页
5. 点击概述，获取默认域名并访问

</details>

## 第七步：访问您的应用

### HTTP 云函数访问

部署成功后，您可以参考[通过 HTTP 访问云函数](https://docs.cloudbase.net/service/access-cloud-function)设置自定义域名访问 HTTP 云函数。

访问地址格式：`https://your-function-url/`

### 云托管访问

云托管部署成功后，系统会自动分配访问地址。您也可以绑定自定义域名。

访问地址格式：`https://your-service-url/`

### 测试接口

无论使用哪种部署方式，您都可以测试以下接口：

- **根路径**：`/` - Spring Boot 欢迎页面
- **健康检查**：`/health` - 查看应用状态
- **用户列表**：`/api/users` - 获取用户列表
- **用户详情**：`/api/users/1` - 获取特定用户
- **创建用户**：`POST /api/users` - 创建新用户

### 示例请求

```bash
# 健康检查
curl https://your-app-url/health

# 获取用户列表
curl https://your-app-url/api/users

# 分页查询
curl "https://your-app-url/api/users?page=1&limit=2"

# 创建新用户
curl -X POST https://your-app-url/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"测试用户","email":"test@example.com"}'
```

## 常见问题

### ❓ 问题分类

<details>
<summary><strong>🔥 HTTP 云函数相关问题</strong></summary>

#### Q: 为什么使用 `*.jar` 而不是具体的 JAR 包名称？
A: 使用通配符有以下优势：
- 避免因项目版本号变化导致的路径错误
- 简化部署流程，无需每次修改启动脚本
- 确保能够找到构建后的 JAR 包，无论其具体名称如何
- 云函数会将文件解压到当前工作目录，使用相对路径即可

#### Q: 如果当前目录下没有 JAR 包怎么办？
A: 检查以下几点：
1. 确保使用 `zip -j` 参数打包，避免目录结构问题
2. 验证 `mvn clean package` 命令执行成功
3. 检查 `target` 目录下是否生成了 JAR 包
4. 确保打包时包含了 JAR 包文件

#### Q: 为什么使用 `zip -j` 参数？
A: `-j` 参数的作用是"junk paths"（忽略路径）：
- 使用 `zip -j` 会忽略文件的目录结构，只保存文件本身
- 这样 `target/cloudrun-springboot-1.0-SNAPSHOT.jar` 在解压后会直接变成 `cloudrun-springboot-1.0-SNAPSHOT.jar`
- 避免了在云函数中出现 `/opt/target/` 的多层目录问题
- 简化了打包过程，无需手动复制和删除文件

#### Q: `scf_bootstrap` 中的 `/opt` 目录是什么？
A: `/opt` 是 CloudBase 云函数运行时环境的标准工作目录：
- 当您上传代码包到云函数时，所有文件会被解压到 `/opt` 目录
- JAR 包、配置文件等都会位于 `/opt` 目录下
- 这是云函数平台的标准约定，无需手动创建
- 启动脚本中必须使用绝对路径 `/opt/your-jar-file.jar` 来引用 JAR 包

#### Q: 为什么 HTTP 云函数必须使用 9000 端口？
A: CloudBase HTTP 云函数要求应用监听 9000 端口，这是平台的标准配置。应用通过环境变量 `PORT=9000` 来控制端口，本地开发时默认使用 8080 端口。

#### Q: Spring Boot 应用冷启动时间较长怎么办？
A: 可以通过以下方式优化：
- 启用 `spring.main.lazy-initialization=true`
- 减少自动配置的组件
- 使用 GraalVM 原生镜像（实验性）
- 合理设置 JVM 参数

#### Q: 运行 `mvn spring-boot:run` 时提示 "No plugin found for prefix 'spring-boot'" 怎么办？
A: 这是因为项目缺少 Spring Boot Maven 插件配置。解决方案：
1. 确保 `pom.xml` 中包含 `spring-boot-starter-parent` 作为父项目
2. 在 `<build><plugins>` 部分添加 `spring-boot-maven-plugin`
3. 参考文档中的完整 `pom.xml` 配置示例

#### Q: 如何处理 JAR 包过大的问题？
A: 
- 使用 `spring-boot-maven-plugin` 的 `repackage` 目标
- 排除不必要的依赖
- 使用 `provided` 作用域排除运行时不需要的依赖

#### Q: 云函数中如何查看 Spring Boot 日志？
A: 在 CloudBase 控制台的云函数页面，点击函数名称进入详情页查看运行日志。

</details>

<details>
<summary><strong>🐳 云托管相关问题</strong></summary>

#### Q: 云托管支持哪些端口？
A: 云托管支持自定义端口，Spring Boot 应用默认使用 8080 端口，也可以根据需要配置其他端口。

#### Q: 如何优化 Docker 镜像构建速度？
A: 
- 使用多阶段构建
- 配置 Maven 本地仓库缓存
- 使用国内镜像源
- 合理设置 `.dockerignore`

#### Q: Spring Boot 应用内存使用过高怎么办？
A: 
- 调整 JVM 堆内存参数 `-Xmx` 和 `-Xms`
- 使用 G1 垃圾收集器 `-XX:+UseG1GC`
- 监控应用内存使用情况

#### Q: 如何配置 Spring Boot 应用的健康检查？
A: 
- 添加 `spring-boot-starter-actuator` 依赖
- 配置 `management.endpoints.web.exposure.include=health`
- 在云托管中配置健康检查路径为 `/actuator/health`

</details>

<details>
<summary><strong>🔧 通用问题</strong></summary>

#### Q: 如何处理静态资源？
A: Spring Boot 默认会处理 `src/main/resources/static` 目录下的静态资源。

#### Q: 如何查看应用日志？
A: 
- **HTTP 云函数**：在 CloudBase 控制台的云函数页面查看运行日志
- **云托管**：在云托管服务详情页面查看实例日志

#### Q: 支持哪些 Java 版本？
A: CloudBase 支持 Java 8、11、17 等版本，建议使用 Java 8 LTS 版本以获得更好的兼容性。

#### Q: 如何处理跨域问题？
A: 可以使用 `@CrossOrigin` 注解或配置全局 CORS 设置。

#### Q: 两种部署方式如何选择？
A: 
- **选择 HTTP 云函数**：轻量级 API 服务、间歇性访问、成本敏感
- **选择云托管**：企业级应用、持续运行、需要完整 JVM 特性

</details>

## 最佳实践

### 1. 环境变量管理

在 `application.properties` 中使用环境变量：

```properties
# 数据库配置
spring.datasource.url=${DB_URL:jdbc:h2:mem:testdb}
spring.datasource.username=${DB_USERNAME:sa}
spring.datasource.password=${DB_PASSWORD:}

# 应用配置
app.jwt.secret=${JWT_SECRET:mySecret}
app.upload.path=${UPLOAD_PATH:/tmp/uploads}
```

### 2. 端口配置策略

为了同时支持两种部署方式，建议使用动态端口配置：

```properties
# 支持多环境端口配置（默认 8080，云函数环境通过 PORT=9000 控制）
server.port=${PORT:8080}

# 云函数环境检测
spring.profiles.active=${SPRING_PROFILES_ACTIVE:default}
```

### 3. 添加全局异常处理

创建 `GlobalExceptionHandler.java`：

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ApiResponse<String> handleException(Exception e) {
        return ApiResponse.error("系统错误：" + e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ApiResponse<String> handleIllegalArgument(IllegalArgumentException e) {
        return ApiResponse.error("参数错误：" + e.getMessage());
    }
}
```

### 4. 配置 CORS 支持

创建 `WebConfig.java`：

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*");
    }
}
```

### 5. JVM 参数优化

针对不同部署方式的 JVM 优化：

```bash
# HTTP 云函数（内存受限）
JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# 云托管（更多资源）
JAVA_OPTS="-Xmx1g -Xms512m -XX:+UseG1GC"
```

### 6. 健康检查增强

自定义健康检查指示器：

```java
@Component
public class CustomHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        // 检查应用状态
        boolean isHealthy = checkApplicationHealth();
        
        if (isHealthy) {
            return Health.up()
                    .withDetail("status", "应用运行正常")
                    .withDetail("timestamp", LocalDateTime.now())
                    .build();
        } else {
            return Health.down()
                    .withDetail("status", "应用异常")
                    .build();
        }
    }
    
    private boolean checkApplicationHealth() {
        // 实现具体的健康检查逻辑
        return true;
    }
}
```

### 7. 部署前检查清单

<details>
<summary><strong>🔥 HTTP 云函数部署检查</strong></summary>

#### HTTP 云函数部署检查

- [ ] `scf_bootstrap` 文件存在且有执行权限
- [ ] 端口配置为 9000
- [ ] JAR 包构建成功且可执行
- [ ] JVM 参数适合云函数环境
- [ ] 启用懒加载优化冷启动
- [ ] 测试本地启动是否正常

</details>

<details>
<summary><strong>🐳 云托管部署检查</strong></summary>

#### 云托管部署检查

- [ ] `Dockerfile` 文件存在且配置正确
- [ ] `settings.xml` 文件配置合理
- [ ] `.dockerignore` 文件排除不必要文件
- [ ] 端口配置灵活（支持环境变量）
- [ ] 多阶段构建优化镜像大小
- [ ] 本地 Docker 构建测试通过

</details>

## 进阶功能

### 数据库集成

添加数据库支持：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
</dependency>
```

### 安全认证

添加 Spring Security：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt</artifactId>
    <version>0.9.1</version>
</dependency>
```

### API 文档

使用 SpringDoc OpenAPI：

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.2.0</version>
</dependency>
```

### 缓存支持

添加 Redis 缓存：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```
