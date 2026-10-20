# 📘 EduPath: Comprehensive API & File-by-File Architecture Documentation

> **Project Name:** EduPath (Student Connecting Platform)  
> **Course:** 3rd Semester Java / Web Backend Project  
> **Tech Stack:** Java 25, Spring Boot 4.1.1, Spring Data JPA, Hibernate, MySQL / H2, Spring Security  
> **Architecture:** 4-Tier Layered Architecture (Controller ➔ Service ➔ Repository ➔ Entity/Database)

---

## 📑 Table of Contents
1. [Architectural Overview & Design Pattern](#1-architectural-overview--design-pattern)
2. [Complete File-by-File Directory Guide](#2-complete-file-by-file-directory-guide)
   - [Configuration Layer](#configuration-layer-config)
   - [Entity / Domain Model Layer](#entity--domain-model-layer-model)
   - [Data Access Layer](#data-access-layer-repository)
   - [Business Logic Layer](#business-logic-layer-service)
   - [REST API Controller Layer](#rest-api-controller-layer-controller)
   - [Data Transfer Object (DTO) Layer](#data-transfer-object-dto-layer-dto)
   - [Exception Handling Layer](#exception-handling-layer-exception)
   - [Configuration & Resource Files](#configuration--resource-files-resources)
3. [Complete REST API Reference (By Screen & Feature)](#3-complete-rest-api-reference)
   - [Screen 1: Junior Feed & Insights](#screen-1-junior-feed--insights)
   - [Screen 2: Senior Guidance Editor](#screen-2-senior-guidance-editor)
   - [Screen 3: Dynamic Graph Roadmap (vis-network)](#screen-3-dynamic-graph-roadmap-vis-network)
   - [Screen 4: AI Career Path Generator (Gemini)](#screen-4-ai-career-path-generator-gemini)
   - [Mentorship Q&A & Discussion Forum](#mentorship-qa--discussion-forum)
   - [Authentication & Role Management](#authentication--role-management)
   - [System Health Check](#system-health-check)
4. [College Viva & Presentation Q&A](#4-college-viva--presentation-qa)

---

## 1. Architectural Overview & Design Pattern

EduPath implements the industry-standard **Layered Architecture (Separation of Concerns)**:

```
┌────────────────────────────────────────────────────────┐
│                   Client / Frontend                    │
│   (CampusConnect HTML5 / vis-network.js / Postman)     │
└───────────────────────────┬────────────────────────────┘
                            │ HTTP JSON Requests
                            ▼
┌────────────────────────────────────────────────────────┐
│                    Controller Layer                    │
│  (Validates input, maps HTTP routes, returns JSON)     │
└───────────────────────────┬────────────────────────────┘
                            │ Calls Service methods
                            ▼
┌────────────────────────────────────────────────────────┐
│                     Service Layer                      │
│   (Business logic, transactions, mappings, algorithms) │
└───────────────────────────┬────────────────────────────┘
                            │ Uses Spring Data JPA
                            ▼
┌────────────────────────────────────────────────────────┐
│                   Repository Layer                     │
│    (Database abstraction, SQL/JPQL queries, caching)   │
└───────────────────────────┬────────────────────────────┘
                            │ Hibernate ORM
                            ▼
┌────────────────────────────────────────────────────────┐
│              Database (MySQL / H2 In-Memory)           │
│   (users, playbook_posts, questions, answers, nodes)   │
└────────────────────────────────────────────────────────┘
```

---

## 2. Complete File-by-File Directory Guide

### Configuration Layer (`config/`)

#### 1. `SecurityConfig.java`
- **Location:** `com.example.edupath.demo.config.SecurityConfig`
- **Purpose:** Configures Spring Security filter chains for modern stateless REST APIs.
- **Why it was created:**
  - By default, Spring Security locks all URLs and generates a random password, requiring form login or basic authentication.
  - This file permits all requests to `/api/**` and `/h2-console/**` so frontend apps, Postman, and mobile clients can communicate without being blocked by 401 Unauthorized.
  - Disables CSRF (Cross-Site Request Forgery) because REST APIs are stateless and don't use session cookies.
  - Configures CORS (Cross-Origin Resource Sharing) allowing frontend applications (e.g. React/Vite on `localhost:3000` or `5173`) to make API calls.

#### 2. `DataInitializer.java`
- **Location:** `com.example.edupath.demo.config.DataInitializer`
- **Purpose:** Automatically seeds realistic demo data on application boot if the database is empty.
- **Why it was created:**
  - Implements Spring Boot's `CommandLineRunner`.
  - Directly seeds the exact items shown in the architecture diagram:
    - Sample Senior Playbooks (*"Semester 1 & 2 Blueprint"*, *"Off-Campus Internship Strategy"*).
    - vis-network interactive roadmap nodes (*"1. C & Pointers"*, *"2. Data Structures"*, *"LeetCode Top 150"*).
    - Verified Q&A answers from Senior Alumni (Amazon).
    - Pre-hashed user accounts for Junior and Senior roles.
  - Ensures professors and evaluators immediately see live data without manual SQL inserts.

---

### Entity / Domain Model Layer (`model/`)

These classes represent database tables using JPA (`jakarta.persistence.*`) annotations matching the final 8-table schema design.

#### 3. `User.java`
- **Table:** `users`
- **Purpose:** Stores user profiles, authentication credentials, and roles.
- **Fields:** `user_id` (PK, auto-increment), `reg_no` (VARCHAR(30) unique, nullable), `name` (VARCHAR(100) not null), `email` (VARCHAR(150) unique not null), `hashed_password` (VARCHAR(255) not null, BCrypt), `role` (VARCHAR(20) not null), `created_at` (TIMESTAMP).

#### 4. `Role.java`
- **Type:** Java Enum
- **Values:** `JUNIOR`, `SENIOR`, `ALUMNI`, `ADMIN`
- **Purpose:** Enforces role-based permissions across endpoints (e.g. only Seniors/Alumni can publish playbooks, only Alumni can get verified badge).

#### 5. `AlumniProfile.java`
- **Table:** `alumni_profiles`
- **Purpose:** Stores alumnus professional information (company and role).
- **Fields:** `alumni_id` (PK and FK to `users.user_id`), `current_company` (VARCHAR(150)), `job_title` (VARCHAR(150)).
- **Relationship:** One-to-zero-or-one shared primary key with `users`.

#### 6. `PlaybookPost.java`
- **Table:** `playbook_posts`
- **Purpose:** Represents guidance guides created by seniors/alumni for the Junior Feed.
- **Fields:** `post_id` (PK, auto-increment), `author_id` (FK to `users.user_id`), `title` (VARCHAR(200)), `markdown_content` (TEXT), `category` (VARCHAR(40)), `status` (VARCHAR(20): `DRAFT`/`PUBLISHED`), `created_at` (TIMESTAMP).

#### 7. `Question.java`
- **Table:** `questions`
- **Purpose:** Represents discussion questions asked by students on the mentorship forum.
- **Fields:** `question_id` (PK, auto-increment), `author_id` (FK to `users.user_id`), `title` (VARCHAR(200)), `question_body` (TEXT), `category` (VARCHAR(40)), `created_at` (TIMESTAMP).
- **Note:** `guide_post_id` is intentionally omitted per agreed final schema.

#### 8. `Answer.java`
- **Table:** `answers`
- **Purpose:** Stores responses to questions, supporting the senior alumni verified badge.
- **Fields:** `answer_id` (PK, auto-increment), `question_id` (FK to `questions.question_id`), `author_id` (FK to `users.user_id`), `answer_body` (TEXT), `is_verified_answer` (BOOLEAN default FALSE), `created_at` (TIMESTAMP).

#### 9. `Roadmap.java`
- **Table:** `roadmaps`
- **Purpose:** Represents AI-generated or custom student career roadmaps.
- **Fields:** `roadmap_id` (PK, auto-increment), `user_id` (FK to `users.user_id`), `goal` (VARCHAR(255)), `title` (VARCHAR(200)), `created_at` (TIMESTAMP).

#### 10. `RoadmapNode.java`
- **Table:** `roadmap_nodes`
- **Purpose:** Represents milestones and skills in a student's roadmap.
- **Fields:** `node_id` (PK, auto-increment), `roadmap_id` (FK to `roadmaps.roadmap_id`), `title` (VARCHAR(200)), `description` (TEXT), `semester` (INT), `sequence_order` (INT).

#### 11. `RoadmapEdge.java` & `RoadmapEdgeId.java`
- **Table:** `roadmap_edges`
- **Purpose:** Represents directed prerequisite dependencies connecting roadmap nodes.
- **Fields:** `source_node_id` (PK & FK to `roadmap_nodes.node_id`), `target_node_id` (PK & FK to `roadmap_nodes.node_id`). Uses composite primary key `(source_node_id, target_node_id)`.

---

### Data Access Layer (`repository/`)

Spring Data JPA interfaces that automatically generate SQL queries at runtime without boilerplate JDBC code:
- **`UserRepository.java`**: Queries by email, reg_no, and role.
- **`AlumniProfileRepository.java`**: CRUD operations on `alumni_profiles`.
- **`PlaybookRepository.java`**: Queries by category, status, search keyword, and author.
- **`QuestionRepository.java`**: Queries by category, search keyword, and author.
- **`AnswerRepository.java`**: Queries answers for a question and by author.
- **`RoadmapRepository.java`**: Queries roadmaps by user.
- **`RoadmapNodeRepository.java`**: Queries nodes for a roadmap ordered by sequence order.
- **`RoadmapEdgeRepository.java`**: Queries prerequisite edges for a roadmap using composite keys.

---

### Business Logic Layer (`service/` & `service/impl/`)

- **`AuthService.java` & `AuthServiceImpl.java`**: User registration, login with BCrypt password hashing, JWT token generation, and current profile retrieval (`/me`).
- **`AlumniService.java` & `AlumniServiceImpl.java`**: Manages alumni profiles, job title, and company updates.
- **`PlaybookService.java` & `PlaybookServiceImpl.java`**: Manages guidance playbooks with role authorization (only SENIOR/ALUMNI/ADMIN can publish).
- **`QuestionService.java` & `QuestionServiceImpl.java`**: Manages questions and answers, including alumni verified badge toggling.
- **`RoadmapService.java` & `RoadmapServiceImpl.java`**: Generates AI career roadmaps, builds milestone nodes and prerequisite graph edges, and manages custom graph updates.
- **`AiAssistantService.java` & `AiAssistantServiceImpl.java`**: Career milestone advisory assistant.

---

### REST API Controller Layer (`controller/`)

Exposes HTTP endpoints (`@RestController`), parses `@PathVariable`, `@RequestParam`, `@RequestBody`, and handles HTTP response codes.

#### 21. `PlaybookController.java` (`/api/v1/playbooks`)
- Powers **Screen 1 (Junior Feed)** and **Screen 2 (Senior Guidance Editor)**.

#### 22. `QuestionController.java` (`/api/v1/questions`)
- Powers the inline questions on guides and the **Screen 4 Mentorship Discussion Thread**.

#### 23. `RoadmapController.java` (`/api/v1/roadmap`)
- Powers **Screen 3 (vis-network Dynamic Graph Roadmap)**.

#### 24. `AiAssistantController.java` (`/api/v1/ai-assistant`)
- Powers **Screen 4 (Gemini Path Generator)**.

#### 25. `AuthController.java` (`/api/v1/auth`)
- Powers user login and registration for the Junior/Senior Portal switch.

#### 26. `HealthCheckController.java` (`/api/v1/health`)
- Fast status endpoint to verify if the server is healthy.

---

### Data Transfer Object (DTO) Layer (`dto/`)

Decouples the internal database structure from the public API contracts to ensure security and prevent data leaks.

- **`ApiResponse.java`**: Generic envelope `{ success, message, data, timestamp }` used across all endpoints for uniform responses.
- **`AuthRequest.java`** & **`AuthResponse.java`**: Registration and login credentials, and issued Bearer JWT tokens.
- **`UserProfileDto.java`** & **`AlumniProfileDto.java`**: User account details and linked alumnus professional data.
- **`PlaybookRequest.java`** & **`PlaybookResponse.java`**: Senior and alumni guidance posts with author linkage.
- **`QuestionRequest.java`** & **`QuestionResponse.java`**: Discussion thread question inputs and response lists.
- **`AnswerRequest.java`** & **`AnswerResponse.java`**: Mentorship replies with verified badges.
- **`RoadmapRequest.java`**, **`RoadmapResponse.java`**, **`GenerateRoadmapRequest.java`**: Career roadmap generation and graph data.
- **`RoadmapNodeDto.java`** & **`RoadmapEdgeDto.java`**: Milestone nodes and directed prerequisite edges connecting them.

---

### Exception Handling Layer (`exception/`)

Provides centralized, production-grade error management.

#### 27. `ResourceNotFoundException.java`
- Custom runtime exception thrown whenever an ID (playbook, node, question) is not found in the database.

#### 28. `ErrorResponse.java`
- Structured JSON format for errors: `{ success: false, status: 404, error: "Not Found", message: "...", validationErrors: {...}, timestamp: "..." }`.

#### 29. `GlobalExceptionHandler.java`
- Annotated with `@RestControllerAdvice`.
- Intercepts `ResourceNotFoundException` ➔ returns HTTP `404 Not Found`.
- Intercepts `@Valid` validation failures (`MethodArgumentNotValidException`) ➔ returns HTTP `400 Bad Request` with an exact map of which field failed and why.
- Intercepts general exceptions ➔ returns HTTP `500 Internal Server Error`.

---

### Configuration & Resource Files (`src/main/resources/`)

- **`application.properties`**: Main configuration. Defaults to `spring.profiles.active=h2` so anyone can run the app with one click without needing MySQL running.
- **`application-h2.properties`**: In-memory database settings for fast local testing and includes the H2 Web Console at `/h2-console`.
- **`application-mysql.properties`**: Full MySQL production settings for when connecting to a local MySQL server on port 3306.
- **`schema-mysql.sql`**: Complete standalone SQL DDL script for creating the tables in MySQL Workbench.

---

## 3. Complete REST API Reference

### Screen 1: Junior Feed & Insights

#### 1.1 Get All Playbooks (Junior Feed)
- **Method:** `GET`
- **URL:** `/api/v1/playbooks`
- **Query Parameters:**
  - `category` *(optional)*: `Academics`, `Internships`, `Higher Studies`, or `ALL`
  - `search` *(optional)*: Keyword to search in title or content
- **What it does:** Fetches all published senior playbooks sorted by newest first, along with author CGPA, placement badge, and question count.
- **Sample Request:**
  ```http
  GET /api/v1/playbooks?category=Academics HTTP/1.1
  Host: localhost:8080
  ```
- **Sample Response (200 OK):**
  ```json
  {
    "success": true,
    "message": "Junior Feed playbooks retrieved successfully",
    "data": [
      {
        "id": 1,
        "title": "Semester 1 & 2 Blueprint",
        "category": "Academics",
        "authorName": "3rd Year CSE (CGPA: 9.1)",
        "authorCgpa": 9.1,
        "placementCompany": null,
        "markdownContent": "Don't ignore Engg Math: impacts Sem 3 algorithms...",
        "questionCount": 1,
        "createdAt": "2026-10-17 18:29:56"
      }
    ],
    "timestamp": "2026-10-17 18:30:23"
  }
  ```

#### 1.2 Get Playbook Details By ID
- **Method:** `GET`
- **URL:** `/api/v1/playbooks/{id}`
- **What it does:** Retrieves a single full guide by its database ID. Returns 404 if not found.

---

### Screen 2: Senior Guidance Editor

#### 2.1 Publish Senior Guide (Syncs to Junior Feed)
- **Method:** `POST`
- **URL:** `/api/v1/playbooks/create` *(also supports `/api/v1/playbooks`)*
- **Headers:** `Content-Type: application/json`
- **What it does:** Validates the senior's inputs and publishes a new guide to the database, making it immediately visible on the Junior Feed.
- **Request Body:**
  ```json
  {
    "title": "How I Cleared Sem 3 DSA While Maintaining 8.8 CGPA",
    "category": "Academics",
    "authorName": "Final Year CSE (Placed at Zoho)",
    "authorCgpa": 8.8,
    "placementCompany": "Placed at Zoho",
    "markdownContent": "Detail step-by-step milestones, interview tips, pits to avoid:\n1. Dedicated 1.5 hours daily to LeetCode.\n2. Visualized trees and recursion using pen & paper."
  }
  ```
- **Sample Response (201 Created):**
  ```json
  {
    "success": true,
    "message": "Guide published successfully and synced to Junior Feed",
    "data": {
      "id": 5,
      "title": "How I Cleared Sem 3 DSA While Maintaining 8.8 CGPA",
      "category": "Academics",
      "authorName": "Final Year CSE (Placed at Zoho)",
      "authorCgpa": 8.8,
      "placementCompany": "Placed at Zoho",
      "markdownContent": "Detail step-by-step milestones...",
      "questionCount": 0,
      "createdAt": "2026-10-17 18:34:32"
    }
  }
  ```

#### 2.2 Update an Existing Guide
- **Method:** `PUT`
- **URL:** `/api/v1/playbooks/{id}`
- **What it does:** Updates the title, markdown content, or category of a published guide.

#### 2.3 Delete a Guide
- **Method:** `DELETE`
- **URL:** `/api/v1/playbooks/{id}`
- **What it does:** Deletes the guide and unlinks any attached questions.

---

### Screen 3: Dynamic Graph Roadmap (vis-network)

#### 3.1 Get Interactive Graph Dataset
- **Method:** `GET`
- **URL:** `/api/v1/roadmap`
- **What it does:** Returns all roadmap nodes and edges formatted specifically for direct feeding into `vis-network.js`: `new vis.Network(container, data, options)`.
- **Sample Response (200 OK):**
  ```json
  {
    "success": true,
    "message": "Dynamic vis-network roadmap dataset loaded",
    "data": {
      "nodes": [
        {
          "id": "node-1",
          "label": "1. C & Pointers",
          "level": 1,
          "group": "Fundamentals",
          "color": "#3b82f6",
          "milestoneText": "Milestone: Strong Syntax & Pointers",
          "prerequisites": "None - Start from Sem 1",
          "isCompleted": true,
          "title": "1. C & Pointers\nPrerequisites: None - Start from Sem 1\nMilestone: Strong Syntax & Pointers"
        },
        {
          "id": "node-2",
          "label": "2. Data Structures",
          "level": 2,
          "group": "Core CS",
          "color": "#2563eb",
          "milestoneText": "Milestone: 8.5+ Sem GPA",
          "prerequisites": "1. C & Pointers, Basic Recursion",
          "isCompleted": true,
          "title": "2. Data Structures\nPrerequisites: 1. C & Pointers, Basic Recursion\nMilestone: 8.5+ Sem GPA"
        },
        {
          "id": "node-3",
          "label": "LeetCode Top 150",
          "level": 3,
          "group": "Problem Solving",
          "color": "#10b981",
          "milestoneText": "Milestone: 150 Problems Solved",
          "prerequisites": "Arrays, Trees, Graphs, Hash Maps",
          "isCompleted": false,
          "title": "LeetCode Top 150\nPrerequisites: Arrays, Trees, Graphs, Hash Maps"
        },
        {
          "id": "node-4",
          "label": "Spring Boot Micro",
          "level": 3,
          "group": "Backend Architecture",
          "color": "#f59e0b",
          "milestoneText": "Milestone: Production Ready REST API",
          "prerequisites": "Java OOP, MySQL Basic Queries, REST conventions",
          "isCompleted": false,
          "title": "Spring Boot Micro\nPrerequisites: Java OOP, MySQL Basic Queries"
        }
      ],
      "edges": [
        { "id": 1, "from": "node-1", "to": "node-2", "arrows": "to", "label": "Prerequisite" },
        { "id": 2, "from": "node-2", "to": "node-3", "arrows": "to", "label": "Coding Practice" },
        { "id": 3, "from": "node-2", "to": "node-4", "arrows": "to", "label": "System Dev" }
      ]
    }
  }
  ```

#### 3.2 Get Selected Node Detail (Node Click)
- **Method:** `GET`
- **URL:** `/api/v1/roadmap/nodes/{id}`
- **What it does:** Returns the prerequisite and reading item details when a student clicks a node in the graph.

#### 3.3 Toggle Node Progress (Completed Semesters / Milestone)
- **Method:** `PATCH`
- **URL:** `/api/v1/roadmap/nodes/{id}/progress`
- **What it does:** Toggles `isCompleted` between `true` and `false` to track student semester learning progress.

#### 3.4 Add New Custom Roadmap Node
- **Method:** `POST`
- **URL:** `/api/v1/roadmap/nodes`
- **Request Body:** `{ "id": "node-5", "label": "AWS & Cloud", "category": "DevOps", "level": 4, "color": "#8b5cf6" }`

---

### Screen 4: AI Career Path Generator (Gemini)

#### 4.1 Generate Adaptive Milestones
- **Method:** `POST`
- **URL:** `/api/v1/ai-assistant/generate-path`
- **Headers:** `Content-Type: application/json`
- **What it does:** Implements the Gemini / Spring AI generation service. Parses student career goal and current semester to calculate adaptive milestones and skill suggestions.
- **Request Body:**
  ```json
  {
    "goal": "Targeting Off-Campus Cloud SDE by Sem 7",
    "currentSemester": 3
  }
  ```
- **Sample Response (200 OK):**
  ```json
  {
    "success": true,
    "message": "Gemini generated adaptive milestones successfully",
    "data": {
      "goal": "Targeting Off-Campus Cloud SDE by Sem 7",
      "currentSemester": 3,
      "adaptiveMilestones": [
        "1. Learn AWS ECS & Docker containerization (Sem 5)",
        "2. Contribute to Open Source Spring repositories (Sem 6)",
        "3. Complete 5 mock interviews on EduPath Q&A Forum",
        "4. Build and deploy a multi-container microservice with CI/CD pipeline"
      ],
      "recommendedRoadmapSkills": [
        "Docker & Kubernetes",
        "AWS / GCP Core Services",
        "Spring Boot Microservices",
        "PostgreSQL / MySQL Indexing",
        "Kafka / RabbitMQ"
      ],
      "seniorMentorAdvice": "Focus heavily on system design basics and solve at least 150 LeetCode Mediums before starting campus recruitment drives."
    }
  }
  ```

---

### Mentorship Q&A & Discussion Forum

#### 5.1 Get Discussion Threads
- **Method:** `GET`
- **URL:** `/api/v1/questions`
- **Optional Query Param:** `playbookId={id}` (to get questions belonging to a specific guide)
- **What it does:** Returns all questions ordered by upvote count along with their answers.

#### 5.2 Submit Inline Question
- **Method:** `POST`
- **URL:** `/api/v1/questions`
- **Request Body:**
  ```json
  {
    "playbookId": 1,
    "questionText": "Learn C++ directly or finish C first?",
    "authorName": "Junior CSE (Sem 2)",
    "authorRole": "ROLE_JUNIOR"
  }
  ```

#### 5.3 Upvote a Question (Mentorship Forum Upvote Counter)
- **Method:** `POST`
- **URL:** `/api/v1/questions/{id}/upvote`
- **What it does:** Increments the question's `upvoteCount` by 1.

#### 5.4 Submit an Answer to a Question
- **Method:** `POST`
- **URL:** `/api/v1/questions/{id}/answers`
- **Request Body:**
  ```json
  {
    "authorName": "Senior Alumni (Amazon)",
    "authorRole": "ROLE_ALUMNI",
    "commentBody": "> 7.5 CGPA avoids filter at most top product companies.",
    "isVerifiedAnswer": true
  }
  ```

#### 5.5 Verify an Answer (Alumni Badge)
- **Method:** `POST`
- **URL:** `/api/v1/questions/answers/{answerId}/verify`
- **What it does:** Sets `is_verified_answer = true` to display the verified alumni tick mark.

---

### Authentication & Role Management

#### 6.1 Register User
- **Method:** `POST`
- **URL:** `/api/v1/auth/register`
- **Request Body:**
  ```json
  {
    "regNo": "23CS105",
    "name": "Pooja M",
    "email": "pooja@edupath.edu",
    "password": "password123",
    "role": "ROLE_JUNIOR",
    "currentCgpa": 8.5,
    "yearOfStudy": "2nd Year CSE"
  }
  ```

#### 6.2 User Login (Header Switch)
- **Method:** `POST`
- **URL:** `/api/v1/auth/login`
- **Request Body:**
  ```json
  {
    "email": "aravind@edupath.edu",
    "password": "password123"
  }
  ```
- **Sample Response (200 OK):**
  ```json
  {
    "success": true,
    "message": "Logged in successfully",
    "data": {
      "token": "JWT-ROLE_SENIOR-88698044",
      "userId": 1,
      "regNo": "21CS010",
      "name": "Aravind R",
      "email": "aravind@edupath.edu",
      "role": "ROLE_SENIOR",
      "currentCgpa": 8.8,
      "yearOfStudy": "Final Year CSE",
      "placementCompany": "Placed at Zoho"
    }
  }
  ```

---

### System Health Check

#### 7.1 Health Check
- **Method:** `GET`
- **URL:** `/api/v1/health`
- **What it does:** Returns `200 OK` with service uptime, server time, and status `"UP"`.

---

## 4. College Viva & Presentation Q&A

**Q1: Why did you use DTOs instead of returning JPA entities directly?**  
*Answer:* Returning entities directly creates circular dependency issues with JSON serialization (e.g. bidirectional relationships) and exposes internal database column names or passwords. DTOs give full control over what fields are sent to the client.

**Q2: How does the dynamic graph work with `vis-network.js`?**  
*Answer:* `RoadmapController` serializes database entities `RoadmapNode` and `RoadmapEdge` into the standard `{ nodes: [...], edges: [...] }` JSON structure that `vis-network.js` natively consumes.

**Q3: How does the application handle exceptions?**  
*Answer:* Using a `@RestControllerAdvice` class (`GlobalExceptionHandler`). Any unhandled error or validation failure is intercepted and formatted into a uniform JSON structure with proper HTTP status codes (`400`, `404`, `500`).

**Q4: How do you switch between H2 and MySQL?**  
*Answer:* Through Spring Boot profiles. Changing `spring.profiles.active` in `application.properties` from `h2` to `mysql` switches the data source from the embedded in-memory database to the live MySQL server without rewriting any Java code.
