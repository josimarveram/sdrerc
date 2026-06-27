param(
    [string]$InstallDir = "C:\SDRERC_CLIENTE",
    [string]$RemoteReleaseMode = "FILE_SHARE",
    [string]$RemoteReleasePath = "",
    [string]$RemoteVersionUrl = "",
    [string]$RemoteZipUrl = "",
    [string]$RemoteChecksumsUrl = "",
    [string]$JavaPath = ""
)

$ErrorActionPreference = "Stop"

function Ensure-Directory {
    param([string]$PathValue)
    if (-not (Test-Path -LiteralPath $PathValue)) {
        New-Item -ItemType Directory -Path $PathValue -Force | Out-Null
    }
}

function Backup-IfExists {
    param([string]$PathValue)
    if (Test-Path -LiteralPath $PathValue) {
        $backup = "{0}.bak-{1}" -f $PathValue, (Get-Date -Format "yyyyMMdd-HHmmss")
        Copy-Item -LiteralPath $PathValue -Destination $backup -Force
        Write-Host "Backup creado: $backup"
    }
}

$installerDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$launcherDir = Join-Path $InstallDir "launcher"
$appDir = Join-Path $InstallDir "app"
$configDir = Join-Path $appDir "config"
$updatesDir = Join-Path $InstallDir "updates"
$backupDir = Join-Path $InstallDir "backup"
$logsDir = Join-Path $InstallDir "logs"

Ensure-Directory $InstallDir
Ensure-Directory $launcherDir
Ensure-Directory $appDir
Ensure-Directory $configDir
Ensure-Directory $updatesDir
Ensure-Directory $backupDir
Ensure-Directory $logsDir

Copy-Item -LiteralPath (Join-Path $installerDir "sdrerc-launcher.ps1") -Destination (Join-Path $launcherDir "sdrerc-launcher.ps1") -Force
Copy-Item -LiteralPath (Join-Path $installerDir "run-sdrerc-client.bat") -Destination (Join-Path $InstallDir "run-sdrerc-client.bat") -Force

$configSource = Join-Path $installerDir "updater-config.json.example"
$configTarget = Join-Path $launcherDir "updater-config.json"
if (Test-Path -LiteralPath $configTarget) {
    Backup-IfExists -PathValue $configTarget
}
Copy-Item -LiteralPath $configSource -Destination $configTarget -Force

if (-not [string]::IsNullOrWhiteSpace($RemoteReleasePath)
    -or -not [string]::IsNullOrWhiteSpace($RemoteVersionUrl)
    -or -not [string]::IsNullOrWhiteSpace($RemoteZipUrl)
    -or -not [string]::IsNullOrWhiteSpace($RemoteChecksumsUrl)
    -or -not [string]::IsNullOrWhiteSpace($JavaPath)
    -or -not [string]::IsNullOrWhiteSpace($RemoteReleaseMode)) {
    $json = Get-Content -LiteralPath $configTarget -Raw -Encoding UTF8 | ConvertFrom-Json
    if (-not [string]::IsNullOrWhiteSpace($RemoteReleaseMode)) {
        $json.remoteReleaseMode = $RemoteReleaseMode
    }
    if (-not [string]::IsNullOrWhiteSpace($RemoteReleasePath)) {
        $json.remoteReleasePath = $RemoteReleasePath
    }
    if (-not [string]::IsNullOrWhiteSpace($RemoteVersionUrl)) {
        $json.remoteVersionUrl = $RemoteVersionUrl
    }
    if (-not [string]::IsNullOrWhiteSpace($RemoteZipUrl)) {
        $json.remoteZipUrl = $RemoteZipUrl
    }
    if (-not [string]::IsNullOrWhiteSpace($RemoteChecksumsUrl)) {
        $json.remoteChecksumsUrl = $RemoteChecksumsUrl
    }
    if (-not [string]::IsNullOrWhiteSpace($JavaPath)) {
        $json.javaPath = $JavaPath
    }
    $json.localBasePath = $InstallDir
    $json.configFile = (Join-Path $configDir "sdrerc-app.properties")
    $json.javaArgs = @(
        "-Dsdrerc.app.config=$($json.configFile)",
        "-Dawt.useSystemAAFontSettings=lcd",
        "-Dswing.aatext=true",
        "-Dsun.java2d.dpiaware=true"
    )
    $json | ConvertTo-Json -Depth 8 | Set-Content -LiteralPath $configTarget -Encoding UTF8
}

$appConfigExample = Join-Path $configDir "sdrerc-app.properties.example"
if (-not (Test-Path -LiteralPath $appConfigExample)) {
    @"
# Configuracion local del cliente SDRERC V2.
# Complete la URL, usuario y password en la laptop cliente.
sdrerc.db.url=jdbc:oracle:thin:@//IP_SERVIDOR:1521/XEPDB1
sdrerc.db.user=SDRERC_APP
sdrerc.db.password=
"@ | Set-Content -LiteralPath $appConfigExample -Encoding UTF8
}

$appConfig = Join-Path $configDir "sdrerc-app.properties"
if (-not (Test-Path -LiteralPath $appConfig)) {
    Copy-Item -LiteralPath $appConfigExample -Destination $appConfig -Force
}

$desktop = [Environment]::GetFolderPath("Desktop")
$shortcutPath = Join-Path $desktop "SDRERC Cliente.lnk"
$shell = New-Object -ComObject WScript.Shell
$shortcut = $shell.CreateShortcut($shortcutPath)
$shortcut.TargetPath = Join-Path $InstallDir "run-sdrerc-client.bat"
$shortcut.WorkingDirectory = $InstallDir
$shortcut.Description = "SDRERC Cliente"
$shortcut.Save()

Write-Host "Instalacion cliente SDRERC completada."
Write-Host "Directorio: $InstallDir"
Write-Host "Configure:   $configTarget"
Write-Host "DB config:   $appConfigExample"
Write-Host "Acceso directo creado: $shortcutPath"
Write-Host "Primera ejecucion: $($shortcut.TargetPath)"
