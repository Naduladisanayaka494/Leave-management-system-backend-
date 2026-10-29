# Leave Management System - Backend API

Production-ready REST API for the **Employee Leave Management System** built with **Spring Boot 3 (Java 21)**, **Spring Security 6 with JWT**, **Spring Data JPA**, and relational database persistence.

---

## 🛠 Technology Stack
- **Framework**: Spring Boot 3.3.4 (Java 21 LTS)
- **Security**: Spring Security 6, JWT (JJWT 0.12.6), BCrypt Password Hashing
- **Database**: Relational H2 Database (File-persisted, MySQL & PostgreSQL compatible)
- **API Documentation**: Springdoc OpenAPI / Swagger UI
- **Testing**: JUnit 5, Mockito, MockMvc

---

## 🚀 Getting Started

### Prerequisites
- JDK 17 or JDK 21
- Maven (or use included `mvnw.cmd`)

### Running Locally
```cmd
.\mvnw.cmd spring-boot:run
```
*(Or run `.\run.bat`)*

The server will start on **`http://localhost:8080`**.

### Interactive API Explorer
- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI Docs**: [http://localhost:8080/api-docs](http://localhost:8080/api-docs)
- **H2 Database Console**: [http://localhost:8080/h2-console](http://localhost:8080/h2-console) (JDBC URL: `jdbc:h2:file:./data/leavedb`, User: `sa`, Password: *(blank)*)

---

## 🔑 Pre-seeded Evaluator Accounts
| Role & Name | Email | Password | Department |
|---|---|---|---|
| **Administrator** (Nadula Dissanayake) | `admin@ombits.com` | `admin123` | Executive |
| **Employee** (Kasun Perera) | `kasun@ombits.com` | `employee123` | Engineering |
| **Employee** (Dilani Jayawardena) | `dilani@ombits.com` | `employee123` | Human Resources |
| **Employee** (Nuwan Fernando) | `nuwan@ombits.com` | `employee123` | Product Design |
| **Employee** (Chamari Bandara) | `chamari@ombits.com` | `employee123` | Quality Assurance |

---

## 📡 API Endpoints

### 1. Authentication (`/api/auth`)
- `POST /api/auth/login` - Authenticate user and receive JWT token
- `POST /api/auth/register` - Register a new employee account
- `GET /api/auth/me` - Retrieve current authenticated user profile

### 2. Employees (`/api/employees`)
- `POST /api/employees` - Create employee with name, email, department (Admin/Auth)
- `GET /api/employees` - List all employees
- `GET /api/employees/{id}` - Get employee details by ID

### 3. Leave Requests (`/api/leaves`)
- `POST /api/leaves` - Submit a leave request (Annual, Casual, Medical)
- `GET /api/leaves` - List leave requests (supports `?status=` and `?employeeId=` filters)
- `GET /api/leaves/{id}` - Get leave request by ID
- `PATCH /api/leaves/{id}/approve` - Approve pending leave (Admin only)
- `PATCH /api/leaves/{id}/reject` - Reject pending leave with mandatory reason (Admin only)
- `GET /api/leaves/stats` - Leave summary metrics for dashboard

---

## 🧪 Automated Testing
Run the complete test suite:
```cmd
.\mvnw.cmd test
```
All business rules and validations (date ordering, overlapping leave prevention, immutable status transitions, and mandatory rejection notes) are covered by automated tests.
