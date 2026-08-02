FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .

# Copy ALL module pom.xml files (even ones you're not building)
COPY common-lib/pom.xml common-lib/pom.xml
COPY trainer-workload-service/pom.xml trainer-workload-service/pom.xml
COPY eureka-server/pom.xml eureka-server/pom.xml
COPY main-service/pom.xml main-service/pom.xml

# Only copy source for what you actually need to build
COPY common-lib/src common-lib/src
COPY trainer-workload-service/src trainer-workload-service/src

RUN mvn -pl trainer-workload-service -am package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/trainer-workload-service/target/trainer-workload-service-*-exec.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
