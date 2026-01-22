# 构建阶段保持不变
FROM maven:3.9.4-eclipse-temurin-11 as build

WORKDIR /app
COPY src /app/src
COPY settings.xml pom.xml /app/
RUN mvn -s /app/settings.xml -f /app/pom.xml clean package -DskipTests

# 运行阶段 - 使用更新的基础镜像
FROM openjdk:11.0.11-jre-slim-buster

# 设置时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENV PORT=8080
ENV JAVA_OPTS="-Xmx1g -Xms512m -XX:+UseG1GC"

CMD ["sh", "-c", "java $JAVA_OPTS -Dserver.port=$PORT -jar app.jar"]
