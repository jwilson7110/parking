FROM maven:3.8.3-openjdk-17 AS build
COPY src /home/app/src
COPY pom.xml /home/app
COPY lots.csv lots.csv
RUN mvn -f /home/app/pom.xml clean package
EXPOSE 8080
ENTRYPOINT ["java","-jar","/home/app/target/parking-0.0.1-SNAPSHOT.jar", "--spring.profiles.active=production"]