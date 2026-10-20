# 🎓 EduPath: Student Connecting Platform

> **"The portal where seniors' experience becomes juniors' direction"**  
> EduPath is a 4-tier layered web application designed to connect university students, seniors, and alumni for mentorship, career guidance playbooks, interactive Q&A discussions, and AI-powered career roadmaps.

---

## 🚀 Tech Stack

- **Backend Framework:** Spring Boot 4.1.1 / Java 25
- **ORM & Data Access:** Spring Data JPA, Hibernate ORM
- **Security & Identity:** Spring Security with HMAC-SHA256 JWT tokens & BCrypt password hashing
- **Databases Supported:**
  - **H2 In-Memory:** Instant zero-config local run
  - **MySQL (Aiven Cloud / Local):** Production 8-table relational schema
- **Architecture:** 4-Tier Layered Architecture (Controller ➔ Service ➔ Repository ➔ Entity/Database)

---

## 🗄️ Database Architecture (Final 8-Table Schema)

| Table | Primary Key | Foreign Key(s) | Responsible / Notes |
| :--- | :--- | :--- | :--- |
| **`users`** | `user_id` (BIGINT AUTO_INCREMENT) | — | Core user accounts with BCrypt password hash and roles (`JUNIOR`, `SENIOR`, `ALUMNI`, `ADMIN`). |
| **`alumni_profiles`** | `alumni_id` (BIGINT) | `alumni_id` ➔ `users.user_id` | One-to-zero-or-one shared primary key; stores current company & job title. |
| **`playbook_posts`** | `post_id` (BIGINT AUTO_INCREMENT) | `author_id` ➔ `users.user_id` | Guidance posts (`DRAFT` / `PUBLISHED`). Application enforces `SENIOR` / `ALUMNI` publish rights. |
| **`questions`** | `question_id` (BIGINT AUTO_INCREMENT) | `author_id` ➔ `users.user_id` | Student Q&A discussion forum (`guide_post_id` intentionally omitted per spec). |
| **`answers`** | `answer_id` (BIGINT AUTO_INCREMENT) | `question_id` ➔ `questions.question_id`<br>`author_id` ➔ `users.user_id` | Responses with verified alumni answer badge (`is_verified_answer`). |
| **`roadmaps`** | `roadmap_id` (BIGINT AUTO_INCREMENT) | `user_id` ➔ `users.user_id` | Student career roadmap containers (AI-generated or custom created). |
| **`roadmap_nodes`** | `node_id` (BIGINT AUTO_INCREMENT) | `roadmap_id` ➔ `roadmaps.roadmap_id` | Milestone skills with semester and sequence order. |
| **`roadmap_edges`** | `(source_node_id, target_node_id)` | `source_node_id` ➔ `roadmap_nodes.node_id`<br>`target_node_id` ➔ `roadmap_nodes.node_id` | Composite primary key preventing duplicate directed prerequisite dependencies. |

---

## 👥 Pre-seeded Test Accounts

| Name | Role | Email | Password | Pre-seeded Features |
| :--- | :--- | :--- | :--- | :--- |
| **Vignesh K** | `JUNIOR` | `vignesh@edupath.edu` | `password123` | Questions asked & AI Roadmap created |
| **Aravind R** | `SENIOR` | `aravind@edupath.edu` | `password123` | Playbook author & Answers |
| **Deepa M** | `ALUMNI` | `deepa@amazon.com` | `password123` | Amazon SDE profile & Verified badge answers |
| **Administrator** | `ADMIN` | `admin@edupath.edu` | `admin123` | Full administrative privileges |

---

## ⚡ How to Run Locally

### Option 1: Instant Local Run (H2 In-Memory)
```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux / Mac
./mvnw spring-boot:run
```
- API Base URL: `http://localhost:8080`
- H2 Console: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:edupath_db`, User: `sa`, Password: *empty*)

### Option 2: Run with MySQL
Change `spring.profiles.active=mysql` in `application.properties` and start:
```bash
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=mysql"
```

---

## 📬 REST API Summary

### 1. Authentication & Identity (`/api/v1/auth`)
- `POST /api/v1/auth/register` — Register user (`JUNIOR`, `SENIOR`, `ALUMNI`, `ADMIN`)
- `POST /api/v1/auth/login` — Login and receive Bearer JWT token
- `GET /api/v1/auth/me` — Get current user profile (requires `Authorization: Bearer <token>`)

### 2. Alumni Profiles (`/api/v1/alumni`)
- `GET /api/v1/alumni` — List all alumni profiles
- `GET /api/v1/alumni/{id}` — Get profile by user ID
- `POST /api/v1/alumni` — Update logged-in alumnus company & title (`ALUMNI`, `ADMIN`)
- `PUT /api/v1/alumni/{id}` — Admin update for alumni profile (`ADMIN`)

### 3. Senior Guidance Playbooks (`/api/v1/playbooks`)
- `GET /api/v1/playbooks` — Junior Feed with filters (`?category=`, `?status=`, `?search=`)
- `GET /api/v1/playbooks/{id}` — Get playbook by ID
- `POST /api/v1/playbooks` — Publish playbook (`SENIOR`, `ALUMNI`, `ADMIN`)
- `PUT /api/v1/playbooks/{id}` — Update playbook (Author or `ADMIN`)
- `DELETE /api/v1/playbooks/{id}` — Delete playbook (Author or `ADMIN`)

### 4. Questions & Answers (`/api/v1/questions`)
- `GET /api/v1/questions` — List questions (`?category=`, `?search=`)
- `GET /api/v1/questions/{id}` — Get question with full answer thread
- `POST /api/v1/questions` — Submit question (Authenticated)
- `DELETE /api/v1/questions/{id}` — Delete question (Author or `ADMIN`)
- `POST /api/v1/questions/{id}/answers` — Post answer to question (Authenticated)
- `PATCH /api/v1/questions/answers/{id}/verify` — Mark/toggle Alumni Verified Answer badge (`ALUMNI`, `SENIOR`, `ADMIN`)
- `DELETE /api/v1/questions/answers/{id}` — Delete answer (Author or `ADMIN`)

### 5. AI Career Roadmaps (`/api/v1/roadmaps`)
- `POST /api/v1/roadmaps/generate` — AI Career Generator: generates roadmaps, milestone nodes & prerequisite edges
- `POST /api/v1/roadmaps` — Create manual roadmap
- `GET /api/v1/roadmaps` — List all roadmaps
- `GET /api/v1/roadmaps/{id}` — Get full roadmap graph with nodes and directed edges
- `POST /api/v1/roadmaps/{id}/nodes` — Add milestone node to roadmap (Owner or `ADMIN`)
- `POST /api/v1/roadmaps/{id}/edges` — Connect directed prerequisite edge between nodes (Owner or `ADMIN`)
- `DELETE /api/v1/roadmaps/{id}` — Delete roadmap (Owner or `ADMIN`)
- `DELETE /api/v1/roadmaps/nodes/{nodeId}` — Delete node (Owner or `ADMIN`)
- `DELETE /api/v1/roadmaps/edges?sourceNodeId={s}&targetNodeId={t}` — Delete edge (Owner or `ADMIN`)

### 6. Health Check (`/api/v1/health`)
- `GET /api/v1/health` — Check server status & API uptime
