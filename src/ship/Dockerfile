FROM openjdk:17
WORKDIR /app
COPY ShipProxy.java .
RUN javac ShipProxy.java
EXPOSE 8080
CMD ["java", "ship.ShipProxy"]