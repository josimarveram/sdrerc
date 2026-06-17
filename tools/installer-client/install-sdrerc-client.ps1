param(
    [string]$ServerIp,
    [string]$Port = "1521",
    [string]$ServiceName = "XEPDB1",
    [string]$DbUser = "SDRERC_APP",
    [string]$DbPassword = "",
    [string]$InstallDir = "C:\SDRERC_CLIENTE",
    [switch]$SkipConnectivityTest
)

$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$sourceJar = Join-Path $scriptDir "SDRERC-V2.jar"
$sourceRun = Join-Path $scriptDir "run-sdrerc-client.bat"
$sourceUninstall = Join-Path $scriptDir "uninstall-sdrerc-client.ps1"
$sourceReadme = Join-Path $scriptDir "README_CLIENTE_RED_LOCAL.md"

if ([string]::IsNullOrWhiteSpace($ServerIp)) {
    $ServerIp = Read-Host "Ingrese la IP de la laptop servidor"
}
if ([string]::IsNullOrWhiteSpace($ServerIp)) {
    throw "Debe indicar la IP del servidor Oracle."
}

if (-not (Test-Path $sourceJar)) {
    throw "No se encontro SDRERC-V2.jar en $scriptDir. Genere dist con build-sdrerc-client-dist.ps1."
}
if (-not (Test-Path $sourceRun)) {
    throw "No se encontro run-sdrerc-client.bat en $scriptDir."
}

$java = Get-Command java -ErrorAction SilentlyContinue
if (-not $java) {
    Write-Warning "No se encontro Java en PATH. Instale Java 8 o una version compatible en la laptop cliente."
}

if (-not $SkipConnectivityTest) {
    $testNet = Get-Command Test-NetConnection -ErrorAction SilentlyContinue
    if ($testNet) {
        Write-Host "Probando conectividad hacia ${ServerIp}:${Port}..."
        $result = Test-NetConnection -ComputerName $ServerIp -Port ([int]$Port) -WarningAction SilentlyContinue
        if (-not $result.TcpTestSucceeded) {
            Write-Warning "No responde el puerto $Port en $ServerIp. Revise red, firewall, listener Oracle y hotspot."
        }
    }
}

$configDir = Join-Path $InstallDir "config"
$libDir = Join-Path $InstallDir "lib"
$logsDir = Join-Path $InstallDir "logs"
New-Item -ItemType Directory -Path $InstallDir, $configDir, $libDir, $logsDir -Force | Out-Null

Copy-Item $sourceJar (Join-Path $InstallDir "SDRERC-V2.jar") -Force
Copy-Item $sourceRun (Join-Path $InstallDir "run-sdrerc-client.bat") -Force
if (Test-Path $sourceUninstall) {
    Copy-Item $sourceUninstall (Join-Path $InstallDir "uninstall-sdrerc-client.ps1") -Force
}
if (Test-Path $sourceReadme) {
    Copy-Item $sourceReadme (Join-Path $InstallDir "README_CLIENTE_RED_LOCAL.md") -Force
}

$sourceLibDir = Join-Path $scriptDir "lib"
if (Test-Path $sourceLibDir) {
    Copy-Item (Join-Path $sourceLibDir "*") $libDir -Recurse -Force -ErrorAction SilentlyContinue
}

$url = "jdbc:oracle:thin:@//$ServerIp`:$Port/$ServiceName"
$configFile = Join-Path $configDir "sdrerc-client.properties"
$configContent = @(
    "sdrerc.db.url=$url",
    "sdrerc.db.user=$DbUser",
    "sdrerc.db.password=$DbPassword",
    "app.mode=CLIENTE_RED_LOCAL",
    "app.name=SDRERC"
)
Set-Content -Path $configFile -Value $configContent -Encoding ASCII

if ([string]::IsNullOrWhiteSpace($DbPassword)) {
    Write-Warning "El password quedo vacio en $configFile. Complete sdrerc.db.password antes de iniciar si corresponde."
}

$desktop = [Environment]::GetFolderPath("Desktop")
$shortcutPath = Join-Path $desktop "SDRERC Cliente.lnk"
$shell = New-Object -ComObject WScript.Shell
$shortcut = $shell.CreateShortcut($shortcutPath)
$shortcut.TargetPath = Join-Path $InstallDir "run-sdrerc-client.bat"
$shortcut.WorkingDirectory = $InstallDir
$shortcut.Description = "SDRERC Cliente"
$shortcut.Save()

Write-Host ""
Write-Host "Instalacion completada en $InstallDir"
Write-Host "Configuracion: $configFile"
Write-Host "Acceso directo: $shortcutPath"
Write-Host ""
Write-Host "Para ejecutar: $InstallDir\run-sdrerc-client.bat"
