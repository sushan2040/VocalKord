FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/language.detector-1.0-SNAPSHOT.jar app.jar
ENV DB_URL=${DB_URL} DB_USERNAME=${DB_USERNAME} DB_PASSWORD=${DB_PASSWORD}
ENTRYPOINT ["java", "-jar", "app.jar"]