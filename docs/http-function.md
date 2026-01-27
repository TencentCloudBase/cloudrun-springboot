# Spring Boot HTTP 云函数部署指南

本指南详细介绍如何将 Spring Boot 应用部署到 CloudBase HTTP 云函数。

> **📋 前置要求**：如果您还没有创建 Spring Boot 项目，请先阅读 [Spring Boot 项目创建指南](./project-setup.md)。

## 📋 目录导航

- [部署特性](#部署特性)
- [准备部署文件](#准备部署文件)
- [项目结构](#项目结构)
- [部署步骤](#部署步骤)
- [访问应用](#访问应用)
- [常见问题](#常见问题)
- [最佳实践](#最佳实践)
- [性能优化](#性能优化)

---

## 部署特性

HTTP 云函数适合以下场景：

- **轻量级 API**：RESTful API 服务、微服务
- **间歇性访问**：不需要持续运行的应用
- **成本敏感**：按请求次数和执行时间计费
- **快速部署**：无需容器化配置

### 技术特点

| 特性 | 说明 |
|------|------|
| **计费方式** | 按请求次数和执行时间 |
| **启动方式** | 冷启动，按需启动 |
| **端口要求** | 固定 9000 端口 |
| **扩缩容** | 自动按请求扩缩 |
| **Java 环境** | 预配置 Java 运行时 |

## 准备部署文件

### 1. 创建启动脚本

创建 `scf_bootstrap` 文件（无扩展名）：

```bash
#!/bin/bash
export PORT=9000
export JAVA_OPTS="-Xms256m -Xmx512m"
cd /var/user
java $JAVA_OPTS -jar target/cloudrun-springboot-1.0-SNAPSHOT.jar
```

为启动脚本添加执行权限：

```bash
chmod +x scf_bootstrap
```

### 2. 优化 application.properties

确保 `src/main/resources/application.properties` 支持云函数环境：

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

# 云函数优化配置
spring.main.lazy-initialization=true
server.tomcat.threads.max=10
server.tomcat.threads.min-spare=2
```

### 3. 构建项目

```bash
# 清理并构建项目
mvn clean package

# 确保 JAR 文件存在
ls -la target/cloudrun-springboot-1.0-SNAPSHOT.jar
```

### 4. 依赖管理

确保 `pom.xml` 包含必要依赖：

```xml
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
```

## 项目结构

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
├── target/
│   └── cloudrun-springboot-1.0-SNAPSHOT.jar  # 🔑 构建后的 JAR 文件
├── pom.xml                                    # Maven 配置文件
└── scf_bootstrap                             # 🔑 云函数启动脚本
```

> 💡 **说明**：
> - `scf_bootstrap` 是 CloudBase 云函数的启动脚本
> - 设置 `PORT=9000` 环境变量确保应用监听云函数要求的端口
> - **重要**：HTTP 云函数部署时需要包含构建后的 JAR 文件
> - 使用云函数运行时环境的 Java 解释器启动应用

## 部署步骤

### 通过控制台部署

> 💡 **注意**：
> - java 程序启动较慢，需要调整函数执行超时时间，默认为 3s, 建议调整 10s


1. 登录 [CloudBase 控制台](https://console.cloud.tencent.com/tcb)
2. 选择您的环境，进入「云函数」页面
3. 点击「新建云函数」
4. 选择「HTTP 云函数」
5. 填写函数名称（如：`cloudrun-springboot-app`）
6. 选择运行时：**Java 8**（或其他支持的版本）
7. 提交方法选择：**本地上传文件夹**
8. 函数代码选择 `cloudrun-springboot` 目录进行上传
9. **自动安装依赖**：关闭此选项（Java 项目使用预构建的 JAR）
10. 点击「创建」按钮等待部署完成

### 通过 CLI 部署(敬请期待)

### 打包部署

如果需要手动打包：

```bash
# 构建项目
mvn clean package

# 创建部署包（包含 JAR 文件和启动脚本）
zip -r cloudrun-springboot-app.zip . -x ".git/*" "src/*" "*.log" "Dockerfile" ".dockerignore" "target/classes/*" "target/test-classes/*"
```

## 访问应用

### 获取访问地址

部署成功后，您可以参考[通过 HTTP 访问云函数](https://docs.cloudbase.net/service/access-cloud-function)设置自定义域名访问 HTTP 云函数。

访问地址格式：`https://your-function-url/`

### 测试接口

- **根路径**：`/` - Spring Boot 欢迎页面
- **健康检查**：`/health` - 查看应用状态
- **监控端点**：`/actuator/health` - Spring Boot Actuator 健康检查
- **用户列表**：`/api/users` - 获取用户列表
- **用户详情**：`/api/users/1` - 获取特定用户
- **创建用户**：`POST /api/users` - 创建新用户

### 示例请求

```bash
# 健康检查
curl https://your-function-url/health

# Spring Boot Actuator 健康检查
curl https://your-function-url/actuator/health

# 获取用户列表
curl https://your-function-url/api/users

# 分页查询
curl "https://your-function-url/api/users?page=1&limit=2"

# 创建新用户
curl -X POST https://your-function-url/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"测试用户","email":"test@example.com"}'
```

## 常见问题

### Q: 为什么 HTTP 云函数必须使用 9000 端口？
A: CloudBase HTTP 云函数要求应用监听 9000 端口，这是平台的标准配置。通过在 `scf_bootstrap` 中设置 `PORT=9000` 环境变量来控制端口，本地开发时默认使用 8080 端口。

### Q: Spring Boot 应用启动时间较长怎么办？
A: 可以通过以下方式优化启动时间：
1. 启用懒加载：`spring.main.lazy-initialization=true`
2. 减少自动配置：排除不需要的自动配置类
3. 优化 JVM 参数：设置合适的堆内存大小
4. 使用 Spring Boot 的 CDS（Class Data Sharing）功能

### Q: 如何处理 CORS 跨域问题？
A: 在 Spring Boot 中配置 CORS：

```java
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOriginPattern("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        return new CorsFilter(source);
    }
}
```

### Q: JAR 文件和依赖如何处理？
A: HTTP 云函数部署时需要包含构建后的 JAR 文件。Spring Boot 的 Fat JAR 包含了所有依赖，无需额外处理依赖问题。

### Q: 如何查看云函数日志？
A: 在 CloudBase 控制台的云函数页面，点击函数名称进入详情页查看运行日志。

### Q: 云函数支持哪些 Java 版本？
A: CloudBase 支持 Java 8、Java 11 等版本，建议使用 Java 8 以获得最佳兼容性。

## 最佳实践

### 1. 环境变量管理

```properties
# application.properties
server.port=${PORT:8080}
spring.profiles.active=${SPRING_PROFILES_ACTIVE:prod}

# 数据库配置
spring.datasource.url=${DATABASE_URL:jdbc:h2:mem:testdb}
spring.datasource.username=${DATABASE_USERNAME:sa}
spring.datasource.password=${DATABASE_PASSWORD:}

# 日志级别
logging.level.com.tencent.cloudrun=${LOG_LEVEL:INFO}
```

### 2. 优化启动脚本

增强 `scf_bootstrap` 脚本：

```bash
#!/bin/bash
export PORT=9000
export SPRING_PROFILES_ACTIVE=prod
export JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# 检查 JAR 文件
if [ ! -f "target/cloudrun-springboot-1.0-SNAPSHOT.jar" ]; then
    echo "JAR file not found"
    exit 1
fi

# 启动应用
cd /var/user
java $JAVA_OPTS -jar target/cloudrun-springboot-1.0-SNAPSHOT.jar
```

### 3. 全局异常处理

```java
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleException(Exception e) {
        logger.error("Unhandled exception: ", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("内部服务器错误"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(IllegalArgumentException e) {
        logger.warn("Invalid argument: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
    }
}
```

### 4. 请求日志拦截器

```java
@Component
public class LoggingInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(LoggingInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        long startTime = System.currentTimeMillis();
        request.setAttribute("startTime", startTime);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        long startTime = (Long) request.getAttribute("startTime");
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        logger.info("{} {} - {} - {}ms", 
                request.getMethod(), 
                request.getRequestURI(), 
                response.getStatus(), 
                duration);
    }
}
```

### 5. 健康检查增强

```java
@RestController
@RequestMapping("/health")
public class HealthController {

    @Value("${spring.application.name}")
    private String applicationName;

    @GetMapping
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "healthy");
        response.put("application", applicationName);
        response.put("framework", "Spring Boot");
        response.put("version", "2.7.18");
        response.put("deployment", "CloudBase HTTP 云函数");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("java_version", System.getProperty("java.version"));
        response.put("memory_usage", getMemoryUsage());
        return response;
    }

    private Map<String, Object> getMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> memory = new HashMap<>();
        memory.put("total", runtime.totalMemory() / 1024 / 1024 + " MB");
        memory.put("free", runtime.freeMemory() / 1024 / 1024 + " MB");
        memory.put("used", (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024 + " MB");
        return memory;
    }
}
```

### 6. 部署前检查清单

- [ ] `scf_bootstrap` 文件存在且有执行权限
- [ ] 端口配置为 9000
- [ ] 项目已构建（JAR 文件存在）
- [ ] **包含构建后的 JAR 文件**
- [ ] 排除不必要的文件（如 `src`、`Dockerfile`）
- [ ] 测试本地构建和启动是否正常
- [ ] 检查启动脚本语法是否正确
- [ ] JVM 参数配置合理

## 性能优化

### 1. 减少冷启动时间

```properties
# application.properties
# 启用懒加载
spring.main.lazy-initialization=true

# 减少 Tomcat 线程
server.tomcat.threads.max=10
server.tomcat.threads.min-spare=2

# 禁用不需要的自动配置
spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
```

### 2. JVM 优化

```bash
# scf_bootstrap 中的 JVM 参数
export JAVA_OPTS="-Xms256m -Xmx512m \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+UseStringDeduplication \
  -XX:+OptimizeStringConcat"
```

### 3. 构建优化

```xml
<!-- pom.xml 中的构建优化 -->
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
    <configuration>
        <excludes>
            <exclude>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-devtools</artifactId>
            </exclude>
        </excludes>
    </configuration>
</plugin>
```

### 4. 内存管理

```java
@Component
public class MemoryMonitor {

    private static final Logger logger = LoggerFactory.getLogger(MemoryMonitor.class);

    @EventListener
    public void handleContextRefresh(ContextRefreshedEvent event) {
        logMemoryUsage("Application started");
    }

    private void logMemoryUsage(String event) {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory() / 1024 / 1024;
        long freeMemory = runtime.freeMemory() / 1024 / 1024;
        long usedMemory = totalMemory - freeMemory;
        
        logger.info("{} - Memory usage: {}MB used, {}MB free, {}MB total", 
                event, usedMemory, freeMemory, totalMemory);
    }
}
```

---

## 相关文档

- [返回主文档](../README.md)
- [云托管部署指南](./cloud-run.md)
- [CloudBase 官方文档](https://docs.cloudbase.net/)