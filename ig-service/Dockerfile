FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace

COPY gradle gradle
COPY gradlew .
COPY settings.gradle .
COPY build.gradle .
COPY gradle.properties .
COPY libs libs
COPY ig-service ig-service

RUN chmod +x gradlew
RUN ./gradlew :ig-service:bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=builder /workspace/ig-service/build/libs/ig-service.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "/app/app.jar"]

