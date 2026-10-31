FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -B -ntp -q dependency:go-offline
COPY src src
RUN mvn -B -ntp -q -DskipTests package

FROM eclipse-temurin:21-jre
RUN useradd --system --no-create-home --uid 10001 esep
WORKDIR /app
COPY --from=build /build/target/*.jar app.jar
USER esep
ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-Djava.awt.headless=true", "-jar", "app.jar"]
