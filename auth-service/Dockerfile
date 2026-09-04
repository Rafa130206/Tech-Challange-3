FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

COPY .mvn .mvn
COPY mvnw ./
COPY auth-service/pom.xml auth-service/pom.xml
COPY auth-service/src auth-service/src

RUN chmod +x mvnw && ./mvnw -f auth-service/pom.xml clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /workspace/auth-service/target/auth-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
