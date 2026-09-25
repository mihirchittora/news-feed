$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot
$envFile = if (Test-Path ".env") { ".env" } else { ".env.example" }
$compose = @("compose", "--env-file", $envFile)

Write-Output "--- report-only data consistency check ---"
Get-Content -Raw (Join-Path $PSScriptRoot "check-data-consistency.sql") |
    & docker @compose exec -T postgres sh -c 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" --set ON_ERROR_STOP=1 -f -'
if ($LASTEXITCODE -ne 0) { throw "The database consistency query failed." }

Write-Output "--- Docker media-volume references missing from disk ---"
$referenceSql = @"
SELECT storage_key FROM story_media WHERE storage_key IS NOT NULL
UNION
SELECT pdf_storage_key FROM newspaper_editions WHERE pdf_storage_key IS NOT NULL
UNION
SELECT cover_image_storage_key FROM newspaper_editions WHERE cover_image_storage_key IS NOT NULL
UNION
SELECT media_storage_key FROM advertisements WHERE media_storage_key IS NOT NULL
ORDER BY 1;
"@
$keys = $referenceSql | & docker @compose exec -T postgres sh -c 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -At --set ON_ERROR_STOP=1 -f -'
if ($LASTEXITCODE -ne 0) { throw "The storage reference query failed." }

foreach ($key in ($keys -split "`r?`n")) {
    if ([string]::IsNullOrWhiteSpace($key)) { continue }
    if ($key.Contains("..") -or $key.StartsWith("/") -or $key.Contains("\")) {
        Write-Output "unsafe storage key: $key"
        continue
    }
    & docker @compose exec -T backend sh -c 'test -f "/data/media/$1"' -- $key
    if ($LASTEXITCODE -ne 0) { Write-Output $key }
}
