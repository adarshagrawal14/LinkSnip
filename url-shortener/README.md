# ⚡ LinkSnip — Backend Service

[![Live Demo](https://img.shields.io/badge/Frontend-linksnip--sage.vercel.app-blue?style=for-the-badge&logo=vercel)](https://linksnip-sage.vercel.app/)
[![Production API](https://img.shields.io/badge/API-Render-black?style=for-the-badge&logo=render)](https://linksnip-t6fk.onrender.com)
[![Swagger UI](https://img.shields.io/badge/Swagger-OpenAPI-green?style=for-the-badge&logo=swagger)](https://linksnip-t6fk.onrender.com/swagger-ui.html)
[![Java 21](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk)](https://adoptium.net)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1.0-brightgreen?style=for-the-badge&logo=springboot)](https://spring.io/projects/spring-boot)

The backend service for **LinkSnip**, a high-performance URL shortener built with Java 21, Spring Boot 4.1, PostgreSQL 16, and Redis 7.

---

## 🔗 Live Endpoints

- **API Base**: [https://linksnip-t6fk.onrender.com](https://linksnip-t6fk.onrender.com)
- **Interactive Swagger Documentation**: [https://linksnip-t6fk.onrender.com/swagger-ui.html](https://linksnip-t6fk.onrender.com/swagger-ui.html)
- **Actuator Health**: [https://linksnip-t6fk.onrender.com/actuator/health](https://linksnip-t6fk.onrender.com/actuator/health)

---

## 🏛️ Architecture & System Design

```mermaid
flowchart LR
    Client["Client / Frontend"] -->|HTTP| RateLimit["Bucket4j RateLimiter"]
    RateLimit --> Security["Spring Security"]
    Security --> Controller["Controllers"]
    Controller --> Service["UrlService"]
    Service <-->|1. Check Cache| Redis[("Redis 7 Cache")]
    Service <-->|2. Fallback DB Query| Postgres[("PostgreSQL 16")]
    Controller -.->|Async Track| Worker["ClickTrackingService (@Async)"]
    Worker --> Postgres
```

### Key Capabilities:
- **Base62 Sequence Encoding**: Monotonically increasing database sequence ensures 100% collision-free short codes.
- **Cache-Aside Pattern (Redis)**: Sub-millisecond redirects with dynamic TTL matching link expiry.
- **Distributed Token-Bucket Rate Limiter**: Per-IP limits using Bucket4j and Lettuce.
- **Asynchronous Click Analytics**: Non-blocking event logging using Spring `@Async` threads.
- **GDPR IP Hashing**: Salted SHA-256 IP hashing protects visitor privacy.

---

## 📡 API Reference

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/v1/urls` | Create a short URL (`longUrl`, optional `customAlias`, optional `expiresAt`) |
| `GET` | `/{shortCode}` | Follow short URL redirect (`302 Found`) |
| `GET` | `/api/v1/urls/{shortCode}` | Fetch details of a short link |
| `GET` | `/api/v1/urls/{shortCode}/analytics` | Click count and referrer statistics |
| `DELETE` | `/api/v1/urls/{shortCode}` | Soft-delete / deactivate a short link |
| `GET` | `/actuator/health` | Service health status |

---

## 🛠️ Quick Start

```bash
docker compose up --build
```
API is available at `http://localhost:8080`.
