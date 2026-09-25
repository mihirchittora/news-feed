# News Platform — Milestone 1

Milestone 1 is a modular-monolith foundation for a digital news platform. It includes account registration, login, stateless JWT authentication, role authorization, the current-user API, an initial admin bootstrap, and the first admin dashboard shell.

Future product modules such as stories, feed items, media processing, comments, likes, newspaper editions, advertisements, categories, tags, and breaking news are intentionally not implemented yet.

## Architecture

```text
Next.js App Router + React + TypeScript + Tailwind
                         │
                         ▼
              Spring Boot REST API + JWT
                         │
                         ▼
                  PostgreSQL + Flyway
```

The backend is a modular monolith. Domain code is separated into `auth`, `user`, and `admin` modules, with shared security, configuration, and error handling under `common`.

## Prerequisites

- Java 21
- Maven 3.9+
- Node.js 20+
- npm
- Docker and Docker Compose

## Environment setup

From the repository root:

```bash
cp .env.example .env
```

`.env` is ignored by git. Replace `JWT_SECRET` with a random value of at least 32 bytes for any shared environment. The included values are development placeholders only.

The backend imports the root `.env` when run from `apps/backend`. The frontend defaults to `http://localhost:8080`; set `NEXT_PUBLIC_API_URL` in `apps/web/.env.local` if the API runs elsewhere.

## Run the full stack with Docker Compose

The macOS-friendly standalone Compose command builds and starts PostgreSQL, the Spring Boot API, and the Next.js app together:

```bash
docker-compose up --build -d
docker-compose ps
```

Open `http://localhost:3000`. The API is available at `http://localhost:8080`, and Swagger is available at `http://localhost:8080/swagger-ui/index.html`.

To follow logs or stop the stack:

```bash
docker-compose logs -f backend
docker-compose down
```

The default host ports are configurable through `.env`: `POSTGRES_PORT`, `BACKEND_PORT`, and `WEB_PORT`. This project defaults PostgreSQL to host port 5440 because 5432 is commonly occupied by an existing local PostgreSQL or Docker service.

```bash
POSTGRES_PORT=5432 docker-compose up --build -d
```

The backend connects to the Compose service name `postgres` inside the Docker network, while the browser uses `NEXT_PUBLIC_API_URL=http://localhost:8080` baked into the web image at build time.

## Run PostgreSQL for local app development

```bash
docker-compose up -d postgres
```

If your Docker installation uses the newer integrated command, `docker compose` is equivalent.

PostgreSQL is exposed at `localhost:5440` by default with the development database `news_platform`. Set `POSTGRES_PORT=5432` if that port is free on your machine. Flyway runs `V1__create_users.sql` automatically when the backend starts.

## Run the backend

```bash
cd apps/backend
mvn spring-boot:run
```

The API runs at `http://localhost:8080`.

## Run the frontend

In a second terminal:

```bash
cd apps/web
npm install
npm run dev
```

The web app runs at `http://localhost:3000`.

## Initial admin bootstrap

If all three values are configured, the backend creates the admin once on startup:

```text
INITIAL_ADMIN_EMAIL
INITIAL_ADMIN_PASSWORD
INITIAL_ADMIN_NAME
```

If an account already exists for that email, startup does nothing. It never overwrites an existing password. If any value is blank, bootstrap is skipped. Do not commit real credentials.

## Authentication behavior

- Registration always creates an `ACTIVE` `USER`; clients cannot choose `role` or `status`.
- Emails are trimmed and normalized to lowercase before persistence.
- Passwords are hashed with BCrypt and are never returned or logged.
- Login returns a short-lived JWT containing only `sub`, `role`, `iat`, and `exp`.
- The browser keeps the token in `sessionStorage` and sends it as an `Authorization: Bearer` header. This keeps the token out of persistent local storage, but it is still readable by JavaScript; an HttpOnly cookie/BFF can replace this in a later hardening milestone.
- The API re-checks that the user exists and is `ACTIVE` for each authenticated request. Backend authorization is authoritative.

## API

| Method | Path | Access | Purpose |
| --- | --- | --- | --- |
| `POST` | `/api/v1/auth/register` | Public | Create a user account |
| `POST` | `/api/v1/auth/login` | Public | Return a Bearer JWT and user summary |
| `GET` | `/` or `/api/v1/health` | Public | Return API service health |
| `GET` | `/api/v1/users/me` | Authenticated | Return the current user |
| `GET` | `/api/v1/admin/me` | `ADMIN` only | Verify admin authorization |

Validation and security errors use one response shape:

```json
{
  "timestamp": "2026-09-24T12:00:00Z",
  "status": 400,
  "code": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "errors": { "email": "Invalid email address" },
  "path": "/api/v1/auth/register"
}
```

Swagger UI is available at `http://localhost:8080/swagger-ui/index.html` when the backend is running. The OpenAPI document is at `http://localhost:8080/v3/api-docs`.

## Frontend routes

- `/` — public platform landing page
- `/login` — shared user/admin login
- `/register` — account registration; redirects to login after success
- `/profile` — authenticated account view
- `/admin/dashboard` — admin-only dashboard shell
- `/403` — signed-in users without admin access (`/forbidden` remains a compatibility alias)

The non-public pages use client-side guards for responsive navigation while every API permission is enforced by Spring Security on the server.

## Database schema

Flyway creates `users` with:

- UUID primary key
- name, normalized unique email, BCrypt password hash
- `USER`/`ADMIN` role constraint
- `ACTIVE`/`DISABLED` status constraint
- UTC `created_at` and `updated_at` timestamps
- email lookup index and database constraints

## Verification commands

Backend:

```bash
cd apps/backend
mvn test
mvn package
```

Frontend:

```bash
cd apps/web
npm run lint
npm test
npm run build
```

The frontend build script uses `next build --webpack`; this is more portable in restricted environments where Turbopack worker processes cannot bind locally.

## Manual smoke test

1. Start PostgreSQL, backend, and frontend.
2. Register a user at `/register`.
3. Log in and confirm redirect to `/`.
4. Open `/profile`, confirm account details, then log out.
5. Configure `INITIAL_ADMIN_*`, restart the backend, and log in as the admin.
6. Confirm redirect to `/admin/dashboard` and the admin verification call.
7. Log in as a normal user and request `/api/v1/admin/me`; it should return `403 FORBIDDEN`.
