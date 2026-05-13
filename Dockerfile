FROM eclipse-temurin:17-jdk-jammy

WORKDIR /usr/local/lib

COPY target/my-ledger-be-0.0.1-SNAPSHOT.jar cashlink-api.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","cashlink-api.jar"]