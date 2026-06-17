param(
    [string]$OutputDir,
    [switch]$SkipBuild
)

$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$root = Resolve-Path (Join-Path $scriptDir "..\..")
if ([string]::IsNullOrWhiteSpace($OutputDir)) {
    $OutputDir = Join-Path $root "dist\sdrerc-client"
}

if (-not $SkipBuild) {
    $mvn = Get-Command mvn -ErrorAction SilentlyContinue
    if (-not $mvn) {
        throw "Maven no esta disponible en PATH. Instale Maven o ejecute mvn clean package antes de usar -SkipBuild."
    }
    Push-Location $root
    try {
        & $mvn.Source clean package
    } finally {
        Pop-Location
    }
}

$jar = Join-Path $root "target\SDRERC-V2.jar"
if (-not (Test-Path $jar)) {
    throw "No se encontro $jar. Ejecute mvn clean package."
}

New-Item -ItemType Directory -Path $OutputDir -Force | Out-Null
New-Item -ItemType Directory -Path (Join-Path $OutputDir "config") -Force | Out-Null
New-Item -ItemType Directory -Path (Join-Path $OutputDir "lib") -Force | Out-Null
New-Item -ItemType Directory -Path (Join-Path $OutputDir "logs") -Force | Out-Null

Copy-Item $jar (Join-Path $OutputDir "SDRERC-V2.jar") -Force
Copy-Item (Join-Path $scriptDir "run-sdrerc-client.bat") (Join-Path $OutputDir "run-sdrerc-client.bat") -Force
Copy-Item (Join-Path $scriptDir "install-sdrerc-client.ps1") (Join-Path $OutputDir "install-sdrerc-client.ps1") -Force
Copy-Item (Join-Path $scriptDir "uninstall-sdrerc-client.ps1") (Join-Path $OutputDir "uninstall-sdrerc-client.ps1") -Force
Copy-Item (Join-Path $scriptDir "README_CLIENTE_RED_LOCAL.md") (Join-Path $OutputDir "README_CLIENTE_RED_LOCAL.md") -Force
Copy-Item (Join-Path $scriptDir "sdrerc-client.properties.template") (Join-Path $OutputDir "config\sdrerc-client.properties.template") -Force

Write-Host "Paquete cliente generado en: $OutputDir"
Write-Host "Copie esa carpeta a la laptop cliente y ejecute install-sdrerc-client.ps1."
