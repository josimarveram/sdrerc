param(
    [Parameter(Mandatory = $true)]
    [string]$ServerIp,

    [string]$Port = "1521",
    [string]$ServiceName = "XEPDB1",
    [string]$DbUser = "SDRERC_APP",
    [string]$DbPassword = "",
    [string]$ConfigPath = "config\sdrerc-app.properties",
    [switch]$SkipConnectivityTest
)

$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
$targetConfig = if ([System.IO.Path]::IsPathRooted($ConfigPath)) {
    $ConfigPath
}
else {
    Join-Path $root $ConfigPath
}

function Read-Properties {
    param([string]$Path)
    $values = @{}
    if (-not (Test-Path -LiteralPath $Path)) {
        return $values
    }

    Get-Content -LiteralPath $Path | ForEach-Object {
        $line = $_.Trim()
        if ($line.Length -eq 0 -or $line.StartsWith("#")) {
            return
        }
        $idx = $line.IndexOf("=")
        if ($idx -le 0) {
            return
        }
        $key = $line.Substring(0, $idx).Trim()
        $value = $line.Substring($idx + 1).Trim()
        $values[$key] = $value
    }
    return $values
}

if (-not $SkipConnectivityTest) {
    Write-Host "Probando conectividad a $ServerIp`:$Port..."
    $test = Test-NetConnection $ServerIp -Port ([int]$Port) -WarningAction SilentlyContinue
    if (-not $test.TcpTestSucceeded) {
        Write-Warning "No responde $ServerIp`:$Port. Revise IP, red, listener Oracle o firewall antes de abrir SDRERC."
    }
    else {
        Write-Host "Puerto Oracle disponible."
    }
}

$existing = Read-Properties -Path $targetConfig
$passwordToWrite = if ($DbPassword -ne "") {
    $DbPassword
}
elseif ($existing.ContainsKey("db.password")) {
    $existing["db.password"]
}
elseif ($existing.ContainsKey("sdrerc.db.password")) {
    $existing["sdrerc.db.password"]
}
else {
    ""
}

$url = "jdbc:oracle:thin:@//$ServerIp`:$Port/$ServiceName"
$dir = Split-Path -Parent $targetConfig
if (-not (Test-Path -LiteralPath $dir)) {
    New-Item -ItemType Directory -Path $dir | Out-Null
}

$content = @(
    "db.url=$url",
    "db.user=$DbUser",
    "db.password=$passwordToWrite",
    "app.mode=LAN",
    "app.name=SDRERC"
)

Set-Content -LiteralPath $targetConfig -Value $content -Encoding UTF8

Write-Host "Configuracion actualizada: $targetConfig"
Write-Host "URL: $url"
Write-Host "Usuario: $DbUser"
if ($passwordToWrite -eq "") {
    Write-Warning "El password quedo vacio. Complete db.password en el archivo local si corresponde."
}
else {
    Write-Host "Password: preservado/configurado localmente."
}
