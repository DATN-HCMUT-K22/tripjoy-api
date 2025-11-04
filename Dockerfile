FROM openjdk:21

ARG FILE_JAR=target/*.jar

ADD ${FILE_JAR} tripjoy-api.jar

ENTRYPOINT ["java", "-jar", "tripjoy-api.jar"]

EXPOSE 8080