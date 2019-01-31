FROM openjdk:8-jdk-alpine
VOLUME /tmp
ADD /build/libs/jaicore-web-0.0.1.jar app.jar
RUN apk update && apk add bash
RUN bash -c 'touch /app.jar'
ENTRYPOINT ["java","-jar","/app.jar"]