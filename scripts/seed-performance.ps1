$ErrorActionPreference = "Stop"

$environment = if ($env:APP_ENVIRONMENT) { $env:APP_ENVIRONMENT } else { "development" }
$profile = if ($env:SPRING_PROFILES_ACTIVE) { $env:SPRING_PROFILES_ACTIVE } else { "" }
if ($environment -eq "production" -or $profile -match "prod") {
    throw "Refusing to seed performance data in production."
}

$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot
$envFile = if (Test-Path ".env") { ".env" } else { ".env.example" }
$compose = @("compose", "--env-file", $envFile)
Get-Content -Raw (Join-Path $PSScriptRoot "seed-performance.sql") |
    & docker @compose exec -T postgres sh -c 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" --set ON_ERROR_STOP=1 -f -'
if ($LASTEXITCODE -ne 0) { throw "The performance seed failed." }
