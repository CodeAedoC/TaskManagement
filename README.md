# 🚀 Enterprise Task Management System

> **Status:** ✅ v1.2  
> **Course:** Backend Engineering Final Project  
> **Stack:** Spring Boot 3.2, Java 21, MySQL

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
* **Email Verification:** Complete email verification flow with HTML templates.
* **Performance:** Optimized database queries with Pagination and Sorting.
* **Architecture:** Strict separation of concerns (Controller → Service → Repository).
* **Reliability:** Global exception handling and strict input validation.

---

## 📂 2. Application Architecture
The project strictly follows a layered architecture:

```text
src/main/java/com/taskmanager/backend/
├── config/             # Security, Swagger, CORS, JWT configuration
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
| **Core** | Spring Boot 3.2.1 | Main Application Framework |
| **Language** | Java 21 (LTS) | Logic & Implementation |
| **Database** | MySQL 8.0+ | Relational Persistence |
| **Security** | Spring Security 6 | Auth & Authorization |
| **Token** | JWT (JJWT 0.12.3) | Stateless Session Management |
| **Docs** | OpenAPI (Swagger) | API Documentation |
| **Email** | Spring Mail (SMTP) | Email Notifications |
| **Config** | Spring DotEnv | Environment Variable Management |

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
DB_URL=jdbc:mysql://localhost:3306/taskflow_db?useSSL=false&serverTimezone=UTC&createDatabaseIfNotExist=true
DB_USER=root
DB_PASSWORD=your_local_password

# JWT Secrets
JWT_SECRET=your_secure_random_secret_key_minimum_32_chars
JWT_EXPIRATION=86400000

# Email Configuration (Mailtrap - Development)
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

### 🔐 User Authentication & Authorization

* [x] **User Registration** (BCrypt Password Hashing)
* [x] **Email Verification** – HTML-formatted verification emails with clickable links
* [x] **Resend Verification** – Endpoint to resend verification emails
* [x] **Token Expiry** – Verification tokens expire after 24 hours
* [x] **JWT Authentication** (Bearer Token)
* [x] **Role Management** (Admin vs User)

### 📧 Email Service

* [x] **HTML Email Templates** – Professional email design with branding
* [x] **Verification Emails** – Clickable "Verify Email" button
* [x] **Task Assignment Emails** – Notifications when tasks are created
* [x] **Async Email Sending** – Non-blocking email delivery with `@Async`

### 📋 Task Management

* [x] **Create/Edit/Delete** Tasks
* [x] **Get Task by ID** – Retrieve specific task details
* [x] **Pagination:** Efficiently load large datasets
* [x] **Sorting:** Sort by any field (ascending/descending)
* [x] **Filtering:** Search by Priority or Status
* [x] **Soft Delete:** Data is preserved for auditing
* [x] **User Ownership:** Tasks are tied to authenticated users

### 🛡️ Quality & Stability

* [x] **DTO Pattern:** Entities are never exposed to the client.
* [x] **Validation:** `@Valid` checks on all inputs.
* [x] **Error Handling:** Standardized JSON error responses.
* [x] **Global Exception Handler:** Centralized error handling with custom exceptions.

---

## 📡 6. API Endpoints

### Authentication Endpoints

| Method | Endpoint | Description |
| --- | --- | --- |
| `POST` | `/api/auth/register` | Register a new user |
| `POST` | `/api/auth/login` | Login and receive JWT token |
| `GET` | `/api/auth/verify-email` | Verify email with token |
| `POST` | `/api/auth/resend-verification` | Resend verification email |

### Task Endpoints (Requires Authentication)

| Method | Endpoint | Description |
| --- | --- | --- |
| `GET` | `/api/tasks` | Get all tasks (paginated) |
| `GET` | `/api/tasks/{id}` | Get task by ID |
| `POST` | `/api/tasks` | Create a new task |
| `PUT` | `/api/tasks/{id}` | Update an existing task |
| `DELETE` | `/api/tasks/{id}` | Soft delete a task |

### Query Parameters for Tasks

| Parameter | Type | Default | Description |
| --- | --- | --- | --- |
| `page` | int | 0 | Page number (0-based) |
| `size` | int | 10 | Page size |
| `sortBy` | string | createdAt | Field to sort by |
| `sortDir` | string | desc | Sort direction (asc/desc) |
| `status` | enum | - | Filter by status |
| `priority` | enum | - | Filter by priority |

---

## 📊 7. Data Models

### User Entity
| Field | Type | Description |
| --- | --- | --- |
| `id` | Long | Primary key (auto-generated) |
| `username` | String | Unique username |
| `email` | String | Unique email address |
| `password` | String | BCrypt hashed password |
| `emailVerified` | Boolean | Email verification status |
| `verificationToken` | String | UUID token for email verification |
| `verificationTokenExpiry` | LocalDateTime | Token expiration time |
| `roles` | Set<Role> | User roles (RBAC) |

### Task Entity
| Field | Type | Description |
| --- | --- | --- |
| `id` | Long | Primary key (auto-generated) |
| `title` | String | Task title |
| `description` | String | Task description |
| `priority` | Enum | LOW, MEDIUM, HIGH, CRITICAL |
| `status` | Enum | PENDING, IN_PROGRESS, COMPLETED, CANCELLED |
| `dueDate` | LocalDateTime | Task due date |
| `createdAt` | LocalDateTime | Creation timestamp |
| `updatedAt` | LocalDateTime | Last update timestamp |
| `isDeleted` | Boolean | Soft delete flag |

---

## 🧪 8. Testing

```bash
# Run all tests
./mvnw test
```

---

## 📝 9. Changelog

### v1.2 (Current)
* ✅ Added email verification with HTML templates
* ✅ Added resend verification endpoint
* ✅ Improved verification email with clickable button design
* ✅ Added task assignment email notifications
* ✅ Implemented async email sending for better performance

### v1.1
* ✅ Initial email service integration with Mailtrap
* ✅ Basic SMTP email notifications for tasks

### v1.0
* ✅ Core authentication system (JWT)
* ✅ Task CRUD operations with pagination
* ✅ Role-based access control
* ✅ Global exception handling
* ✅ OpenAPI/Swagger documentation
