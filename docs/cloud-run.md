# Spring Boot 云托管部署指南

本指南详细介绍如何将 Spring Boot 应用部署到 CloudBase 云托管服务。

> **📋 前置要求**：如果您还没有创建 Spring Boot 项目，请先阅读 [Spring Boot 项目创建指南](./project-setup.md)。

## 📋 目录导航

- [部署特性](#部署特性)
- [准备部署文件](#准备部署文件)
- [项目结构](#项目结构)
- [部署步骤](#部署步骤)
- [访问应用](#访问应用)
- [常见问题](#常见问题)
- [最佳实践](#最佳实践)
- [高级配置](#高级配置)

---

## 部署特性

云托管适合以下场景：

- **企业级应用**：复杂的 Web 应用和管理系统
- **高并发**：需要处理大量并发请求
- **自定义环境**：需要特定的运行时环境
- **微服务架构**：容器化部署和管理

### 技术特点

| 特性 | 说明 |
|------|------|
| **计费方式** | 按资源使用量（CPU/内存） |
| **启动方式** | 持续运行 |
| **端口配置** | 可自定义端口（默认 8080） |
| **扩缩容** | 支持自动扩缩容配置 |
| **Java 环境** | 完全自定义 Java 环境 |

## 准备部署文件

### 1. 创建 Dockerfile

创建 `Dockerfile` 文件：

```dockerfile
# 使用官方 OpenJDK 运行时作为基础镜像
FROM openjdk:8-jre-alpine

# 设置工作目录
WORKDIR /app

# 安装必要的工具
RUN apk add --no-cache curl

# 复制 Maven 配置文件
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# 复制源代码
COPY src src

# 构建应用（如果需要在容器内构建）
# RUN ./mvnw clean package -DskipTests

# 或者直接复制已构建的 JAR 文件
COPY target/cloudrun-springboot-1.0-SNAPSHOT.jar app.jar

# 暴露端口
EXPOSE 8080

# 设置环境变量
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"
ENV SERVER_PORT=8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# 启动命令
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.jar"]
```

### 2. 创建 .dockerignore 文件

创建 `.dockerignore` 文件以优化构建性能：

```
.git
.gitignore
README.md
Dockerfile
.dockerignore
target/classes
target/test-classes
target/maven-status
target/maven-archiver
target/surefire-reports
.mvn/wrapper/maven-wrapper.jar
mvnw
mvnw.cmd
scf_bootstrap
docs/
*.log
.DS_Store
.idea
.vscode
*.iml
```

### 3. 优化 application.properties

确保 `src/main/resources/application.properties` 支持云托管环境：

```properties
# 服务器配置
server.port=${SERVER_PORT:8080}
server.servlet.context-path=/

# 应用信息
spring.application.name=cloudrun-springboot
management.endpoints.web.exposure.include=health,info,metrics

# 日志配置
logging.level.com.tencent.cloudrun=INFO
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n

# 编码配置
server.servlet.encoding.charset=UTF-8
server.servlet.encoding.enabled=true
server.servlet.encoding.force=true

# 生产环境优化
server.tomcat.threads.max=200
server.tomcat.threads.min-spare=10
server.tomcat.connection-timeout=20000
server.tomcat.max-connections=8192

# 数据库连接池配置（如果使用数据库）
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000
```

### 4. 依赖管理

确保 `pom.xml` 包含所有必要依赖：

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
    <!-- 如果需要数据库支持 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
        <scope>runtime</scope>
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
│   └── cloudrun-springboot-1.0-SNAPSHOT.jar
├── pom.xml                    # Maven 配置文件
├── Dockerfile                 # 🔑 容器配置文件
└── .dockerignore             # Docker 忽略文件
```

> 💡 **说明**：
> - 云托管支持自定义端口，默认使用 8080 端口
> - 使用 Docker 容器内的 Java 环境
> - Docker 容器提供了完整的 Java 环境控制

## 部署步骤

### 通过控制台部署

1. 登录 [CloudBase 控制台](https://console.cloud.tencent.com/tcb)
2. 选择您的环境，进入「云托管」页面
3. 点击「新建服务」
4. 填写服务名称（如：`cloudrun-springboot-service`）
5. 选择「本地代码」上传方式
6. 上传包含 `Dockerfile` 的项目目录
7. 配置服务参数：
   - **端口**：8080（或您在应用中配置的端口）
   - **CPU**：0.5 核
   - **内存**：1 GB
   - **实例数量**：1-10（根据需求调整）
8. 点击「创建」按钮等待部署完成

### 通过 CLI 部署

```bash
# 安装 CloudBase CLI
npm install -g @cloudbase/cli

# 登录
tcb login

# 初始化云托管配置
tcb run init

# 部署云托管服务
tcb run deploy --port 8080
```

### 配置文件部署

创建 `cloudbaserc.json` 配置文件：

```json
{
  "envId": "your-env-id",
  "framework": {
    "name": "springboot",
    "plugins": {
      "run": {
        "name": "@cloudbase/framework-plugin-run",
        "options": {
          "serviceName": "cloudrun-springboot-service",
          "servicePath": "/",
          "localPath": "./",
          "dockerfile": "./Dockerfile",
          "buildDir": "./",
          "cpu": 0.5,
          "mem": 1,
          "minNum": 1,
          "maxNum": 10,
          "policyType": "cpu",
          "policyThreshold": 60,
          "containerPort": 8080,
          "envVariables": {
            "SPRING_PROFILES_ACTIVE": "prod",
            "JAVA_OPTS": "-Xms256m -Xmx512m"
          }
        }
      }
    }
  }
}
```

然后执行部署：

```bash
tcb framework deploy
```

### 模板部署（快速开始）

1. 登录 [腾讯云托管控制台](https://tcb.cloud.tencent.com/dev#/platform-run/service/create?type=image)
2. 点击「通过模板部署」，选择 **Spring Boot 模板**
3. 输入自定义服务名称，点击部署
4. 等待部署完成后，点击左上角箭头，返回到服务详情页
5. 点击概述，获取默认域名并访问

## 访问应用

### 获取访问地址

云托管部署成功后，系统会自动分配访问地址。您也可以绑定自定义域名。

访问地址格式：`https://your-service-url/`

### 测试接口

- **根路径**：`/` - Spring Boot 欢迎页面
- **健康检查**：`/health` - 查看应用状态
- **监控端点**：`/actuator/health` - Spring Boot Actuator 健康检查
- **应用信息**：`/actuator/info` - 应用信息
- **用户列表**：`/api/users` - 获取用户列表
- **用户详情**：`/api/users/1` - 获取特定用户
- **创建用户**：`POST /api/users` - 创建新用户

### 示例请求

```bash
# 健康检查
curl https://your-service-url/health

# Spring Boot Actuator 健康检查
curl https://your-service-url/actuator/health

# 获取用户列表
curl https://your-service-url/api/users

# 分页查询
curl "https://your-service-url/api/users?page=1&limit=2"

# 创建新用户
curl -X POST https://your-service-url/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"测试用户","email":"test@example.com"}'
```

## 常见问题

### Q: 云托管支持哪些端口？
A: 云托管支持自定义端口，Spring Boot 应用默认使用 8080 端口，也可以根据需要配置其他端口。

### Q: 如何配置生产环境设置？
A: 通过环境变量和配置文件控制应用配置：

```properties
# application-prod.properties
spring.profiles.active=prod
server.port=${SERVER_PORT:8080}

# 数据库配置
spring.datasource.url=${DATABASE_URL}
spring.datasource.username=${DATABASE_USERNAME}
spring.datasource.password=${DATABASE_PASSWORD}

# 日志级别
logging.level.com.tencent.cloudrun=${LOG_LEVEL:INFO}
```

### Q: 如何配置环境变量？
A: 可以通过以下方式配置：
- 控制台服务配置页面
- `cloudbaserc.json` 配置文件
- Dockerfile 中的 ENV 指令

### Q: Spring Boot 应用如何处理静态文件？
A: 在云托管环境中，Spring Boot 可以直接处理静态文件：

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }
}
```

### Q: 如何查看云托管日志？
A: 在云托管服务详情页面可以查看：
- 实例日志
- 构建日志
- 访问日志
- 错误日志

## 最佳实践

### 1. 多阶段构建优化

```dockerfile
# 构建阶段
FROM maven:3.8.4-openjdk-8 AS builder

WORKDIR /app
COPY pom.xml .
COPY src src

RUN mvn clean package -DskipTests

# 运行阶段
FROM openjdk:8-jre-alpine

WORKDIR /app
COPY --from=builder /app/target/cloudrun-springboot-1.0-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 2. 环境变量管理

```java
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppConfig {
    
    private String name;
    private String version;
    private Database database = new Database();
    
    // Getters and Setters
    
    public static class Database {
        private String url;
        private String username;
        private String password;
        private int maxPoolSize = 20;
        
        // Getters and Setters
    }
}
```

### 3. 健康检查增强

```java
@Component
public class CustomHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        // 检查数据库连接
        if (isDatabaseHealthy()) {
            return Health.up()
                    .withDetail("database", "Available")
                    .withDetail("memory", getMemoryInfo())
                    .build();
        } else {
            return Health.down()
                    .withDetail("database", "Unavailable")
                    .build();
        }
    }

    private boolean isDatabaseHealthy() {
        // 实现数据库健康检查逻辑
        return true;
    }

    private Map<String, Object> getMemoryInfo() {
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> memory = new HashMap<>();
        memory.put("total", runtime.totalMemory() / 1024 / 1024 + " MB");
        memory.put("free", runtime.freeMemory() / 1024 / 1024 + " MB");
        memory.put("used", (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024 + " MB");
        return memory;
    }
}
```

### 4. 日志配置

```xml
<!-- logback-spring.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <springProfile name="!prod">
        <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
            <encoder>
                <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
            </encoder>
        </appender>
        <root level="INFO">
            <appender-ref ref="CONSOLE"/>
        </root>
    </springProfile>

    <springProfile name="prod">
        <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
            <encoder class="net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder">
                <providers>
                    <timestamp/>
                    <logLevel/>
                    <loggerName/>
                    <message/>
                    <mdc/>
                </providers>
            </encoder>
        </appender>
        <root level="INFO">
            <appender-ref ref="CONSOLE"/>
        </root>
    </springProfile>
</configuration>
```

### 5. CORS 配置

```java
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        
        // 允许的域名
        config.setAllowedOriginPatterns(Arrays.asList("*"));
        
        // 允许的请求头
        config.setAllowedHeaders(Arrays.asList("*"));
        
        // 允许的请求方法
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        
        // 允许携带凭证
        config.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        return new CorsFilter(source);
    }
}
```

### 6. 全局异常处理

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

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFoundException(ResourceNotFoundException e) {
        logger.warn("Resource not found: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage()));
    }
}
```

### 7. 部署前检查清单

- [ ] `Dockerfile` 文件存在且配置正确
- [ ] `.dockerignore` 文件配置合理
- [ ] 端口配置灵活（支持环境变量）
- [ ] 容器启动命令正确
- [ ] **排除 `scf_bootstrap` 文件**（仅用于云函数）
- [ ] 本地 Docker 构建测试通过
- [ ] Spring Boot 应用配置正确
- [ ] 健康检查端点可用
- [ ] 环境变量配置完整

## 高级配置

### 1. 负载均衡配置

```json
{
  "run": {
    "name": "@cloudbase/framework-plugin-run",
    "options": {
      "serviceName": "cloudrun-springboot-service",
      "cpu": 1,
      "mem": 2,
      "minNum": 2,
      "maxNum": 20,
      "policyType": "cpu",
      "policyThreshold": 70,
      "containerPort": 8080,
      "customLogs": "stdout",
      "initialDelaySeconds": 30,
      "healthCheckPath": "/actuator/health"
    }
  }
}
```

### 2. 数据库集成

```properties
# application-prod.properties
# MySQL 配置
spring.datasource.url=${DATABASE_URL:jdbc:mysql://localhost:3306/cloudrun}
spring.datasource.username=${DATABASE_USERNAME:root}
spring.datasource.password=${DATABASE_PASSWORD:}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA 配置
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
spring.jpa.properties.hibernate.format_sql=true

# 连接池配置
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000
```

### 3. Redis 缓存配置

```java
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        LettuceConnectionFactory factory = new LettuceConnectionFactory(
                new RedisStandaloneConfiguration(
                        System.getenv("REDIS_HOST"),
                        Integer.parseInt(System.getenv("REDIS_PORT"))
                )
        );
        return factory;
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(config)
                .build();
    }
}
```

### 4. 监控和告警

```java
@Component
public class ApplicationMetrics {

    private final MeterRegistry meterRegistry;
    private final Counter requestCounter;
    private final Timer requestTimer;

    public ApplicationMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.requestCounter = Counter.builder("http_requests_total")
                .description("Total HTTP requests")
                .register(meterRegistry);
        this.requestTimer = Timer.builder("http_request_duration")
                .description("HTTP request duration")
                .register(meterRegistry);
    }

    public void incrementRequestCount() {
        requestCounter.increment();
    }

    public Timer.Sample startTimer() {
        return Timer.start(meterRegistry);
    }
}
```

---

## 相关文档

- [返回主文档](../README.md)
- [HTTP 云函数部署指南](./http-function.md)
- [CloudBase 官方文档](https://docs.cloudbase.net/)