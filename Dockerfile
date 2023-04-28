FROM eclipse-temurin:11-jre
RUN apt update -y && apt install curl -y
WORKDIR /app
COPY target/report-publisher*.jar app.jar
COPY reports ./reports