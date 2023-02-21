FROM openjdk:8-jdk-alpine
MAINTAINER https://github.com/poshjosh
COPY target/message-server-1.0-SNAPSHOT.jar message-server.jar
ENTRYPOINT ["java","-jar","/message-server.jar"]