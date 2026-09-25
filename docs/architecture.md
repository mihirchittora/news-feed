# Production architecture

The platform remains a modular monolith:

```text
Next.js standalone web container
              |
              v
Spring Boot REST API (JWT, RBAC, Actuator)
              |
       PostgreSQL + Flyway
              |
  local/object media abstraction
```

Spring modules are separated by domain (`auth`, `user`, `rbac`, `story`, `category`, `tag`, `engagement`, `newspaper`, `advertisement`, and `dashboard`). Shared error handling, security, time, and storage configuration live under `common`.

Important runtime decisions:

- PostgreSQL remains the source of truth for authorization, schedules, publication state, and engagement.
- Flyway migrations are append-only. Milestone 8 adds `V12__harden_integrity_constraints.sql`; previously applied migrations are not edited.
- JWTs are short-lived and contain only the user ID and legacy role claim. Each request rechecks the user status and reloads active roles/permissions.
- Uploads are streamed to a temporary file, validated by declared type plus magic bytes, then atomically moved to a generated key. Original filenames are never used as storage paths.
- Public newspaper metadata/covers are separate from the authenticated published-PDF endpoint.
- Public advertisements are selected by database schedule/status predicates and category placement matching.
- Request IDs are accepted only from a bounded safe character set, generated when absent, returned as `X-Request-Id`, placed in MDC, and included in API errors.
- Actuator exposes health, liveness/readiness, and metrics only. Detailed environment and bean endpoints are not exposed.

The application intentionally does not add Redis, a queue, Elasticsearch, a gateway, or other speculative infrastructure for this milestone.
