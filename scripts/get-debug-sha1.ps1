# Get Android debug SHA-1 for Google Sign-In / Firebase
# Usage (from your FLUTTER project root, not my-ledger-be):
#   powershell -File "<path-to-my-ledger-be>\scripts\get-debug-sha1.ps1" -FlutterRoot "D:\path\to\my_ledger_app"
# Or copy this file to your Flutter project as android\get-debug-sha1.ps1 and run:
#   powershell -File android\get-debug-sha1.ps1
#testing
param(
    [string]$FlutterRoot = ""
)

$ErrorActionPreference = "Stop"

function Find-AndroidDir {
    param([string]$Root)
    if ([string]::IsNullOrWhiteSpace($Root)) {
        $Root = Get-Location
    }
    $r = Resolve-Path -LiteralPath $Root -ErrorAction SilentlyContinue
    if (-not $r) {
        Write-Host "Path not found: $Root" -ForegroundColor Red
        exit 1
    }
    $rootPath = $r.Path
    $android = Join-Path $rootPath "android"
    if (Test-Path (Join-Path $android "gradlew.bat")) {
        return $android
    }
    # Already inside android folder?
    if (Test-Path (Join-Path $rootPath "gradlew.bat")) {
        return $rootPath
    }
    Write-Host "Could not find android\gradlew.bat under: $rootPath" -ForegroundColor Red
    Write-Host "Pass -FlutterRoot to your Flutter app folder (contains pubspec.yaml and android\)." -ForegroundColor Yellow
    exit 1
}

$androidDir = Find-AndroidDir -Root $FlutterRoot
Write-Host "Using Android project: $androidDir" -ForegroundColor Cyan
Push-Location $androidDir
try {
    if (-not (Test-Path ".\gradlew.bat")) {
        Write-Host "gradlew.bat missing. Run 'flutter pub get' in your Flutter project first." -ForegroundColor Red
        exit 1
    }
    Write-Host "Running: .\gradlew.bat signingReport (this may take a minute)..." -ForegroundColor Cyan
    & .\gradlew.bat signingReport 2>&1 | Out-String | Write-Host
} finally {
    Pop-Location
}

Write-Host ""
Write-Host "=== WHAT TO COPY ===" -ForegroundColor Green
Write-Host "1. Find the block: Variant: debug"
Write-Host "2. Copy the line: SHA1: xx:xx:xx:..."
Write-Host "3. Google Cloud Console -> APIs & Services -> Credentials -> Create OAuth client ID -> Android"
Write-Host "   Package name: com.myledger.my_ledger_app"
Write-Host "   SHA-1: (paste)"
Write-Host "4. If you use Firebase: Project settings -> Your Android app -> Add fingerprint -> same SHA-1"
Write-Host "5. Download fresh google-services.json if you use Firebase"
Write-Host ""
