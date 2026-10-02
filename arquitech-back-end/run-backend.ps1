$ErrorActionPreference = 'Stop'

function Test-Java17Home([string]$Candidate) {
    if ([string]::IsNullOrWhiteSpace($Candidate)) { return $false }
    $javaExe = Join-Path $Candidate 'bin\java.exe'
    if (-not (Test-Path -LiteralPath $javaExe -PathType Leaf)) { return $false }
    $versionText = (& $javaExe -version 2>&1 | Out-String)
    return $versionText -match '(?:java|openjdk) version "17(?:\.|\")'
}

$java17Home = $null
if (Test-Java17Home $env:JAVA17_HOME) {
    $java17Home = (Resolve-Path -LiteralPath $env:JAVA17_HOME).Path
}

if (-not $java17Home) {
    $searchRoots = @(
        'C:\Program Files\Eclipse Adoptium',
        'C:\Program Files\Java',
        'C:\Program Files\Microsoft'
    )
    foreach ($root in $searchRoots) {
        if (-not (Test-Path -LiteralPath $root -PathType Container)) { continue }
        $candidates = Get-ChildItem -LiteralPath $root -Directory -ErrorAction SilentlyContinue |
            Where-Object { $_.Name -match '(?i)(jdk|java).*17|17.*(jdk|java)' } |
            Sort-Object Name -Descending
        foreach ($candidate in $candidates) {
            if (Test-Java17Home $candidate.FullName) {
                $java17Home = $candidate.FullName
                break
            }
        }
        if ($java17Home) { break }
    }
}

if (-not $java17Home) {
    throw 'No se encontro un JDK 17 valido. Define JAVA17_HOME con la carpeta del JDK 17.'
}

$wrapper = Join-Path $PSScriptRoot 'mvnw.cmd'
if (-not (Test-Path -LiteralPath $wrapper -PathType Leaf)) {
    throw "No se encontro Maven Wrapper en $wrapper"
}

if ([string]::IsNullOrWhiteSpace($env:PROD_DB_PASSWORD)) {
    $securePassword = Read-Host 'Contrasena local de MySQL (PROD_DB_PASSWORD)' -AsSecureString
    $passwordPointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
    try {
        $env:PROD_DB_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($passwordPointer)
    } finally {
        [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($passwordPointer)
    }
}

if ([string]::IsNullOrWhiteSpace($env:JWT_SECRET)) {
    $secretBytes = [byte[]]::new(48)
    [Security.Cryptography.RandomNumberGenerator]::Fill($secretBytes)
    $env:JWT_SECRET = [Convert]::ToBase64String($secretBytes)
    Write-Host 'JWT_SECRET temporal generado para esta ejecucion.'
}

if ([string]::IsNullOrWhiteSpace($env:PROD_DB_USERNAME)) { $env:PROD_DB_USERNAME = 'root' }
if ([string]::IsNullOrWhiteSpace($env:PORT)) { $env:PORT = '8080' }
if ([string]::IsNullOrWhiteSpace($env:DDL_AUTO)) { $env:DDL_AUTO = 'update' }
if ([string]::IsNullOrWhiteSpace($env:SHOW_SQL)) { $env:SHOW_SQL = 'false' }
if ([string]::IsNullOrWhiteSpace($env:CORS_ALLOWED_ORIGINS)) { $env:CORS_ALLOWED_ORIGINS = 'http://localhost:4200' }

# Environment changes are scoped to this PowerShell process and its Maven child process.
$env:JAVA_HOME = $java17Home
$env:Path = "$(Join-Path $java17Home 'bin');$env:Path"

Write-Host "Usando JDK 17: $java17Home"
& (Join-Path $java17Home 'bin\java.exe') -version
Push-Location $PSScriptRoot
try {
    & $wrapper spring-boot:run
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
} finally {
    Pop-Location
}
