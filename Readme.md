# spring-microservices-ecommerce

A production-grade e-commerce backend built with **Spring Boot 3.4** and **Spring Cloud**, demonstrating real-world distributed systems patterns: service discovery, centralized configuration, API gateway routing, JWT authentication, circuit breakers, and event-driven communication.

> ⚠️ **Work in progress** — being built incrementally as a learning project and portfolio piece.

---

## 🏛️ Architecture

*(Architecture diagram coming soon)*

---

## 🧰 Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.4, Spring Cloud 2024 |
| Service Discovery | Netflix Eureka |
| Config Management | Spring Cloud Config Server |
| API Gateway | Spring Cloud Gateway (WebFlux) |
| Sync Communication | OpenFeign |
| Async Communication | Apache Kafka |
| Persistence | PostgreSQL (per-service DBs) |
| Caching | Redis |
| Resilience | Resilience4j |
| Security | Spring Security + JWT |
| Build | Maven (multi-module) |
| Infrastructure | Docker Compose |
| Testing | JUnit 5, Testcontainers *(planned)* |

---

## 🧩 Services

| Service | Port | Status | Responsibility |
|---|---|---|---|
| discovery-server | 8761 | ✅ Working | Eureka service registry |
| config-server | 8888 | ✅ Working | Centralized configuration |
| api-gateway | 8080 | 🚧 In progress | Single entry point, routing |
| user-service | 8081 | 🚧 Pending | Auth, users, JWT issuance |
| product-service | 8082 | 🚧 Pending | Product catalog, caching |
| order-service | 8083 | 🚧 Pending | Orders, Feign + Resilience4j |
| payment-service | 8084 | 🚧 Pending | Payments, idempotency |
| notification-service | 8085 | 🚧 Pending | Kafka consumer for events |

---

## 🚀 Running Locally

### Prerequisites

- Java 21+
- Maven 3.9+
- Docker + Docker Compose

### 1. Start infrastructure

```bash
cp .env.example .env    # first time only
docker compose up -d
# build the project
mvn clean install -DskipTests

#Run services (in ordeer)
mvn -pl discovery-server spring-boot:run
mvn -pl config-server spring-boot:run
mvn -pl api-gateway spring-boot:run
mvn -pl user-service spring-boot:run
# ... product-service, order-service, etc.
```
