# Wallet Service

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

## 📊 Мониторинг

### Grafana Dashboard

![Grafana Dashboard](docs/images/grafana-dashboard.png)

**Доступные метрики:**
- ✅ Request Rate (запросов в секунду)
- ✅ Error Rate (5xx ошибки)
- ✅ Average Response Time (p95)
- ✅ JVM Heap Memory
- ✅ Active DB Connections (HikariCP)
- ✅ Uptime приложения

**URL доступа (после `docker compose up`):**
| Сервис | URL | Логин/пароль |
|--------|-----|--------------|
| Grafana | http://localhost:3000 | admin / admin |
| Prometheus | http://localhost:9090 | — |
| Приложение | http://localhost:8080 | — |

**Пример запроса для генерации метрик:**
```bash
curl.exe http://localhost:8080/api/v1/wallets/11111111-1111-1111-1111-111111111111
curl -X POST http://localhost:8080/api/v1/wallet \
  -H "Content-Type: application/json" \
  -d '{"walletId":"...","operationType":"DEPOSIT","amount":100}'