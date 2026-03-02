# FROM openjdk:17
# WORKDIR /app
# COPY ShipProxy.java .
# RUN javac ShipProxy.java
# EXPOSE 8080
# CMD ["java", "ship.ShipProxy"]

FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY ShipProxy.java .
RUN javac ShipProxy.java
EXPOSE 8080
CMD ["java", "ShipProxy"]