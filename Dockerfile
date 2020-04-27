FROM openjdk:8-alpine
COPY target/example-app*.jar /usr/local/bin/example-app.jar
RUN chmod +x /usr/local/bin/example-app.jar
ENTRYPOINT ["java", "-jar", "/usr/local/bin/example-app.jar"]