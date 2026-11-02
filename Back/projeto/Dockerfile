FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package && cp target/*.jar /app/aplicacao.jar

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/aplicacao.jar aplicacao.jar
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0"
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "aplicacao.jar"]
