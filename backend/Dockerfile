# 使用 OpenJDK 8 基础镜像
FROM openjdk:8-jre-slim

# 设置工作目录
WORKDIR /app

# 复制 JAR 文件
COPY target/podcast-website-1.0.0.jar app.jar

# 暴露端口
EXPOSE 8080

# 运行应用
ENTRYPOINT ["java", "-jar", "app.jar"]