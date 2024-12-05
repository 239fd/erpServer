FROM openjdk:17-jdk-slim

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY . .

RUN chmod +x gradlew
RUN ./gradlew build -x test --no-daemon

RUN mkdir -p /app/dist && \
    cp build/libs/wms-0.0.1-SNAPSHOT.jar /app/dist/app.jar

ENTRYPOINT ["java", "-jar", "/app/dist/app.jar"]
