FROM openjdk:17-jdk-slim

# 设置工作目录
WORKDIR /app

# 复制 JAR 文件
COPY target/*.jar app.jar

# 复制图标资源到容器内 tv-files 目录
COPY tv-files/templates/ /tv-recruitment/tv-files/templates/

# 暴露端口
EXPOSE 8080

# 启动命令
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
