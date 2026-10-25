# EduPath Q&A Forum

The Q&A forum module only. It provides Junior question posting, an open-question queue visible to all Seniors, and private delivery of the first Senior answer to the Junior who asked. It does not implement portal login, user registration, dashboards, or navigation.

## Project layout

This is one Spring Boot application, with backend and frontend code grouped by responsibility within the standard Maven layout:

```text
src/
  main/
    java/com/edupath/qa/       Backend Java application
    resources/
      static/                  Frontend HTML, CSS, and JavaScript
      db/migration/            Database schema migrations
      application*.properties  Application configuration
  test/java/                   Backend tests
data/                          Local development database (created at runtime)
pom.xml                        Maven build and dependencies
```

The frontend is packaged and served by the backend, so keep its files in `src/main/resources/static` rather than moving them to a separate top-level app. Java code belongs under `src/main/java`.

## Open the local preview

Requirements: Java 21 or later. Maven Wrapper files are included.

In PowerShell, from the `Edupath` folder:

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
.\mvnw.cmd spring-boot:run
```

Set `JAVA_HOME` to the folder where your Java JDK is actually installed. Leave this terminal running and open **http://localhost:8080** in your browser.

The local-only preview asks whether to preview the Junior or Senior forum view. This selector is not a login or account-creation flow; it exists only so teammates can inspect and test the Q&A module before their portal login is integrated. You enter the actual question and answer yourself. The local preview starts without sample questions or answers. Questions and answers are saved in `data/edupath-forum.mv.db` and are shared across the Junior and Senior preview views.

Stop the server by pressing `Ctrl+C` in the terminal running Spring Boot.

## Q&A workflow

- An authenticated Junior posts a title, topic, and question.
- The question appears in the open-question queue available to all Seniors.
- A Senior posts an answer. A database unique constraint on `answers.question_id` ensures exactly one accepted answer, even if Seniors answer concurrently.
- An answered question leaves the Senior open queue. The accepted answer appears only with that Junior's own question.
- Junior and Senior screens poll every five seconds, so new questions and completed answers appear without a manual refresh.
- API authorization checks roles server-side. Question ownership is enforced against the signed-in user's ID, not a browser-supplied identity.

## Existing portal integration

The forum expects the existing EduPath portal's Spring Security session. The signed-in `Authentication.getName()` must match the account email in `users.email`, and Senior/Junior roles must grant `ROLE_SENIOR` / `ROLE_JUNIOR`. Keep the existing portal's login and registration; do not use the local role preview as production authentication.

Use `forum.html` as the forum entry point after the existing Q&A navigation item is clicked. The module provides:

| Method | Endpoint | Access |
|---|---|---|
| `GET` | `/api/me` | Authenticated Junior or Senior |
| `GET` | `/api/csrf` | CSRF token for the current session |
| `GET` | `/api/junior/questions` | Signed-in Junior's questions and accepted answers |
| `POST` | `/api/junior/questions` | Signed-in Junior; creates a question |
| `GET` | `/api/junior/questions/{questionId}` | Owning Junior only |
| `GET` | `/api/senior/questions` | Signed-in Senior; all open Junior questions |
| `POST` | `/api/senior/questions/{questionId}/answers` | Signed-in Senior; first answer wins |

The local-preview role switch exists under the `dev` profile only. It is not available with the MySQL profile.

## Database and Aiven

The supplied database design identifies MySQL and these forum tables:

- `users`: portal identity and `JUNIOR` / `SENIOR` role
- `questions`: `question_id`, `author_id`, `title`, `question_body`, `category`, `created_at`
- `answers`: `answer_id`, `question_id`, `author_id`, `answer_body`, `is_verified_answer`, `created_at`

Flyway creates these tables when absent. A separate migration creates a unique index on `answers.question_id`; inspect existing data for duplicate answers before applying it to a shared Aiven database. Migrations never delete existing forum records.

Use Aiven's MySQL JDBC details and keep credentials in environment variables:

```powershell
$env:SPRING_PROFILES_ACTIVE = "mysql"
$env:DB_URL = "jdbc:mysql://YOUR-AIVEN-HOST:PORT/YOUR_DATABASE?sslMode=REQUIRED"
$env:DB_USERNAME = "YOUR_AIVEN_USERNAME"
$env:DB_PASSWORD = "YOUR_AIVEN_PASSWORD"
.\mvnw.cmd spring-boot:run
```

The MySQL profile requires all three `DB_*` variables; it does not fall back to a local database. Do not start it against the shared Aiven instance until the team has verified the existing schema and reviewed the Flyway migrations, especially the unique-answer migration. This setup change does not connect to or modify Aiven.

The production portal must provide the authenticated session; the local preview identity feature is disabled in this profile.

## Validate

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
.\mvnw.cmd test
```

Tests cover question ownership, private answer visibility, role-based API access, validation, and concurrent first-answer-wins behavior.
