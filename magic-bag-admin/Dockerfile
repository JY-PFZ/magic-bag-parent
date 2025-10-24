# ==============================
# 通用 Spring Boot 微服务 Dockerfile（支持私有 Maven 仓库）
# ==============================

# 构建阶段
FROM maven:3.9.6-eclipse-temurin-21 AS builder

ARG SERVICE_NAME=.
ENV SERVICE_NAME=${SERVICE_NAME}

WORKDIR /app

# 复制 pom.xml 并预下载依赖（利用缓存）
COPY ${SERVICE_NAME}/pom.xml .

# 使用外部 settings.xml（构建时挂载，不打入镜像）
RUN --mount=type=bind,source=.m2/settings.xml,target=/root/.m2/settings.xml \
    --mount=type=cache,target=/root/.m2/repository \
    mvn dependency:go-offline -B -s /root/.m2/settings.xml

# 复制源码并构建
COPY ${SERVICE_NAME}/src ./src
RUN --mount=type=bind,source=.m2/settings.xml,target=/root/.m2/settings.xml \
    --mount=type=cache,target=/root/.m2/repository \
    mvn clean package -DskipTests -B -s /root/.m2/settings.xml


# ==============================
# 运行阶段
# ==============================
FROM eclipse-temurin:21-jre-jammy

# 设置时区（可选：通过构建参数传入）
ARG TZ=Asia/Singapore
ENV TZ=${TZ}
RUN ln -snf /usr/share/zoneinfo/${TZ} /etc/localtime && echo ${TZ} > /etc/timezone

WORKDIR /app

# 从构建阶段复制唯一的可执行 JAR
COPY --from=builder /app/target/*.jar app.jar

# 动态暴露端口（示例为占位符，实际构建时传入）
ARG APP_PORT=8080
ENV APP_PORT=${APP_PORT}

EXPOSE ${APP_PORT}

# 健康检查（假设服务提供 /actuator/health）
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:${APP_PORT}/actuator/health || exit 1

# JVM 优化参数（限制内存）
ENV JAVA_OPTS="-Xms512m -Xmx1g -XX:+UseG1GC -XX:+UseContainerSupport"

ENTRYPOINT ["/bin/sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar --server.address=0.0.0.0 --server.port=$APP_PORT"]