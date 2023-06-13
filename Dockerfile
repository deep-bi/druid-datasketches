FROM maven:3.9.2-eclipse-temurin-8 AS maven
COPY . /app
WORKDIR /app
RUN mvn clean package

FROM apache/druid:25.0.0 AS druid
RUN ls /opt/druid/extensions/druid-datasketches/
COPY --from=maven /app/target/druid-datasketches-25.0.0.jar /opt/druid/extensions/druid-datasketches/.