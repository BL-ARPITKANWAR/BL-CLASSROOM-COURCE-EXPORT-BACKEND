FROM maven:3.9.8-eclipse-temurin-24 AS builder

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:24-jre

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8084

ENV JAVA_OPTS="-Xms256m -Xmx512m"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]