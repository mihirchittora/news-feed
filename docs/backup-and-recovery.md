# Backup and recovery

This document describes the responsibilities of the deployment operator. The repository does not provision a cloud backup vendor.

## PostgreSQL

Create a compressed logical backup through the Compose PostgreSQL container. This avoids requiring PostgreSQL client tools on Windows, macOS, or Linux:

```text
docker compose exec -T postgres sh -c 'pg_dump --format=custom --file=/tmp/news-platform.dump -U "$POSTGRES_USER" -d "$POSTGRES_DB"'
docker compose cp postgres:/tmp/news-platform.dump ./news-platform.dump
docker compose exec -T postgres rm -f /tmp/news-platform.dump
```

Restore into an empty database after stopping the API. Copy the dump into the PostgreSQL container and restore it through the container tools:

```text
docker compose cp ./news-platform.dump postgres:/tmp/news-platform.dump
docker compose exec -T postgres sh -c 'createdb -U "$POSTGRES_USER" news_platform_restore'
docker compose exec -T postgres sh -c 'pg_restore --clean --if-exists --dbname=news_platform_restore -U "$POSTGRES_USER" /tmp/news-platform.dump'
docker compose exec -T postgres rm -f /tmp/news-platform.dump
```

For a Compose development database, the `news_platform_postgres_data` named volume is persistent across normal `docker compose down`. A volume is not a backup: dump it using the database operator procedure above.

## Media and newspaper files

Back up the configured object-storage bucket, or the local `MEDIA_STORAGE_PATH` directory when using the development adapter. Preserve the `newspapers/YYYY/MM/DD/` namespace and all story/ad media keys. Database restore without the matching storage backup leaves references unusable.

## Recovery scenarios

- **Database loss:** provision an empty PostgreSQL instance, restore the latest backup, verify `flyway_schema_history`, then start the API against it.
- **Media loss:** restore the matching media backup first. Run the Unix `.sh` or Windows `.ps1` consistency helper and investigate every missing storage reference before publishing content.
- **Failed deployment:** stop the new version, restore the prior image/configuration, and roll back only migrations using an approved database procedure. Do not edit an already-applied migration.
- **Bad migration:** stop application writes, take a fresh backup, diagnose the migration failure, and ship a new forward migration or restore the pre-migration backup. The current application does not provide automatic destructive migration rollback.

## Recovery validation

At least once per release cycle, restore a copy to an isolated environment and verify: login, RBAC, a published story and media, comments, public newspaper metadata, authenticated newspaper PDF access, ads, dashboard, `/actuator/health/readiness`, and the consistency checker. Record the restore timestamp, backup identifier, schema version, and findings.
