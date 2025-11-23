# CSV Import Service

A service that accepts CSV files, creates **File Upload Jobs**, publishes them to **Kafka** for asynchronous processing, and tracks their lifecycle. Built with a domain-driven design (DDD) approach.

---

## 🧠 Domain-Driven Design (DDD)

Traditional designs treat uploads as mere file operations. That approach leaks infrastructure concerns into the API and hides the real business behavior.

Here, **File Upload Jobs** are the aggregate root:

```
FileUploadJob
├── id (UUID)
├── filePath
├── status: PENDING | PROCESSING | COMPLETED | FAILED
├── createdAt
├── completedAt
└── errors[] (row-level issues)
```

This enables:

* `/api/v1/files` — create new jobs
* `/api/v1/files/{id}` — get job status
* `/api/v1/files` — list all jobs

---

## 🚀 Architecture Overview

1. Client uploads a CSV to **`POST /api/v1/files`**.
2. Service stores the file locally.
3. A `FileUploadJob` is persisted in the database.
4. Job ID is published to **Kafka**.
5. Worker service consumes the job ID, reads the file, processes rows.
6. Job transitions state → PROCESSING → COMPLETED or FAILED.

---

## 📡 Why Kafka?

Kafka simulates **GCP Pub/Sub** locally:

* Distributed commit log
* At-least-once delivery
* Scalable consumption
* Consumer groups & partitions

---

## 🛠 Technology Stack

### Backend

* Kotlin + Spring Boot 3
* Kafka (local simulation for Pub/Sub)
* Apache Commons CSV
* Local filesystem for file storage

### Frontend

* Angular 21
* Simple UI to upload CSVs and monitor job status

### Build & Packaging

* Gradle (Kotlin DSL)
* Docker Compose (Kafka, optional containerized backend/frontend)

---

## 🧩 API Endpoints

### Create a New File Upload Job

```
POST /api/v1/files
Content-Type: multipart/form-data

Body:
- file: CSV file
```

**Response:**

```json
{
  "id": "uuid",
  "status": "PENDING",
  "createdAt": "2025-11-23T20:10:00Z",
  "completedAt": null,
  "errors": []
}
```

---

### Get Job by ID

```
GET /api/v1/files/{id}
```

**Response:**

```json
{
  "id": "uuid",
  "status": "PROCESSING",
  "createdAt": "...",
  "completedAt": null,
  "errors": []
}
```

---

### List All Jobs

```
GET /api/v1/files
```

**Response:**

```json
[
  {
    "id": "uuid1",
    "status": "COMPLETED",
    "createdAt": "...",
    "completedAt": "...",
    "errors": []
  },
  {
    "id": "uuid2",
    "status": "FAILED",
    "createdAt": "...",
    "completedAt": "...",
    "errors": ["Row 3 missing email"]
  }
]
```

---

## 🔧 Running the Application

There are **multiple ways** to run this project:

### Option 1: Docker Compose (Recommended for Production / Local Full Stack)

```bash
docker-compose up -d
```

This starts:

* Kafka
* Backend (Spring Boot + FileUploadService)
* Optionally frontend container (Nginx serving Angular)

**Ports:**

* Backend: `http://localhost:8080`
* Frontend: `http://localhost:4200` (if containerized)

---

### Option 2: Run Backend and Frontend Separately (Recommended for Development)

**Start Kafka:**

```bash
docker-compose up -d kafka
```

**Run Backend:**

```bash
./gradlew bootRun
```

Backend is available on `http://localhost:8080`.

**Run Angular Frontend:**

```bash
cd frontend
npm install
ng serve --port 4200
```

Frontend is available on `http://localhost:4200`.

> This bypasses Dockerized Nginx, enabling live reload during development.

---

### Option 3: Build Docker Images for Backend and Frontend Separately

**Run via `docker-compose.yml`:**

```yaml
backend:
  image: ghcr.io/yelpalekshitij/pub-sub-csv-import-service/file-upload-service:latest
  container_name: file-upload-service
  ports:
    - "8080:8080"
  environment:
    SPRING_PROFILES_ACTIVE: docker
    KAFKA_BOOTSTRAP_SERVERS: kafka:9092
  depends_on:
    - kafka

frontend:
  image: ghcr.io/yelpalekshitij/pub-sub-csv-import-service/frontend:latest
  container_name: frontend
  ports:
    - "4200:80"
  depends_on:
    - backend
```

> Useful for staging or production environments where rebuilding locally is not needed.

---

## ⚡ Improvements / TODO

* Due to time constraints, the following are not implemented yet:

* Persistence Layer – Currently jobs are in memory. Replace with database + JPA.

* Authentication & Authorization – Add secure login for backend and frontend.

* Enhanced Frontend UI – Improve styling, usability, responsive design.

* Integration Tests with TestContainers – For Kafka and database.

* Swagger / OpenAPI Documentation – API docs for easier exploration.

* Retry / Batch Processing Enhancements – Currently simple sequential processing.

---

## ✔ Summary

* **DDD-aligned**: `FileUploadJob` is the true domain aggregate.
* **Kafka**: Simulates Pub/Sub for asynchronous, scalable processing.
* **Flexible execution**: Run locally via commands for development or Docker for full-stack deployment.
* **Angular Frontend**: Minimal interface for CSV upload and job monitoring.
