# Multi-stage Dockerfile
FROM maven:3.9.4-eclipse-temurin-21 as builder
WORKDIR /workspace
COPY pom.xml mvnw ./
COPY .mvn .mvn
COPY src src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre-jammy
ARG JAR_FILE=target/*.jar
COPY --from=builder /workspace/${JAR_FILE} app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]

