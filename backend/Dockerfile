FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/language.detector-1.0-SNAPSHOT.jar app.jar
RUN chmod +r app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]