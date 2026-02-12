FROM gradle:jdk21-jammy AS builder
WORKDIR /app

COPY build.gradle settings.gradle gradlew ./
COPY gradle gradle

COPY src src


RUN ./gradlew bootJar -x test --no-daemon

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]