# 使用 JDK 17 基础镜像
FROM eclipse-temurin:17-jdk-alpine

# 设置工作目录
WORKDIR /app

# 复制打包好的 jar 文件
# 建议先在根目录执行 mvnw.cmd package 确保 target 下有文件
COPY target/smart-nursing-admin-*.jar app.jar

# 暴露端口
EXPOSE 8080

# 启动命令
ENTRYPOINT ["java", "-jar", "app.jar"]