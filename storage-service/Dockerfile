FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY . .
RUN chmod +x gradlew && ./gradlew --no-daemon :storage-service:bootJar

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/storage-service/build/libs/storage-service-1.0-SNAPSHOT.jar app.jar
USER 10001:10001
EXPOSE 8083 9090
ENTRYPOINT ["java", "-jar", "app.jar"]
