FROM openjdk:17-jdk-slim
ENV AWS_S3_ACCESS_KEY=${AWS_S3_ACCESS_KEY} 
ENV AWS_S3_SECRET_ACCESS_KEY=${AWS_S3_SECRET_ACCESS_KEY} 
ENV SERVER_PORT=${SERVER_PORT}
WORKDIR /app
COPY target/language.detector-1.0-SNAPSHOT.jar app.jar
RUN chmod +r app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]