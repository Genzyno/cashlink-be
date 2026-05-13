# Patches android/app/google-services.json oauth_client list with your Android OAuth client + SHA-1.
# Prerequisite: You already added Android OAuth client in Google Cloud and downloaded google-services.json from Firebase once.
#
# Usage (from Flutter project root):
#   powershell -File android/generate-google-services.ps1 -AndroidClientId "123456-abc.apps.googleusercontent.com" -Sha1 "AA:BB:CC:..."
#
# Get SHA1 from: android/get-debug-sha1.ps1  (Variant: debug -> SHA1 line)

param(
    [Parameter(Mandatory = $true)]
    [string]$AndroidClientId,
    [Parameter(Mandatory = $true)]
    [string]$Sha1,
    [string]$PackageName = "com.cashlink.my_ledger_app",
    [string]$WebClientId = "",
    [string]$GoogleServicesPath = ""
)

$ErrorActionPreference = "Stop"

function Normalize-CertHash([string]$sha1) {
    return ($sha1.Trim() -replace ':', '').ToLowerInvariant()
}

$certHash = Normalize-CertHash $Sha1
if ($certHash.Length -lt 20) {
    Write-Host "SHA-1 looks too short. Use full SHA1 from signingReport (e.g. AA:BB:CC:...)." -ForegroundColor Red
    exit 1
}

$androidDir = $PSScriptRoot
if ([string]::IsNullOrWhiteSpace($GoogleServicesPath)) {
    $GoogleServicesPath = Join-Path $androidDir "..\app\google-services.json"
}
$GoogleServicesPath = Resolve-Path -LiteralPath $GoogleServicesPath -ErrorAction SilentlyContinue
if (-not $GoogleServicesPath) {
    Write-Host "google-services.json not found." -ForegroundColor Red
    Write-Host "1. Firebase Console -> Project settings -> Your Android app -> Download google-services.json"
    Write-Host "2. Save to: android\app\google-services.json"
    Write-Host "3. Run this script again."
    exit 1
}

$jsonPath = $GoogleServicesPath.Path
$raw = Get-Content -LiteralPath $jsonPath -Raw -Encoding UTF8
$j = $raw | ConvertFrom-Json

if (-not $j.client -or $j.client.Count -lt 1) {
    Write-Host "Invalid google-services.json: missing 'client' array. Re-download from Firebase." -ForegroundColor Red
    exit 1
}

$target = $null
foreach ($c in $j.client) {
    $pn = $c.client_info.android_client_info.package_name
    if ($pn -eq $PackageName) {
        $target = $c
        break
    }
}
if (-not $target) {
    Write-Host "No client entry for package '$PackageName'. Add Android app in Firebase with this package name, then re-download JSON." -ForegroundColor Red
    exit 1
}

if (-not $target.oauth_client) {
    $target | Add-Member -NotePropertyName oauth_client -NotePropertyValue @() -Force
}

$oauth = [System.Collections.ArrayList]@()
foreach ($o in $target.oauth_client) {
    # Drop old Android client (type 1) for same package so we can replace
    if ($o.client_type -eq 1 -and $o.android_info.package_name -eq $PackageName) {
        continue
    }
    [void]$oauth.Add($o)
}

$androidInfo = New-Object psobject -Property @{
    package_name       = $PackageName
    certificate_hash   = $certHash
}
$newAndroid = New-Object psobject -Property @{
    client_id    = $AndroidClientId.Trim()
    client_type  = 1
    android_info = $androidInfo
}
[void]$oauth.Add($newAndroid)

if (-not [string]::IsNullOrWhiteSpace($WebClientId)) {
    $hasWeb = $false
    foreach ($o in $oauth) {
        if ($o.client_type -eq 3) { $hasWeb = $true; break }
    }
    if (-not $hasWeb) {
        $newWeb = New-Object psobject -Property @{
            client_id   = $WebClientId.Trim()
            client_type = 3
        }
        [void]$oauth.Add($newWeb)
    }
}

$target.oauth_client = @($oauth)

# PowerShell ConvertTo-Json may flatten oddly for nested objects; use depth
$out = $j | ConvertTo-Json -Depth 20
[System.IO.File]::WriteAllText($jsonPath, $out, [System.Text.UTF8Encoding]::new($false))

Write-Host "Updated: $jsonPath" -ForegroundColor Green
Write-Host "Rebuild: flutter clean && flutter run" -ForegroundColor Cyan
