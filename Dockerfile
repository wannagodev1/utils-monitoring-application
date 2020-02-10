FROM openjdk:8-jre-alpine

MAINTAINER Wannago Dev1 <Wannago.dev1@gmail.com>

RUN apk --no-cache add curl

ENV JAVA_OPTS=""

ADD target/utils-monitoring-application.jar /app/

ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app/utils-monitoring-application.jar"]

HEALTHCHECK --interval=30s --timeout=30s --retries=10 CMD curl -f http://localhost:9101/actuator/health || exit 1

EXPOSE 8001 9101