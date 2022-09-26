FROM eclipse-temurin:17 AS gradle
WORKDIR /gradle
COPY gradlew build.gradle settings.gradle .
COPY ./gradle ./gradle
RUN ["./gradlew", "dependencies", "--no-daemon"]
COPY . .
RUN ["./gradlew", "clean", "build", "--no-daemon"]

FROM eclipse-temurin:17
EXPOSE 8080
WORKDIR /gripp
COPY --from=gradle /gradle/build/libs/*.jar ./gripp.jar
ENTRYPOINT ["java", "-jar", "gripp.jar"]
