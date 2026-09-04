FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

COPY .mvn .mvn
COPY mvnw ./
COPY scheduling-service/pom.xml scheduling-service/pom.xml
COPY scheduling-service/src scheduling-service/src

RUN chmod +x mvnw && ./mvnw -f scheduling-service/pom.xml clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /workspace/scheduling-service/target/scheduling-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]