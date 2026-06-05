FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY bonussystem/pom.xml .
COPY bonussystem/src ./src

RUN apt-get update && apt-get install -y maven
RUN mvn clean package -DskipTests

EXPOSE 8080

CMD ["java", "-jar", "target/bonussystem-0.0.1-SNAPSHOT.jar"]