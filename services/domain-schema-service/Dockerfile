FROM gradle:8.5-jdk17 AS builder
WORKDIR /app
COPY build.gradle settings.gradle ./
COPY src ./src
RUN gradle clean build -x test

FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=builder /app/build/libs/domain-schema-service-1.0.0.jar app.jar

# Add wait-for-it script for service dependencies
RUN apt-get update && apt-get install -y wget
RUN wget https://raw.githubusercontent.com/vishnubob/wait-for-it/master/wait-for-it.sh
RUN chmod +x wait-for-it.sh

EXPOSE 8083

ENTRYPOINT ["./wait-for-it.sh", "kafka:9092", "--", "./wait-for-it.sh", "redis:6379", "--", "java", "-jar", "app.jar"]
