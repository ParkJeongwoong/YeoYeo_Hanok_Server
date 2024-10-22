FROM openjdk:17-jdk-slim

VOLUME /logs
VOLUME /tmp

ARG JAR_FILE=./build/libs/*.jar
ENV IDLE_PROFILE dev

COPY ${JAR_FILE} yeoyeo.jar
COPY ./src/main/resources/application.properties /application.properties
COPY ./src/main/resources/application-env.properties /application-env.properties
COPY ./src/main/resources/application-real-db.properties /application-real-db.properties
COPY ./src/main/resources/application-dev.properties /application-dev.properties
COPY ./src/main/resources/application-real1.properties /application-real1.properties
COPY ./src/main/resources/application-real2.properties /application-real2.properties

COPY ./pinpoint-agent-2.5.1 /pinpoint-agent-2.5.1

# 실행 명령

ENTRYPOINT ["nohup", "java","-jar",\
"-javaagent:/pinpoint-agent-2.5.1/pinpoint-bootstrap-2.5.1.jar",\
"-Dpinpoint.agentId=$IDLE_PROFILE","-Dpinpoint.applicationName=yeoyeo",\
"-Dpinpoint.config=/pinpoint-agent-2.5.1/pinpoint-root.config",\
"-Dspring.config.location=/application.properties,/application-env.properties,/application-real-db.properties,/application-${IDLE_PROFILE}.properties",\
"-Dspring.profiles.active=${IDLE_PROFILE}",\
"yeoyeo.jar", "2>&1", "&"]
