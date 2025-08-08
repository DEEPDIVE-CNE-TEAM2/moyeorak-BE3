# 1단계: 빌드
FROM gradle:8.5-jdk17 AS builder
WORKDIR /app
COPY . .
RUN chmod +x gradlew
RUN ./gradlew clean build -x test

# 2단계: 런타임
FROM amazoncorretto:17-alpine
WORKDIR /moyeorak-BE
COPY --from=builder /app/build/libs/moyeorak-0.0.1-SNAPSHOT.jar moyeorak.jar
ENTRYPOINT ["java", "-jar", "moyeorak.jar"]