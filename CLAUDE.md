# CLAUDE.md - Development Guide

## Build and Run Instructions

### Database (PostgreSQL via Docker)
- **Start Postgres:** `docker compose up -d postgres` (uses `docker-compose.yml` at repo root; reads `DB_NAME`/`DB_USERNAME`/`DB_PASSWORD` from `.env`, copy `.env.example` if you don't have one).
- **Stop:** `docker compose down` (add `-v` to also wipe the data volume).
- Only needed for the `prod` Spring profile — the default `dev` profile uses an in-memory H2 database and needs no Docker setup.

### Backend (Spring Boot)
- **Requirements:** Java 17+, Maven 3.8+
- **Configuration:** Update database credentials in `backend/src/main/resources/application.yml`. Local dev uses an in-memory H2 database with zero setup; production uses PostgreSQL via env vars (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`).
- **Secrets:** `JWT_SECRET` and `N8N_WEBHOOK_URL` are read from environment variables. Never hardcode them or commit them.
- **Build application:** `mvn clean package -f backend/pom.xml`
- **Run application:** `mvn spring-boot:run -f backend/pom.xml` (defaults to the `dev` profile, which seeds one user per role — see `docs/specs/auth_backend.md` for credentials)
- **Run JUnit 5 tests:** `mvn test -f backend/pom.xml`

### Frontend (Next.js)
- **Requirements:** Node.js 18+
- **Install dependencies:** `npm install --prefix frontend`
- **Run development server:** `npm run dev --prefix frontend`
- **Build production build:** `npm run build --prefix frontend`
- **Run Playwright E2E tests:** `npx playwright test --project=chromium` (inside frontend directory)

---

## Architecture Constraints & Code Style Guidelines

### Spring Boot Code Style
- **Layer Separation:** Adhere strictly to: `Controller -> Service -> Repository` architecture.
- **DTOs:** Always use DTO (Data Transfer Objects) for requests and responses. Do not expose Entity models directly in the Controllers.
- **Security:** Use Spring Security with JWT tokens. Ensure method security checks are set up (`@PreAuthorize`) based on roles: `REQUESTOR`, `APPROVER`, `ASSIGNEE`, `ASSIGNER`, `ADMIN`.
- **Lombok:** Use `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`, and `@Builder`.

### Next.js Code Style
- **Directory Structure:** Use App Router (`app/` directory). Keep components clean and functional.
- **Styling:** Use Tailwind CSS utility classes exclusively.
- **TypeScript:** Strict type checks. Define structural interfaces for `Ticket`, `User`, and `AuditLog`.
- **State Management:** Use Context API for authentication persistence.
- **UI Direction:** The dashboard/ticket-detail experience follows the "Timeline-first" wireframe direction (see `reference/`) — status is shown as a vertical progress rail, the audit trail is the primary content of the ticket detail view, and the dashboard leads with a chronological activity feed plus KPI chips.

### Error Handling Guidelines
- **Backend:** Global exception handler (`@ControllerAdvice`) returning standardized error payload: `{ "timestamp", "status", "error", "path" }`.
- **Frontend:** Implement error boundaries and clean toast UI notices using Tailwind alerts for failed API connections.

### Git Workflow
- Commit each feature/phase separately with a descriptive message. Do not bundle unrelated changes into one commit.
