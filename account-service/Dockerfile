FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/account-service-1.0.0.jar app.jar
EXPOSE 7003
ENTRYPOINT ["java","-jar","/app/app.jar"]
