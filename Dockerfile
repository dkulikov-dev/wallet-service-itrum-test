FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Копируем всё
COPY . .

# Собираем приложение
RUN mvn clean package -DskipTests

# Runtime образ
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Копируем готовый JAR из build стадии
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]