FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace

COPY gradle gradle
COPY gradlew .
COPY settings.gradle .
COPY build.gradle .
COPY gradle.properties .
COPY libs libs
COPY api-gateway api-gateway

RUN chmod +x gradlew
RUN ./gradlew :api-gateway:bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=builder /workspace/api-gateway/build/libs/api-gateway.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]

