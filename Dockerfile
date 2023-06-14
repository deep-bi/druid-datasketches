FROM maven:3.9.2-eclipse-temurin-8 AS maven
COPY . /app
WORKDIR /app
RUN mvn clean package

FROM apache/druid:26.0.0 AS druid
COPY --from=maven /app/target/druid-datasketches-26.0.0.jar /opt/druid/extensions/druid-datasketches/.