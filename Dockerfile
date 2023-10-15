FROM openjdk:17-jdk-slim

VOLUME /log

ARG JAR_FILE=./build/libs/*.jar
ENV IDLE_PROFILE local
ENV JAVA_AGENT /home/ec2-user/app/pinpoint/pinpoint-agent-2.2.2/pinpoint-bootstrap-2.2.2.jar
ENV PINPOINT_CONFIG /home/ec2-user/app/pinpoint/pinpoint-agent-2.2.2/pinpoint-root.config
ENV SPRING_CONFIG /home/ec2-user/app/hanok/config/application-real-db.properties,/home/ec2-user/app/hanok/config/application-env.properties,/home/ec2-user/app/hanok/config/application-${IDLE_PROFILE}.properties

COPY ${JAR_FILE} yeoyeo.jar

# 실행 명령

ENTRYPOINT ["nohup", "java","-jar",\
"-javaagent:${JAVA_AGENT}",\
"-Dpinpoint.agentId=${IDLE_PROFILE}",\
"-Dpinpoint.applicationName=yeoyeo",\
"-Dpinpoint.config=${PINPOINT_CONFIG}",\
"-Dspring.config.location=classpath:${SPRING_CONFIG}", \
"-Dspring.profiles.active=${IDLE_PROFILE}",\
"yeoyeo.jar", "2>&1", "&"]
