FROM openjdk:8u111-jdk-alpine
EXPOSE 8080
ADD target/mancala-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]