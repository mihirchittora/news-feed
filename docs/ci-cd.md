# CI/CD and release promotion

The repository contains four workflows under `.github/workflows`:

- `ci.yml` runs backend verification, frontend typecheck/tests/build, both Docker builds, Terraform formatting/validation, and a toolchain smoke job on Ubuntu, macOS, and Windows runners.
- `build-images.yml` runs on `main` and manual dispatch. It builds each image once, scans both local images with Trivy, pushes a full-commit-SHA tag to private OCIR, and records the resulting manifest digests as an artifact.
- `deploy-staging.yml` follows a successful main-branch image build or can be dispatched with a full commit SHA. It resolves the tag to its registry digest, writes the staging Terraform variables from a protected GitHub secret, applies the plan, and smoke-tests the load-balancer URL.
- `deploy-production.yml` is manual only. The `production` GitHub environment should require reviewer approval. It accepts only a full commit SHA, optionally checks configured staging evidence, resolves the exact same OCIR artifact digests, applies production, and runs read-only smoke tests.

Promotion is digest-based:

```text
commit SHA -> build once -> scan -> OCIR SHA tag -> resolve digest -> staging -> production
```

No workflow deploys `latest`. A rollback selects the previous successful commit SHA and resolves that tag to its immutable digest. Database migrations are forward-only; reverting an application image does not revert a migration.

## GitHub configuration

Set these as repository or environment variables:

- `OCIR_REGISTRY`, for example `fra.ocir.io`.
- `OCIR_NAMESPACE`.
- Optional `BACKEND_IMAGE_NAME` and `WEB_IMAGE_NAME`.
- Production-only `STAGING_FRONTEND_URL` and `STAGING_COMMIT_SHA` if staging promotion evidence is enforced.

Set these as secrets. Staging and production should use distinct values and state endpoints:

- `OCIR_USERNAME`, `OCIR_AUTH_TOKEN`.
- `STAGING_TFVARS`, `PRODUCTION_TFVARS`: complete environment-specific variable files based on the checked-in examples, excluding image values supplied by the workflow.
- Set `database_password_secret_version` in each variable file to the OCI Vault version used by managed PostgreSQL. The application runtime reads the latest configured secret; update the database resource and runtime together when rotating this credential.
- `STAGING_TF_HTTP_ADDRESS`, `STAGING_TF_HTTP_USERNAME`, `STAGING_TF_HTTP_PASSWORD`, `STAGING_TF_HTTP_LOCK_ADDRESS`, `STAGING_TF_HTTP_UNLOCK_ADDRESS`.
- The equivalent `PRODUCTION_TF_HTTP_*` values.

Protect the `staging` and `production` environments, require approval for production, and configure main-branch required checks for `CI`. The workflow files do not create GitHub environment protection rules or secrets, so those are not claimed as completed here.

The OCIR token used by GitHub Actions is only a build/push credential. Running containers use OCI resource principals for Vault and Object Storage. Keep those trust paths separate and rotate the CI token through the repository/environment secret.
