FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY bonussystem/ .

RUN apt-get update && apt-get install -y maven

RUN mvn clean package -Dmaven.test.skip=true

EXPOSE 8080

CMD ["java","-jar","target/bonussystem-0.0.1-SNAPSHOT.jar"]