FROM eclipse-temurin:21-jre

WORKDIR /app

COPY target/emulator-service-1.0.0.jar app.jar
COPY src/main/resources/application.yml application.yml
COPY src/main/resources/response.json response.json

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
