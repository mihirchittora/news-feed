# OCI deployment runbook

This is the operator procedure for the Terraform roots in `infra/environments/staging` and `infra/environments/production`.

## Preflight

- Confirm the target GitHub environment, OCI compartment, region, availability domain, DNS, certificate, notification topic, Vault policies, and private Terraform HTTP state are correct.
- Confirm the database, JWT, optional initial-admin, and OCIR image-pull secret OCIDs are in the environment's protected `*.tfvars` secret. Never place secret values in the repository.
- Confirm the selected commit passed CI and `Build immutable images`; use the full 40-character SHA, not a branch name.
- For production, verify the selected commit is the staging-approved release and obtain the configured environment approval.

## Normal staging deployment

1. Merge to `main` after CI passes. The image workflow builds and scans the backend and web images and pushes SHA-tagged images.
2. Allow `Deploy staging` to run, or dispatch it with a known full commit SHA.
3. Inspect the Terraform plan and workflow summary. Confirm both image references contain `@sha256:` digests.
4. Confirm smoke tests pass through the load balancer: readiness, feed, categories, breaking news, newspaper metadata, and ads.
5. Record the promoted SHA, two image digests, Terraform state revision, and smoke-test timestamp.

## Normal production deployment

1. Dispatch `Deploy production` with the full SHA already validated in staging.
2. Approve the protected GitHub production environment after reviewing the plan, migration changes, backup status, and release notes.
3. The single backend container runs Flyway during startup. Do not start an additional ad-hoc migration job or manually edit `flyway_schema_history`.
4. Wait for the production smoke tests and record the resulting endpoint, SHA, digests, and Terraform state revision.

## Rollback

1. Stop promotion if readiness or any smoke test fails. Preserve the failed deployment's workflow log, image digest, application logs, and migration error.
2. Identify the last successful production SHA from the workflow summary. Dispatch production with that exact SHA; do not rebuild or use `latest`.
3. If a migration was applied, do not assume the old image is schema-compatible. Assess compatibility first. Ship a forward fix or restore a database backup using the approved recovery plan.
4. Verify readiness, public feed/category routes, authenticated access, media, and newspaper PDF authorization after rollback.

## Incident checks

- **Readiness fails:** check PostgreSQL connectivity, Flyway output, Vault permissions, Object Storage namespace/bucket values, and the container's resource-principal policy.
- **Image pull fails:** verify the Container Instance Vault image-pull secret, OCIR registry endpoint, repository path, and image digest permissions.
- **503 from the load balancer:** inspect container health checks and private VNIC/NSG rules. Verify the load-balancer backend IP still matches the current Container Instance VNIC.
- **Media fails while API is healthy:** verify the bucket is private but accessible to the runtime principal, the bucket name/namespace are correct, and the object key exists. Do not make the bucket public as a first response.
- **TLS/DNS fails:** verify the certificate OCID, listener, DNS record, and load-balancer address.

## Recovery and deletion guardrails

Take or confirm a database recovery point before destructive infrastructure changes. Do not run `terraform destroy` against a production state as a rollback mechanism. The bucket is versioned, but object and database retention still require an operator backup policy; see [`backup-and-recovery.md`](backup-and-recovery.md).
