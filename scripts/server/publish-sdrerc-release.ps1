param(
    [Parameter(Mandatory = $true)]
    [string]$Version,

    [string]$ReleaseRoot = "D:\SDRERC_RELEASES",

    [string]$Notes = "Release SDRERC V2 LAN",

    [int]$HttpPort = 8088,

    [switch]$SkipBuild
)

$ErrorActionPreference = "Stop"

function Ensure-Directory {
    param([string]$PathValue)
    if (-not (Test-Path -LiteralPath $PathValue)) {
        New-Item -ItemType Directory -Path $PathValue -Force | Out-Null
    }
}

function Write-JsonFile {
    param(
        [object]$Value,
        [string]$PathValue
    )
    $Value | ConvertTo-Json -Depth 8 | Set-Content -LiteralPath $PathValue -Encoding UTF8
}

if ($Version -notmatch "^\d+\.\d+\.\d+([.-][A-Za-z0-9]+)?$") {
    throw "Version invalida. Use un formato como 1.0.1."
}

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$repoRoot = Resolve-Path (Join-Path $scriptDir "..\..")
$jarPath = Join-Path $repoRoot "target\SDRERC-V2.jar"
$workRoot = Join-Path $repoRoot "target\release-lan"
$releaseWork = Join-Path $workRoot ("SDRERC-V2-{0}" -f $Version)
$packageRoot = Join-Path $releaseWork "package"
$zipPath = Join-Path $releaseWork "SDRERC-V2.zip"
$latestDir = Join-Path $ReleaseRoot "latest"
$versionedDir = Join-Path (Join-Path $ReleaseRoot "versions") $Version

if (-not $SkipBuild) {
    $mvn = Get-Command mvn -ErrorAction SilentlyContinue
    if (-not $mvn) {
        throw "Maven no esta disponible en PATH."
    }

    Push-Location $repoRoot
    try {
        Write-Host "Ejecutando mvn clean compile..."
        & $mvn.Source clean compile
        if ($LASTEXITCODE -ne 0) { throw "mvn clean compile fallo con codigo $LASTEXITCODE." }

        Write-Host "Ejecutando mvn clean package..."
        & $mvn.Source clean package
        if ($LASTEXITCODE -ne 0) { throw "mvn clean package fallo con codigo $LASTEXITCODE." }
    }
    finally {
        Pop-Location
    }
}

if (-not (Test-Path -LiteralPath $jarPath)) {
    throw "No existe el JAR esperado: $jarPath. Ejecute mvn clean package."
}

if (Test-Path -LiteralPath $releaseWork) {
    Remove-Item -LiteralPath $releaseWork -Recurse -Force
}
Ensure-Directory $packageRoot
Ensure-Directory (Join-Path $packageRoot "config")

Copy-Item -LiteralPath $jarPath -Destination (Join-Path $packageRoot "SDRERC-V2.jar") -Force

$configExample = @"
# Configuracion ejemplo del cliente SDRERC V2.
# No guardar passwords reales en repositorio ni en paquetes compartidos.
sdrerc.db.url=jdbc:oracle:thin:@//IP_SERVIDOR:1521/XEPDB1
sdrerc.db.user=SDRERC_APP
sdrerc.db.password=
"@
$configExample | Set-Content -LiteralPath (Join-Path $packageRoot "config\sdrerc-app.properties.example") -Encoding UTF8

$versionInfo = [ordered]@{
    app = "SDRERC-V2"
    version = $Version
    releaseDate = (Get-Date -Format "yyyy-MM-dd")
    zipFile = "SDRERC-V2.zip"
    mainJar = "SDRERC-V2.jar"
    minJavaVersion = "1.8"
    notes = $Notes
}
Write-JsonFile -Value $versionInfo -PathValue (Join-Path $packageRoot "version-local.json")

if (Test-Path -LiteralPath $zipPath) {
    Remove-Item -LiteralPath $zipPath -Force
}
Compress-Archive -Path (Join-Path $packageRoot "*") -DestinationPath $zipPath -Force

$hash = (Get-FileHash -LiteralPath $zipPath -Algorithm SHA256).Hash.ToUpperInvariant()
$checksums = "{0}  {1}" -f $hash, "SDRERC-V2.zip"

Ensure-Directory $latestDir
Ensure-Directory $versionedDir

Copy-Item -LiteralPath $zipPath -Destination (Join-Path $latestDir "SDRERC-V2.zip") -Force
Write-JsonFile -Value $versionInfo -PathValue (Join-Path $latestDir "version.json")
$checksums | Set-Content -LiteralPath (Join-Path $latestDir "checksums.txt") -Encoding ASCII

Copy-Item -LiteralPath $zipPath -Destination (Join-Path $versionedDir "SDRERC-V2.zip") -Force
Write-JsonFile -Value $versionInfo -PathValue (Join-Path $versionedDir "version.json")
$checksums | Set-Content -LiteralPath (Join-Path $versionedDir "checksums.txt") -Encoding ASCII

Write-Host "Release publicado correctamente."
Write-Host "Version: $Version"
Write-Host "Latest:  $latestDir"
Write-Host "Archivo: SDRERC-V2.zip"
Write-Host "SHA256:  $hash"
Write-Host ""
Write-Host "Ejemplo de publicacion HTTP local/VPN:"
Write-Host "  cd $latestDir"
Write-Host "  python -m http.server $HttpPort"
Write-Host ""
Write-Host "URLs esperadas:"
Write-Host "  http://IP_O_NOMBRE_SERVIDOR:$HttpPort/version.json"
Write-Host "  http://IP_O_NOMBRE_SERVIDOR:$HttpPort/SDRERC-V2.zip"
Write-Host "  http://IP_O_NOMBRE_SERVIDOR:$HttpPort/checksums.txt"
