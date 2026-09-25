$ErrorActionPreference = "Stop"

$apiUrl = if ($env:API_URL) { $env:API_URL.TrimEnd("/") } else { "http://localhost:8080" }
$email = "milestone8-smoke-$([DateTimeOffset]::UtcNow.ToUnixTimeSeconds())-$PID@example.invalid"
$password = "SmokePass123!"

function Invoke-Api {
    param(
        [Parameter(Mandatory = $true)][string]$Method,
        [Parameter(Mandatory = $true)][string]$Uri,
        [hashtable]$Headers = @{},
        [string]$Body,
        [string]$ContentType = "application/json"
    )

    try {
        $response = Invoke-WebRequest -UseBasicParsing -Method $Method -Uri $Uri -Headers $Headers -Body $Body -ContentType $ContentType
        return [pscustomobject]@{ StatusCode = [int]$response.StatusCode; Headers = $response.Headers; Content = $response.Content }
    } catch {
        if (-not $_.Exception.Response) { throw }
        $response = $_.Exception.Response
        $reader = New-Object System.IO.StreamReader($response.GetResponseStream())
        try { $content = $reader.ReadToEnd() } finally { $reader.Dispose() }
        return [pscustomobject]@{ StatusCode = [int]$response.StatusCode; Headers = $response.Headers; Content = $content }
    }
}

$health = Invoke-Api -Method Get -Uri "$apiUrl/actuator/health/readiness"
if ($health.StatusCode -ne 200) { throw "Readiness returned $($health.StatusCode)." }
if (-not $health.Headers["X-Request-Id"]) { throw "Readiness did not return X-Request-Id." }

$registerBody = @{ name = "Milestone Smoke"; email = $email; password = $password } | ConvertTo-Json -Compress
$register = Invoke-Api -Method Post -Uri "$apiUrl/api/v1/auth/register" -Body $registerBody
if ($register.StatusCode -ne 201 -and $register.StatusCode -ne 409) { throw "Registration returned $($register.StatusCode)." }

$loginBody = @{ email = $email; password = $password } | ConvertTo-Json -Compress
$login = Invoke-Api -Method Post -Uri "$apiUrl/api/v1/auth/login" -Body $loginBody
if ($login.StatusCode -ne 200) { throw "Login returned $($login.StatusCode)." }
$token = ($login.Content | ConvertFrom-Json).accessToken
if ([string]::IsNullOrWhiteSpace($token)) { throw "Login did not return an access token." }

$authHeaders = @{ Authorization = "Bearer $token" }
$me = Invoke-Api -Method Get -Uri "$apiUrl/api/v1/users/me" -Headers $authHeaders
if ($me.StatusCode -ne 200) { throw "Current-user request returned $($me.StatusCode)." }

$dashboard = Invoke-Api -Method Get -Uri "$apiUrl/api/v1/admin/dashboard" -Headers $authHeaders
if ($dashboard.StatusCode -ne 403) { throw "Normal-user dashboard request returned $($dashboard.StatusCode), expected 403." }

$validation = Invoke-Api -Method Post -Uri "$apiUrl/api/v1/auth/login" -Headers @{ "X-Request-Id" = "smoke-validation" } -Body '{"email":"not-an-email","password":"x"}'
if ($validation.StatusCode -ne 400) { throw "Validation request returned $($validation.StatusCode)." }
$validationError = $validation.Content | ConvertFrom-Json
if ($validationError.code -ne "VALIDATION_ERROR" -or $validationError.requestId -ne "smoke-validation") {
    throw "Validation response did not contain the expected standardized error and request ID."
}

Write-Output "critical API smoke passed: health=$($health.StatusCode) register=$($register.StatusCode) login=$($login.StatusCode) me=$($me.StatusCode) user_dashboard=$($dashboard.StatusCode) validation=$($validation.StatusCode)"
