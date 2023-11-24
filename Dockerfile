ARG application=stockadmin
ARG port=8099

FROM openjdk:8
ARG application
ARG port
EXPOSE ${port}
RUN mkdir /app
COPY target/light-merge-0.2.jar /app/light-merge.jar
CMD ["java", "-jar", "/app/light-merge.jar", "--server.port=${port}"]
