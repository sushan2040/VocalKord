FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/language.detector-1.0-SNAPSHOT.jar app.jar
RUN chmod +r app.jar
# Add healthcheck
HEALTHCHECK --interval=10s --timeout=3s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:8082/health || exit 1
ENV AWS_S3_ACCESS_KEY=${AWS_S3_ACCESS_KEY}
ENV AWS_S3_SECRET_ACCESS_KEY=${AWS_S3_SECRET_ACCESS_KEY}
ENV SERVER_PORT=${SERVER_PORT}
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]