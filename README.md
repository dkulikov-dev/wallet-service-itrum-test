# Wallet Service — Тестовое задание ИТРУМ

REST API для управления балансом кошельков с поддержкой высокой конкурентности.

## Основная особенность
При 1000 RPS на один кошелёк используется **атомарный UPDATE** на уровне PostgreSQL (`UPDATE ... WHERE balance >= amount`).  
Благодаря этому ни один запрос не возвращает 50x ошибку, даже при сильной конкуренции.

## Стек
- Java 17
- Spring Boot 3.3
- Spring Data JPA + Liquibase
- PostgreSQL 16
- Docker + Docker Compose
- Maven

## Запуск

### Через Docker Compose
```bash
docker compose up --build
```
Приложение будет доступно по адресу: http://localhost:8080

### Локально
```bash
docker compose up -d postgres
mvn spring-boot:run
```

## Основные эндпоинты

POST/api/v1/wallet — пополнение / снятие средств
GET/api/v1/wallets/{walletId} — получение баланса

## Обработка ошибок

404 — кошелёк не найден
409 — недостаточно средств
400 — невалидный запрос
