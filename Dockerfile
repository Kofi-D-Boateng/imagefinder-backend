FROM maven:3.8.3-amazoncorretto-17 AS build-stage
WORKDIR /app
COPY . .
# BUILD THE JAR
RUN mvn package

FROM openjdk:17-alpine
WORKDIR /app
COPY --from=build-stage /app/*.jar .
EXPOSE 9000
ENTRYPOINT ["java","-jar","/app/app.jar"]