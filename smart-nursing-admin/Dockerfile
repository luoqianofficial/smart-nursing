# 使用 JDK 17 基础镜像
FROM openjdk:17-jdk-slim

# 设置工作目录
WORKDIR /app

# 复制打包好的 jar 文件
# 注意：这个 jar 是由 Maven 在父工程根目录执行 package 后生成的
COPY target/smart-nursing-admin-*.jar app.jar

# 暴露 Spring Boot 应用的端口（假设你用 8080）
EXPOSE 8080

# 启动命令
ENTRYPOINT ["java", "-jar", "app.jar"]