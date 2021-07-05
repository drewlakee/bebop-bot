FROM openjdk:11-slim

LABEL maintainer = "Andrew Aleynikov [drew.lake@yandex.ru]"

COPY [".", "/bebop-bot"]

WORKDIR /bebop-bot

RUN ["chmod", "+x", "./gradlew"]
RUN ["./gradlew", "build", "--no-daemon"]

ENTRYPOINT ["java", "-jar", "build/libs/bebop-bot-1.0.2.jar"]
