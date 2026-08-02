FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY common-lib/pom.xml common-lib/pom.xml
COPY eureka-server/pom.xml eureka-server/pom.xml
COPY main-service/pom.xml main-service/pom.xml
COPY trainer-workload-service/pom.xml trainer-workload-service/pom.xml
COPY common-lib/src common-lib/src
COPY main-service/src main-service/src

RUN mvn -pl main-service -am package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/main-service/target/main-service-*-exec.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
