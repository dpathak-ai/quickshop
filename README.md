# QuickShop

A cloud-native e-commerce backend built as a **Java 21 / Spring Boot 4** microservices system. The full stack — 7 services plus all infrastructure — starts with a single `docker compose up`.

---

## Table of Contents

- [Architecture](#architecture)
- [Services](#services)
- [Tech Stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
  - [Run with Docker Compose (recommended)](#run-with-docker-compose-recommended)
  - [Run locally (without Docker)](#run-locally-without-docker)
- [Environment Variables](#environment-variables)
- [API Overview](#api-overview)
- [Observability](#observability)
- [Debugging & Logs](#debugging--logs)
- [Project Structure](#project-structure)

---

## Architecture

```
                        ┌─────────────────────────────────────────────┐
                        │              quickshop-network               │
                        │                                              │
  Client ──────────► [ api-gateway :8080 ]                            │
                        │       │  (Spring Cloud Gateway + Eureka lb)  │
           ┌────────────┼───────┼────────────────────┐                │
           │            │       │                    │                │
    [ user-service   [ product-service   [ order-service             │
        :8081 ]          :8082 ]              :8084 ]                 │
           │                             [ inventory-service          │
           │                                  :8083 ]                 │
           │                             [ notification-service       │
           │                                  :8085 ]                 │
           │                                                          │
           └──────── Service Discovery ──────────────────────────────┘
                    [ service-registry :8761 ] (Eureka)

Infrastructure
  ├── MySQL          :3306   (shared database: quickshop)
  ├── Redis          :6379   (cache + session store)
  ├── Kafka          :9092   (event streaming)
  ├── Zookeeper      :2181   (Kafka coordination)
  ├── Kafka UI       :9000   (Kafka management console)
  ├── Jaeger         :16686  (distributed tracing)
  ├── Prometheus     :9090   (metrics scraping)
  └── Grafana        :3000   (metrics dashboards)
```

---

## Services

| Service | Port | Responsibilities |
|---|---|---|
| `api-gateway` | 8080 | Single entry point — routes all `/api/**` traffic to downstream services via Eureka load-balancing |
| `service-registry` | 8761 | Netflix Eureka server — service discovery for all microservices |
| `user-service` | 8081 | User registration, login, JWT issuance & refresh, profile management |
| `product-service` | 8082 | Product catalog — CRUD, versioned API (v1/v2), Redis-backed caching |
| `order-service` | 8084 | Order placement, Outbox pattern for reliable Kafka publishing, Resilience4j circuit breaker |
| `inventory-service` | 8083 | Stock management, Kafka consumer/producer, Redis cache |
| `notification-service` | 8085 | Stateless Kafka consumer — listens for order/inventory events and dispatches notifications |

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.0.5 |
| Service Discovery | Spring Cloud Netflix Eureka 2025.1.1 |
| API Gateway | Spring Cloud Gateway (WebFlux / reactive) |
| Persistence | Spring Data JPA + MySQL 8 |
| Caching | Spring Cache + Redis 7 (Lettuce) |
| Messaging | Apache Kafka (Confluent 7.6) |
| Security | Spring Security + JWT (JJWT 0.12) |
| Resilience | Resilience4j (Circuit Breaker, Retry, TimeLimiter) |
| Service Communication | OpenFeign |
| Tracing | Micrometer Tracing → OpenTelemetry → Jaeger (OTLP HTTP) |
| Metrics | Micrometer + Prometheus + Grafana |
| Build | Gradle 8 (multi-module) |
| Containerisation | Docker + Docker Compose |

---

## Prerequisites

| Tool | Minimum version |
|---|---|
| Docker Desktop | 4.x |
| Docker Compose | v2 (`docker compose` not `docker-compose`) |
| Java JDK *(local dev only)* | 21 |

---

## Getting Started

### Run with Docker Compose (recommended)

```bash
# 1. Clone the repository
git clone <repo-url>
cd quickshop

# 2. Start the full stack (first run builds all images — takes ~5-10 min)
docker compose --env-file .env up --build -d

# 3. Check all 15 containers are healthy
docker compose ps

# 4. Tail consolidated logs
docker compose logs -f
```

**Startup order is automatic** — health checks ensure infra is ready before services start, and `service-registry` is healthy before any business service connects to it.

#### Stop everything
```bash
docker compose down

# Stop AND wipe all volumes (fresh DB, etc.)
docker compose down -v
```

---

### Run locally (without Docker)

> Infrastructure (MySQL, Redis, Kafka, Jaeger) must still be running. Start just the infra:

```bash
docker compose up -d mysql redis zookeeper kafka jaeger prometheus grafana
```

Then run each service from its own directory:

```bash
# From the project root
./gradlew :service-registry:bootRun
./gradlew :user-service:bootRun
./gradlew :product-service:bootRun
./gradlew :inventory-service:bootRun
./gradlew :order-service:bootRun
./gradlew :notification-service:bootRun
./gradlew :api-gateway:bootRun
```

All `application.yaml` files default to `localhost` for every connection, so no extra config is needed for local runs.

---

## Environment Variables

All variables live in `.env` at the project root. Docker Compose reads this file automatically via `--env-file .env`.

| Variable | Default | Description |
|---|---|---|
| `MYSQL_ROOT_PASSWORD` | `root` | MySQL root password |
| `MYSQL_USER` | `quickshop` | Application DB user |
| `MYSQL_PASSWORD` | `quickshop` | Application DB password |
| `MYSQL_DATABASE` | `quickshop` | Database name |
| `REDIS_HOST` | `quickshop-redis` | Redis container hostname |
| `REDIS_PORT` | `6379` | Redis port |
| `KAFKA_BOOTSTRAP` | `quickshop-kafka:29092` | Kafka internal broker address |
| `EUREKA_URL` | `http://service-registry:8761/eureka/` | Eureka server URL |
| `JAEGER_OTLP` | `http://quickshop-jaeger:4318/v1/traces` | Jaeger OTLP endpoint |
| `JWT_SECRET` | *(base64 key)* | Shared JWT signing secret |
| `GF_SECURITY_ADMIN_USER` | `admin` | Grafana admin username |
| `GF_SECURITY_ADMIN_PASSWORD` | `admin` | Grafana admin password |

> ⚠️ Change `JWT_SECRET`, database passwords, and Grafana credentials before any production deployment.

---

## API Overview

All requests go through the API Gateway at `http://localhost:8080`.

### Auth (user-service)
| Method | Path | Description |
|---|---|---|
| `POST` | `/api/v1/auth/register` | Register a new user |
| `POST` | `/api/v1/auth/login` | Login — returns JWT access + refresh tokens |
| `POST` | `/api/v1/auth/refresh` | Refresh access token |

### Users (user-service)
| Method | Path | Description |
|---|---|---|
| `GET` | `/api/v1/users/me` | Get current user profile |
| `PUT` | `/api/v1/users/me` | Update profile |

### Products (product-service)
| Method | Path | Description |
|---|---|---|
| `GET` | `/api/v1/products` | List all products |
| `GET` | `/api/v1/products/{id}` | Get product by ID |
| `POST` | `/api/v1/products` | Create product *(admin)* |
| `PUT` | `/api/v1/products/{id}` | Update product *(admin)* |
| `DELETE` | `/api/v1/products/{id}` | Delete product *(admin)* |
| `GET` | `/api/v2/products` | List products (v2 — extended response) |

### Orders (order-service)
| Method | Path | Description |
|---|---|---|
| `POST` | `/api/v1/orders` | Place a new order |
| `GET` | `/api/v1/orders` | List user's orders |
| `GET` | `/api/v1/orders/{id}` | Get order details |

### Inventory (inventory-service)
| Method | Path | Description |
|---|---|---|
| `GET` | `/api/v1/inventory/{productId}` | Check stock level |
| `PUT` | `/api/v1/inventory/{productId}` | Update stock *(admin)* |

---

## Observability

| Tool | URL | Credentials |
|---|---|---|
| **Eureka Dashboard** | http://localhost:8761 | — |
| **Kafka UI** | http://localhost:9000 | — |
| **Jaeger (traces)** | http://localhost:16686 | — |
| **Prometheus** | http://localhost:9090 | — |
| **Grafana** | http://localhost:3000 | admin / admin |

### Distributed Tracing

Every service injects `traceId` and `spanId` into every log line:

```
2026-04-25 10:23:41 INFO  [traceId=4bf92f3577b34da6] [order-service] ...
```

Open **Jaeger UI → Search → select a service → Find Traces** to visualise the full request journey across all services.

### Metrics

Prometheus scrapes `/actuator/prometheus` on all 7 services every 15 seconds. Import a **Spring Boot dashboard** (Grafana dashboard ID `**6756**`) in Grafana to get JVM, HTTP, and cache metrics out of the box.

---

## Debugging & Logs

```bash
# All services — live tail
docker compose logs -f

# Single service
docker compose logs -f order-service

# Last 100 lines across all services
docker compose logs --tail=100 -f

# Find all errors
docker compose logs --tail=200 | grep -i "error\|exception\|caused by"

# Trace a single request across all services using its traceId
docker compose logs --tail=500 | grep "traceId=<paste-traceId-here>"

# Logs from the last 5 minutes
docker compose logs --since=5m -f
```

---

## Project Structure

```
quickshop/
├── api-gateway/                # Spring Cloud Gateway (WebFlux)
│   ├── src/
│   └── Dockerfile
├── service-registry/           # Netflix Eureka Server
│   ├── src/
│   └── Dockerfile
├── user-service/               # Auth, JWT, user management
│   ├── src/
│   └── Dockerfile
├── product-service/            # Product catalog + Redis cache
│   ├── src/
│   └── Dockerfile
├── order-service/              # Orders, Outbox pattern, Resilience4j
│   ├── src/
│   └── Dockerfile
├── inventory-service/          # Stock management + Kafka
│   ├── src/
│   └── Dockerfile
├── notification-service/       # Stateless Kafka consumer
│   ├── src/
│   └── Dockerfile
├── grafana/
│   └── provisioning/           # Auto-provisioned Grafana datasources
├── docker-compose.yml          # Full stack — infra + all 7 services
├── prometheus.yml              # Prometheus scrape config
├── init-db.sql                 # MySQL database initialisation
├── .env                        # Environment variables (do not commit in prod)
├── build.gradle                # Root Gradle build (common deps for all services)
└── settings.gradle             # Multi-module project declaration
```
