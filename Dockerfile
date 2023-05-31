FROM eclipse-temurin:11-jre
ENV USER_UID=1001 \
    USER_NAME=report-publisher
RUN addgroup --gid ${USER_UID} ${USER_NAME} \
    && adduser --disabled-password --uid ${USER_UID} --ingroup ${USER_NAME} ${USER_NAME}
RUN apt update -y && apt install curl -y
USER report-publisher
WORKDIR /app
COPY target/report-publisher*.jar app.jar
COPY reports ./reports
