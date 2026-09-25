# News Platform — Milestone 8

Milestones 1–3 provide the modular-monolith foundation for a digital news platform: authentication, configurable RBAC, editorial publishing, media, chronological feeds, likes, comments, replies, and moderation.

Milestone 4 adds a simple editorial Breaking News flag to published stories. Milestone 5 adds a two-level category hierarchy and a protected Daily Newspaper archive. Milestone 6 adds operational advertisements. Milestone 7 adds a real-data, permission-filtered admin dashboard and reporting view. Milestone 8 hardens the launch path with append-only integrity constraints, upload/content validation, request correlation, abuse throttling, security headers, Actuator probes/metrics, production configuration, Docker health ordering, sitemap/robots, and operational documentation. Newspaper metadata and covers are public; PDF documents require an authenticated account and a published edition. Push notifications, recommendations, WebSockets, subscriptions, payments, public analytics, user tracking, and ad analytics remain out of scope.

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

- Docker Desktop with the Compose plugin on Windows or macOS, or Docker Engine with `docker compose` on Linux.
- PowerShell on Windows only when using the optional `.ps1` helper scripts.

The supported application workflow does not require host-installed Java, Maven, Node.js, npm, or PostgreSQL. Those tools are present only inside the build/runtime containers. Optional local test commands are listed separately below.

If `docker compose` reports `unknown command: compose`, the Docker CLI is missing its Compose v2 plugin. Install or enable the Compose plugin in Docker Desktop/Docker Engine. On older installations that provide the standalone compatible command, `docker-compose` can be used as a temporary local fallback; Docker Desktop and clean-checkout verification should use `docker compose`.

## Environment setup

From the repository root, no environment file is required for the development defaults:

```bash
docker compose up --build -d
```

To customize the defaults, copy `.env.example` to `.env` using the file manager, or use `cp .env.example .env` in a POSIX shell or `Copy-Item .env.example .env` in PowerShell. `.env` is ignored by git. Replace `JWT_SECRET` with a random value of at least 32 bytes for any shared environment. The included values are development placeholders only.

`APP_ENVIRONMENT=production` activates fail-fast checks for non-development JWT/database settings, explicit HTTPS CORS origins, and HSTS. Production configuration must supply secrets through the environment or secret manager; do not bake them into images.

The frontend defaults to `http://localhost:8080`; Docker uses the persistent `news_media_data` named volume mounted at `/data/media`. No host path is mounted into the application containers.

## Run the full stack with Docker Compose

The platform-independent Compose command builds and starts PostgreSQL, the Spring Boot API, and the Next.js app together:

```text
docker compose up --build -d
docker compose ps
```

Open `http://localhost:3000`. The API is available at `http://localhost:8080`, and Swagger is available at `http://localhost:8080/swagger-ui/index.html`.

To follow logs or stop the stack:

```text
docker compose logs -f backend
docker compose down
```

The backend exposes public health checks at `/actuator/health`, `/actuator/health/liveness`, and `/actuator/health/readiness`; only health, liveness/readiness, and metrics are exposed under Actuator. Use the `X-Request-Id` response header to correlate API errors with structured logs.

Uploaded Docker development media survives `docker compose down` and normal restarts. `docker compose down -v` intentionally clears the Compose-managed database and media named volumes together.

The default host ports are configurable through `.env`: `POSTGRES_PORT`, `BACKEND_PORT`, and `WEB_PORT`. This project defaults PostgreSQL to host port 5440 because 5432 is commonly occupied by an existing local PostgreSQL or Docker service. Edit `.env` instead of using shell-specific inline environment syntax.

```text
POSTGRES_PORT=5432
```

The backend connects to the Compose service name `postgres` inside the Docker network, while the browser uses `NEXT_PUBLIC_API_URL=http://localhost:8080` baked into the web image at build time. `BREAKING_NEWS_DEFAULT_DURATION_MINUTES` controls the default expiry when an editor does not choose one.

## Run PostgreSQL for local app development

```text
docker compose up -d postgres
```

PostgreSQL is exposed at `localhost:5440` by default with the development database `news_platform`. Flyway runs the migrations automatically when the backend starts.

## Optional local-only development commands

The following commands are not required for Docker Compose and require local Java/Maven or Node/npm installations. Use the containers above for the supported cross-platform path.

### Run the backend locally

```bash
cd apps/backend
mvn spring-boot:run
```

The API runs at `http://localhost:8080`.

## Admin dashboard reporting

`/admin/dashboard` loads its operational data from `GET /api/v1/admin/dashboard?period=TODAY`, with `LAST_7_DAYS` and `LAST_30_DAYS` also supported. Periods use calendar-day boundaries in the server-side `APP_TIMEZONE` (default `Asia/Kolkata`), while persisted timestamps remain UTC. The API returns permission-filtered sections rather than requiring one staff member to hold every module permission. See [`docs/dashboard-metrics.md`](docs/dashboard-metrics.md) for metric definitions, timezone semantics, and RBAC behavior.

### Run the frontend locally

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
| `GET` | `/api/v1/categories` | Public | List active categories |
| `GET` | `/api/v1/categories/{slug}` | Public | Read one active category and its children |
| `GET` | `/api/v1/feed` | Public | Chronological published feed; supports `category`, `tag`, `limit`, `cursor` |
| `GET` | `/api/v1/feed/{slug}` | Public | Published story detail |
| `GET` | `/api/v1/breaking-news` | Public | Active Breaking News, newest first |
| `GET/POST/PUT/DELETE` | `/api/v1/admin/categories` | `CATEGORY_MANAGE` | Manage categories |
| `GET/POST/PUT/DELETE` | `/api/v1/admin/tags` | `TAG_MANAGE` | Manage normalized reusable tags |
| `GET/POST/PUT/DELETE` | `/api/v1/admin/stories` | Story permissions | Manage story drafts and relations |
| `POST` | `/api/v1/admin/stories/{id}/publish` | `STORY_PUBLISH` | Publish a complete story |
| `POST` | `/api/v1/admin/stories/{id}/unpublish` | `STORY_PUBLISH` | Remove a story from public views |
| `GET` | `/api/v1/admin/breaking-news` | `BREAKING_NEWS_MANAGE` | List active or expired breaking stories |
| `POST` | `/api/v1/admin/stories/{id}/breaking` | `BREAKING_NEWS_MANAGE` | Enable Breaking News or update expiry |
| `DELETE` | `/api/v1/admin/stories/{id}/breaking` | `BREAKING_NEWS_MANAGE` | Remove Breaking News state |
| `POST` | `/api/v1/admin/media` | `STORY_CREATE` or `STORY_EDIT` | Store an image/video through the media abstraction |
| `GET` | `/api/v1/newspapers` | Public | List published newspaper metadata and covers |
| `GET` | `/api/v1/newspapers/{id}` | Public | Read published newspaper metadata |
| `GET` | `/api/v1/newspapers/{id}/document` | Authenticated | Stream a published newspaper PDF |
| `GET/POST/PUT/DELETE` | `/api/v1/admin/newspapers` | Newspaper permissions | Manage editions and metadata |
| `POST` | `/api/v1/admin/newspapers/{id}/document` | `NEWSPAPER_UPLOAD` or `NEWSPAPER_EDIT` | Upload or replace a PDF |
| `POST` | `/api/v1/admin/newspapers/{id}/cover` | `NEWSPAPER_UPLOAD` or `NEWSPAPER_EDIT` | Upload or replace a cover |
| `POST` | `/api/v1/admin/newspapers/{id}/publish` | `NEWSPAPER_PUBLISH` | Publish an edition |
| `POST` | `/api/v1/admin/newspapers/{id}/unpublish` | `NEWSPAPER_PUBLISH` | Unpublish an edition |
| `GET` | `/api/v1/admin/dashboard?period=TODAY` | Administrative staff permissions | Read the permission-filtered operational dashboard |

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

- `/` — public Breaking News area and chronological home feed
- `/category/[slug]` — category feed
- `/tag/[slug]` — tag results feed
- `/story/[slug]` — responsive story detail with media
- `/login` — shared user/admin login
- `/register` — account registration; redirects to login after success
- `/profile` — authenticated account view
- `/admin/dashboard` — admin-only dashboard shell
- `/admin/stories`, `/admin/stories/new`, `/admin/stories/[id]` — story list, editor, preview, media, publish/unpublish, delete
- `/admin/breaking-news` — active and recently expired Breaking News management
- `/admin/categories` — two-level category tree management and ordering
- `/admin/newspapers`, `/admin/newspapers/new`, `/admin/newspapers/[id]` — newspaper upload, edit, publish, and archive management
- `/newspaper` — public newspaper archive and metadata
- `/newspaper/[id]` — authenticated PDF reader with an anonymous sign-in gate
- `/admin/tags` — tag management and search
- `/403` — signed-in users without admin access (`/forbidden` remains a compatibility alias)

The non-public pages use client-side guards for responsive navigation while every API permission is enforced by Spring Security on the server.

## Operations and verification

- [`docs/authorization-matrix.md`](docs/authorization-matrix.md) — backend permission matrix and RBAC safety rules
- [`docs/architecture.md`](docs/architecture.md) — actual modular-monolith runtime boundaries
- [`docs/backup-and-recovery.md`](docs/backup-and-recovery.md) — database/media backup and recovery procedures
- [`docs/production-readiness.md`](docs/production-readiness.md) — release checklist and current blockers
- `scripts/check-data-consistency.sh` / `scripts/check-data-consistency.ps1` — report-only integrity and Docker-volume diagnostic
- `scripts/seed-performance.sh` / `scripts/seed-performance.ps1` — development-only deterministic representative dataset; it refuses production
- `scripts/smoke-critical-api.sh` / `scripts/smoke-critical-api.ps1` — health/auth/me/RBAC/error API smoke test against a running backend

The Unix helpers prefer `docker compose` and fall back to the standalone `docker-compose` command when the v2 plugin is unavailable; they do not require host PostgreSQL. The PowerShell helpers provide the equivalent Windows path. Seed data is never loaded automatically by the application or Compose.

## Editorial schema

Flyway creates `categories`, `tags`, `stories`, `story_tags`, and `story_media` in addition to the Milestone 1 tables. Migration V7 adds `stories.is_breaking`, `breaking_started_at`, and `breaking_until`, plus a partial index for active published breaking queries. Stories use `DRAFT`, `PUBLISHED`, and `UNPUBLISHED` states; public queries only select `PUBLISHED` stories ordered by `published_at DESC, id DESC`, while Breaking News uses the active timestamp window and `breaking_started_at DESC`. Story bodies are stored as sanitized HTML with paragraph, heading, emphasis, links, and list elements allowed. Media binaries never enter PostgreSQL: the `MediaStorageService` interface currently uses a local file adapter, with a Docker named volume for development.

Flyway V8 adds `categories.parent_id`, a self foreign key with `RESTRICT` deletion, sibling-name uniqueness, and hierarchy indexes. The application only permits top-level parents and rejects third-level categories; story assignment rejects non-leaf categories when children exist. Public category filters include a selected parent and its active children, while direct story URLs remain available when a category is later deactivated.

Flyway V9 creates `newspaper_editions` with `DRAFT`, `PUBLISHED`, and `UNPUBLISHED` states, a database uniqueness rule on `(edition_date, lower(edition))`, and indexes for archive queries. PDFs and optional covers are stored under `newspapers/YYYY/MM/DD/` through the local storage adapter; PostgreSQL stores only keys and metadata. The document endpoint streams the file only after JWT authentication and publication checks, so no permanent public PDF URL is exposed.

Flyway creates `users` with:

- UUID primary key
- name, normalized unique email, BCrypt password hash
- `USER`/`ADMIN` role constraint
- `ACTIVE`/`DISABLED` status constraint
- UTC `created_at` and `updated_at` timestamps
- email lookup index and database constraints

## Verification commands

The cross-platform verification path is:

```text
docker compose config
docker compose build
docker compose up --build -d
```

Then run `scripts/smoke-critical-api.sh` on Unix-like systems or `scripts/smoke-critical-api.ps1` in PowerShell.

Optional local backend tests:

```bash
cd apps/backend
mvn test
mvn package
```

Optional local frontend tests:

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
