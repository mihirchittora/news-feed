# Production readiness checklist

Legend: `IMPLEMENTED` means code/configuration exists; `VERIFIED` means the command or environment check was actually run; `DOCUMENTED ONLY` means an operator procedure exists; `REMAINING RISK` means it still needs deployment-specific validation.

## SECURITY

- [x] `IMPLEMENTED` Production secret validation, JWT minimum length, password hashing, disabled-user revalidation
- [x] `IMPLEMENTED` Explicit CORS origins; wildcard origins rejected
- [x] `IMPLEMENTED` CSP, frame, content-type, referrer, permissions, and conditional HSTS headers
- [x] `IMPLEMENTED` Upload size, magic-byte, generated-key, and atomic-write validation
- [x] `IMPLEMENTED` Rich-text allowlist and plain-text comment handling
- [x] `IMPLEMENTED` Setup-token expiry, hashing, single-use locking, and auth endpoint rate limiting
- [ ] `REMAINING RISK` HTTPS termination, secret rotation, WAF/proxy limits, and external security testing

## DATABASE

- [x] `IMPLEMENTED` Append-only Flyway V12 integrity constraints and query indexes
- [x] `VERIFIED` Existing schema upgraded from Flyway V11 to V12 during local Compose startup
- [x] `VERIFIED` Fresh disposable PostgreSQL volume initialized and migrated through V12
- [x] `IMPLEMENTED` Transactional service methods for publication, RBAC, moderation, newspaper, and advertisement changes
- [ ] `REMAINING RISK` Upgrade migration must be exercised against the operator's real previous-milestone database
- [ ] `DOCUMENTED ONLY` Backup/restore process in [`backup-and-recovery.md`](backup-and-recovery.md)

## APPLICATION / OPERATIONS

- [x] `IMPLEMENTED` Request IDs, MDC, structured request completion logs, standardized errors
- [x] `IMPLEMENTED` Actuator health/liveness/readiness/metrics exposure with readiness including PostgreSQL
- [x] `IMPLEMENTED` Graceful shutdown timeout and Docker health dependency ordering
- [x] `VERIFIED` Local Compose startup reached healthy PostgreSQL/backend containers; web container served successfully
- [x] `VERIFIED` Report-only consistency check found zero broken foreign-key references; one unattached media upload is valid draft workflow state
- [ ] `REMAINING RISK` Alert thresholds, log shipping, dashboards, and on-call ownership are deployment-specific

## FRONTEND

- [x] `IMPLEMENTED` Next.js error and not-found boundaries, safe API error messaging, metadata, sitemap, robots rules, and security headers
- [ ] `REMAINING RISK` Accessibility and responsive verification at 360/390/430/768/1024/1440px needs a browser QA pass
- [ ] `REMAINING RISK` Production canonical/site/API URLs and social images need deployment values

## CROSS-PLATFORM

- [x] `IMPLEMENTED` Compose development defaults allow `docker compose up --build` from a clean checkout without a host `.env`; production still requires explicit secrets/configuration
- [x] `IMPLEMENTED` PostgreSQL data and application media use Docker named volumes; no host filesystem mounts are required
- [x] `IMPLEMENTED` Docker build contexts use repository-relative paths and multi-architecture base images; containers do not require host Java, Node, npm, Maven, or PostgreSQL
- [x] `IMPLEMENTED` `.gitattributes` enforces LF text checkout and UTF-8 build encoding; no case-insensitive duplicate paths were found
- [x] `IMPLEMENTED` Unix helpers have equivalent PowerShell helpers; database and media checks run inside Compose containers
- [ ] `REMAINING RISK` This workspace could not execute the modern `docker compose` plugin or Windows PowerShell; run the clean-checkout workflow once on each target OS/desktop runtime

## TESTING

- [x] `VERIFIED` Backend baseline unit tests: 38 passed before the final patch
- [x] `VERIFIED` Backend final suite: 40 tests passed; frontend typecheck, 16 Vitest tests, and production build passed
- [x] `VERIFIED` Live API smoke: registration/login/me, normal-user dashboard denial, validation error, request ID, and auth 429
- [x] `IMPLEMENTED` Reusable critical API smoke script at [`scripts/smoke-critical-api.sh`](../scripts/smoke-critical-api.sh)
- [ ] `REMAINING RISK` Full browser E2E journeys and failure matrix are not yet executed in this workspace
- [ ] `DOCUMENTED ONLY` Real backup/restore drill against operator-managed backup media is not yet executed in this workspace
- [ ] `REMAINING RISK` The local verification environment lacked the modern `docker compose` plugin; the repository workflow now targets the cross-platform Compose plugin required by Docker Desktop/Linux.
- [ ] `REMAINING RISK` The web image's `npm ci` audit reports 5 transitive vulnerabilities (3 moderate, 1 high, 1 critical); dependency remediation needs ownership and review

## RELEASE GATE

Do not declare production-ready until the remaining deployment-specific checks above are completed, a real backup restore is recorded, dependency audit findings are resolved or accepted, and the Docker clean/E2E tests pass.
