FROM openjdk:8-jdk
MAINTAINER https://github.com/poshjosh
ENV \
JVM_GC="+UseConcMarkSweepGC" \
JVM_XMS="256m" \
JVM_XMX="1024m"  \
JAVA_RANDOM="-Djava.security.egd=file:/dev/./urandom"
RUN /bin/cp /usr/share/zoneinfo/CET /etc/localtime && echo 'CET' >/etc/timezone
COPY target/message-server-1.0-SNAPSHOT.jar app.jar
COPY ./docker-entrypoint.sh /docker-entrypoint.sh
RUN chmod +x /docker-entrypoint.sh
ENTRYPOINT ["/docker-entrypoint.sh"]