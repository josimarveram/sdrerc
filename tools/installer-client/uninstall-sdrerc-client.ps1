param(
    [string]$InstallDir = "C:\SDRERC_CLIENTE",
    [switch]$Force
)

$ErrorActionPreference = "Stop"

if (-not $Force) {
    $answer = Read-Host "Se eliminara $InstallDir y el acceso directo SDRERC Cliente. Escriba SI para continuar"
    if ($answer -ne "SI") {
        Write-Host "Desinstalacion cancelada."
        exit 0
    }
}

$desktop = [Environment]::GetFolderPath("Desktop")
$shortcutPath = Join-Path $desktop "SDRERC Cliente.lnk"
if (Test-Path $shortcutPath) {
    Remove-Item $shortcutPath -Force
}

if (Test-Path $InstallDir) {
    Remove-Item $InstallDir -Recurse -Force
}

Write-Host "SDRERC Cliente fue desinstalado."
