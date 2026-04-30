# Run from Flutter project root:  powershell -File android/get-debug-sha1.ps1
# Or from android folder:          powershell -File get-debug-sha1.ps1

$ErrorActionPreference = "Stop"
$here = $PSScriptRoot
if (-not (Test-Path (Join-Path $here "gradlew.bat"))) {
    Write-Host "Run this from your Flutter project's android folder (gradlew.bat must exist here)." -ForegroundColor Red
    Write-Host "Current: $here" -ForegroundColor Yellow
    exit 1
}

Write-Host "Running Gradle signingReport..." -ForegroundColor Cyan
Push-Location $here
try {
    & .\gradlew.bat signingReport
} finally {
    Pop-Location
}

Write-Host ""
Write-Host "=== Next steps ===" -ForegroundColor Green
Write-Host "1. Under 'Variant: debug', copy SHA1: xx:xx:xx:..."
Write-Host "2. Google Cloud -> APIs & Services -> Credentials -> Create OAuth client ID -> Android"
Write-Host "   Package: com.myledger.my_ledger_app"
Write-Host "3. Copy the Android OAuth Client ID (ends with .apps.googleusercontent.com)"
Write-Host "4. Run: powershell -File android/generate-google-services.ps1 -AndroidClientId \"YOUR_ANDROID_CLIENT_ID\""
Write-Host ""
