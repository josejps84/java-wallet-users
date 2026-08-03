FROM maven:3.9.9-eclipse-temurin-25 AS build
WORKDIR /workspace
COPY pom.xml ./
COPY src ./src
RUN mvn -DskipTests clean package
RUN apt-get update && apt-get install -y --no-install-recommends unzip && rm -rf /var/lib/apt/lists/*

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /workspace/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "MONGO_URI=${SPRING_MONGODB_URI:-${SPRING_DATA_MONGODB_URI:-mongodb://mongo:27017/wallet}}; java -Dspring.mongodb.uri=${MONGO_URI} -Dspring.data.mongodb.uri=${MONGO_URI} -jar /app/app.jar"]
