FROM eclipse-temurin:21-jre

WORKDIR /app

COPY target/emulator-service-1.0.0.jar app.jar
COPY src/main/resources/application.yml application.yml

ENV JAVA_TOOL_OPTIONS="-Dcom.sun.management.jmxremote \
    -Dcom.sun.management.jmxremote.port=9010 \
    -Dcom.sun.management.jmxremote.rmi.port=9010 \
    -Dcom.sun.management.jmxremote.local.only=false \
    -Dcom.sun.management.jmxremote.authenticate=false \
    -Dcom.sun.management.jmxremote.ssl=false \
    -Djava.rmi.server.hostname=localhost"

EXPOSE 8080
EXPOSE 9010

CMD ["java", "-jar", "app.jar"]
