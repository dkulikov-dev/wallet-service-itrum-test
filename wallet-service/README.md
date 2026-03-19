# Wallet Service - Тестовое задание ИТРУМ

REST API для управления кошельками с поддержкой высокой конкурентности.

## Требования
- Java 17+
- Docker + Docker Compose
- PostgreSQL 16

## Запуск

### Локально
```bash
docker compose up -d postgres
mvn spring-boot:run