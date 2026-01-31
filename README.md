# 🚀 Enterprise Task Management System

> **Status:** ✅ v1.1 
> **Course:** Backend Engineering Final Project
> **Stack:** Spring Boot 3.x, Java 21, MySQL

---

## ⚠️ SECURITY NOTICE
**Development Mode Only:** This repository contains a reference to environment variables.
* **Never commit** `.env` files or real credentials.
* **Secrets** are managed via `application.properties` using environment variables.

---

## 📖 1. Project Overview
**TaskFlow** is a production-grade backend system designed to simulate enterprise task management platforms. It moves beyond simple CRUD to offer a secure, scalable, and architecturally strict solution.

**Key Capabilities:**
* **Security:** Role-Based Access Control (RBAC) with stateless JWT authentication.
* **Performance:** Optimized database queries with Pagination and Sorting.
* **Architecture:** Strict separation of concerns (Controller vs Service vs Repository).
* **Reliability:** Global exception handling and strict input validation.

---

## 📂 2. Application Architecture
The project strictly follows a layered architecture:

```text
src/main/java/com/taskmanager/backend/
├── config/             # Security, Swagger, CORS configuration
├── controller/         # REST Controllers (Request/Response handling)
├── dto/                # Data Transfer Objects (API Contracts)
├── model/              # Database Entities (JPA)
├── repository/         # Data Access Layer (Interfaces)
├── service/            # Business Logic Layer
├── exception/          # Global Error Handling
└── util/               # Helper classes (JWT, DateUtils)
```

---

## 🛠️ 3. Technical Stack

| Component | Technology | Description |
| --- | --- | --- |
| **Core** | Spring Boot 3.2+ | Main Application Framework |
| **Language** | Java 21 (LTS) | Logic & Implementation |
| **Database** | MySQL 8.0+ | Relational Persistence |
| **Security** | Spring Security 6 | Auth & Authorization |
| **Token** | JWT (JJWT) | Stateless Session Management |
| **Docs** | OpenAPI (Swagger) | API Documentation |

---

## ⚡ 4. Setup & Installation

### Prerequisites

* **JDK 21** or higher
* MySQL Server (Port 3306)
* Maven

### Configuration

Set the following environment variables (or use a `.env` file):

```properties
# MySQL Configuration
DB_URL=jdbc:mysql://localhost:3306/taskflow_db?useSSL=false&serverTimezone=UTC
DB_USER=root
DB_PASSWORD=your_local_password

# JWT Secrets
JWT_SECRET=your_secure_random_secret_key_minimum_32_chars
JWT_EXPIRATION=86400000

# Email Configuration (Mailtrap - v1.1)
MAIL_HOST=sandbox.smtp.mailtrap.io
MAIL_PORT=2525
MAIL_USERNAME=your_mailtrap_username
MAIL_PASSWORD=your_mailtrap_password
```

### Running the App

```bash
# Clean and Run
./mvnw clean spring-boot:run
```

Access Swagger UI: `http://localhost:8080/swagger-ui.html`

---

## ✅ 5. Features Implemented

### 🔐 User Access

* [x] **Registration & Login** (BCrypt Password Hashing)
* [x] **JWT Authentication** (Bearer Token)
* [x] **Role Management** (Admin vs User)

### 📋 Task Management

* [x] **Create/Edit/Delete** Tasks
* [x] **Pagination:** Efficiently load large datasets
* [x] **Filtering:** Search by Priority or Status
* [x] **Soft Delete:** Data is preserved for auditing

### 🛡️ Quality & Stability

* [x] **DTO Pattern:** Entities are never exposed to the client.
* [x] **Validation:** `@Valid` checks on all inputs.
* [x] **Error Handling:** Standardized JSON error responses.

### 🔄 Integrations (v1.1)

* [x] **Email Service:** Asynchronous SMTP email notifications when tasks are created.

---

## 🧪 6. Testing

```bash
./mvnw test
```
