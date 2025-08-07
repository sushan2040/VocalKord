FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/language.detector-1.0-SNAPSHOT.jar app.jar
RUN chmod +r app.jar
ENV AWS_S3_ACCESS_KEY=${AWS_S3_ACCESS_KEY} AWS_S3_SECRET_ACCESS_KEY=${AWS_S3_SECRET_ACCESS_KEY} SERVER_PORT=${SERVER_PORT}
ENTRYPOINT ["java", "-jar", "app.jar"]