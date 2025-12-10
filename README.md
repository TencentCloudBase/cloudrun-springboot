# 快速部署 Springboot 应用

本篇文章为您介绍应用控制台的部署方案, 您可以通过以下操作完成部署。

## 模版部署 Springboot 应用

1、登录 [腾讯云托管控制台](https://tcb.cloud.tencent.com/dev#/platform-run/service/create?type=image)

2、点击通过模版部署，选择 ```Springboot 模版```

3、输入自定义服务名称，点击部署

4、等待部署完成之后，点击左上角箭头，返回到服务详情页

5、点击概述，获取默认域名并访问，会显示云托管默认首页

## 自定义部署 Springboot 应用

### 创建一个 Springboot 应用

要创建 Spring Boot 应用程序，请确保你的机器上安装了[JDK](https://www.oracle.com/java/technologies/downloads/)。

前往[start.spring.io](https://start.spring.io/)初始化新的 Spring Boot 应用。选择以下选项来自定并生成你的应用。

- Project: Maven
- 语言: Java
- Spring Boot: 3.3.4
- 项目元数据:
  - Group: com.tencent
  - Artifact: cloudrun
  - Name: cloudrun
  - Description: Demo project for Spring Boot
  - Package name: com.tencent.cloudrun
  - Packaging: Jar
  - Java: 17
- 依赖项:
  - 点击"添加依赖项"按钮并搜索 Spring Boot。选择它。

单击"CREATE"按钮,下载压缩文件并解压到你机器的文件夹中。

### 源码

[cloudrun-springboot](https://github.com/TencentCloudBase/tcbr-templates/tree/main/cloudrun-springboot)

### 部署到云托管

1、在 Spring Boot 应用程序的跟目录中创建一个 `Dockerfile` 文件, 文件内容如下:
Note: maven 版本和 jre 版本需要根据实际情况进行修改。`cloudrun-1.0-SNAPSHOT.jar`名称根据实际情况修改。根目录下需要`settings.xml`文件

```dockerfile
FROM maven:3.6.0-jdk-17-slim as build

# 指定构建过程中的工作目录
WORKDIR /app

# 将src目录下所有文件，拷贝到工作目录中src目录下（.gitignore/.dockerignore中文件除外）
COPY src /app/src

# 将pom.xml文件，拷贝到工作目录下
COPY settings.xml pom.xml /app/

# 执行代码编译命令
# 自定义settings.xml, 选用国内镜像源以提高下载速度
RUN mvn -s /app/settings.xml -f /app/pom.xml clean package

# 选择运行时基础镜像
FROM alpine:3.13

# 安装依赖包，如需其他依赖包，请到alpine依赖包管理(https://pkgs.alpinelinux.org/packages?name=php8*imagick*&branch=v3.13)查找。
# 选用国内镜像源以提高下载速度
RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.tencent.com/g' /etc/apk/repositories \
    && apk add --update --no-cache openjdk17-jre-base \
    && rm -f /var/cache/apk/*

# 容器默认时区为UTC，如需使用上海时间请启用以下时区设置命令
# RUN apk add tzdata && cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo Asia/Shanghai > /etc/timezone

# 使用 HTTPS 协议访问容器云调用证书安装
RUN apk add ca-certificates

# 指定运行时的工作目录
WORKDIR /app

# 将构建产物jar包拷贝到运行时目录中
COPY --from=build /app/target/*.jar .

# 暴露端口
# 此处端口必须与「服务设置」-「流水线」以及「手动上传代码包」部署时填写的端口一致，否则会部署失败。
EXPOSE 80

# 执行启动命令.
# 写多行独立的CMD命令是错误写法！只有最后一行CMD命令会被执行，之前的都会被忽略，导致业务报错。
# 请参考[Docker官方文档之CMD命令](https://docs.docker.com/engine/reference/builder/#cmd)
CMD ["java", "-jar", "/app/cloudrun-1.0-SNAPSHOT.jar"]
```

2、进入 [腾讯云托管](https://tcb.cloud.tencent.com/dev#/platform-run/service/create?type=package),

3、选择 ```通过本地代码``` 部署，

4、填写配置信息:

  * 代码包类型: 选择文件夹
  * 代码包: 点击选择 cloudrun-springboot 目录，并上传目录文件
  * 服务名称: 填写服务名称
  * 部署类型: 选择容器服务型
  * 端口: 默认填写 80
  * 目标目录: 默认为空
  * Dockerfile 名称: Dockerfile
  * 环境变量: 如果有按需要填写
  * 公网访问: 默认打开
  * 内网访问: 默认关闭

5、配置填写完成之后，点击部署等待部署完成，

6、部署完成之后，跳转到服务概述页面，点击默认域名进行公网访问及测试。
