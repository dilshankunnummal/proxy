# FROM openjdk:17
# WORKDIR /app
# COPY OffshoreProxy.java .
# RUN javac OffshoreProxy.java
# EXPOSE 9000
# CMD ["java", "offshore.OffshoreProxy"]

FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY OffshoreProxy.java .
RUN javac OffshoreProxy.java
EXPOSE 9000
CMD ["java", "OffshoreProxy"]