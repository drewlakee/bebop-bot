FROM openjdk:11-slim

LABEL maintainer = "Andrew Aleynikov [drew.lake@yandex.ru]"

COPY . /

RUN chmod 777 gradlew
RUN gradlew build --no-daemon

RUN cp build/libs/bebop-bot-1.0.2.jar /bin

ENTRYPOINT ["java", "-jar", "/bin/bebop-bot-1.0.2.jar"]
