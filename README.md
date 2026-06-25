# Smart Incident Management Platform (IMP)

An enterprise-grade, cloud-native SaaS Incident Management Platform built with **Spring Boot 3, Angular 19, Kafka, Redis, and PostgreSQL**. 

This platform supports ticket lifecycles, automated SLA tracking, background escalations, rich comments (with user `@mentions`), S3-compatible file attachments, historical audit logs, and analytics metrics exporting.

---

## Technical Stack

- **Backend Microservices**: Java 21, Spring Boot 3.3.x, Spring Data JPA, Spring Security, Spring Cloud OpenFeign.
- **Frontend App**: Angular 19, TypeScript, RxJS, custom vanilla CSS styled for premium B2B high-density layouts (Inter font, neutral shadows).
- **Caching & Locks**: Redis (distributed locking for concurrency control, sequence counting).
- **Event Bus**: Apache Kafka (decoupling incident updates, comment notifications, and mention dispatches).
- **Email Server**: MailHog (simulated SMTP server with a web portal).
- **Database Engine**: PostgreSQL (decoupled database schema per service).

---

## System Architecture

```
                                +---------------------------+
                                |    Angular 19 Frontend    |
                                +-------------+-------------+
                                              |
                                              | HTTP / WebSockets
                                              v
                                +-------------+-------------+
                                |  Spring Cloud Gateway     | (Port 8080)
                                +------+------+------+------+
                                       |      |      |
            +--------------------------+      |      +--------------------------+
            | Path: /api/v1/auth/**           | Path: /api/v1/incidents/**      | Path: /api/v1/analytics/**
            | Path: /api/v1/users/**          |                                 | Path: /api/v1/reports/**
            v                                 v                                 v
+-----------+-----------+        +------------+------------+        +-----------+-----------+
|     Auth Service      |        |     Incident Service     |        |   Reporting Service   |
|     (Port 8081)       |        |     (Port 8082)          |        |     (Port 8083)       |
+-----------+-----------+        +------+-----+------------+        +-----------+-----------+
            |                           |     |                                 |
            v Read/Write                |     | Read/Write                      v Read Only
    +-------+-------+                   |   +-+-------------+             +-----+---------+
    | PostgreSQL DB |                   |   | PostgreSQL DB |             | PostgreSQL DB |
    |   (auth_db)   |                   |   | (incident_db) |             | (incident_db) |
    +---------------+                   |   +---------------+             +---------------+
                                        |
                                        v Publish Events
                                +-------+-------+
                                | Apache Kafka  | (Topic: incident-events)
                                +-------+-------+
                                        |
                                        v Consume Events
                                +-------+-------+
                                | Notification  |
                                |    Service    | (Port 8084) -> Dispatches to MailHog
                                +---------------+
```

---

## API Endpoints

### 1. Gateway Routing (`8080`)
All paths are routed dynamically through the gateway proxy:
- `POST /api/v1/auth/login` - Sign in.
- `POST /api/v1/auth/refresh` - Refresh JWT token.
- `GET /api/v1/auth/me` - Fetch active user profile.
- `POST /api/v1/users` - Register a new user profile.
- `GET /api/v1/users` - Paginated directory listing.
- `GET /api/v1/incidents` - Advanced paginated, criteria-based incident query search.
- `POST /api/v1/incidents` - Create ticket.
- `PUT /api/v1/incidents/{id}` - Modify parameters (assignee, priority, status transitions).
- `POST /api/v1/comments/incident/{id}` - Post comment note.
- `POST /api/v1/attachments/incident/{id}` - Upload files.
- `GET /api/v1/analytics/dashboard` - Get MTTR, MTTA, and distribution breakdown.
- `GET /api/v1/reports/pdf` - Download compliance PDF summaries.

---

## Seeding & Demo Access

On startup, the system seeds 3 departments, 3 teams, and users for each organizational tier:

| Username | Role | Department / Team | Password |
|---|---|---|---|
| `superadmin` | `SUPER_ADMIN` | IT | `adminpassword` |
| `admin` | `ADMIN` | IT | `adminpassword` |
| `manager` | `INCIDENT_MANAGER` | IT | `adminpassword` |
| `lead` | `TEAM_LEAD` | OPS / DevOps & Cloud | `adminpassword` |
| `engineer` | `SUPPORT_ENGINEER` | OPS / DevOps & Cloud | `adminpassword` |
| `employee` | `EMPLOYEE` | CS / L2 Support | `adminpassword` |

---

## Execution Guide

### Local Development Setup

To run backing services (databases, caches, event streams) locally:
```bash
cd docker
docker compose up -d postgres redis kafka mailhog
```

Build the backend services:
```bash
mvn clean package -DskipTests
```
Launch individual microservices by executing their respective jar files or via IDE target points.

Build and launch the Angular frontend:
```bash
cd frontend
npm install
npm run start
```
Access the client dashboard at `http://localhost:4200`.

---

## Production Deployment Guide

### Single Command Docker Setup

To build and run the entire microservice ecosystem (including the compiled Angular app) under a single internal bridged network:
```bash
docker compose -f docker/docker-compose.yml up --build -d
```

Verify running containers:
```bash
docker compose -f docker/docker-compose.yml ps
```

- **Frontend Console**: `http://localhost` (Port 80)
- **API Gateway Manager**: `http://localhost:8080`
- **MailHog Inbox**: `http://localhost:8025`

### Kubernetes Setup

Ensure a local cluster (Minikube or Kind) is running:
```bash
kubectl apply -f k8s/infrastructure.yaml
kubectl apply -f k8s/services.yaml
```
Verify routing endpoints:
```bash
kubectl get svc
```
