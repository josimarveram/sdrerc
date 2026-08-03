param(
    [string]$TailscaleSubnet = "100.64.0.0/10",
    [int]$OraclePort = 1521,
    [int]$SmbPort = 445
)

# Abre en Windows Firewall (solo para este servidor SDRERC) el puerto de Oracle y el de
# SMB (compartido de archivos del release), restringido UNICAMENTE a la subred CGNAT de
# Tailscale (100.64.0.0/10). No toca ni reemplaza ninguna regla existente para la LAN local:
# agrega 2 reglas nuevas y separadas, identificadas por nombre, para que un cliente remoto
# conectado por Tailscale pueda llegar al listener de Oracle y al recurso compartido de
# releases sin exponer ningun puerto a internet.
#
# Requiere ejecutarse en una consola de PowerShell "Ejecutar como administrador".
#
# Uso:
#   .\scripts\server\configurar-firewall-tailscale.ps1

$ErrorActionPreference = "Stop"

$isAdmin = ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
if (-not $isAdmin) {
    Write-Host "Este script requiere una consola de PowerShell 'Ejecutar como administrador'." -ForegroundColor Red
    exit 1
}

function Add-TailscaleRuleIfMissing {
    param(
        [string]$DisplayName,
        [int]$Port
    )
    $existing = Get-NetFirewallRule -DisplayName $DisplayName -ErrorAction SilentlyContinue
    if ($existing) {
        Write-Host "Ya existe la regla '$DisplayName'. No se modifica." -ForegroundColor Yellow
        return
    }
    New-NetFirewallRule -DisplayName $DisplayName `
        -Direction Inbound `
        -Protocol TCP `
        -LocalPort $Port `
        -RemoteAddress $TailscaleSubnet `
        -Action Allow `
        -Profile Any | Out-Null
    Write-Host "Regla creada: '$DisplayName' (TCP $Port, origen $TailscaleSubnet)." -ForegroundColor Green
}

Add-TailscaleRuleIfMissing -DisplayName "SDRERC Oracle 1521 (Tailscale)" -Port $OraclePort
Add-TailscaleRuleIfMissing -DisplayName "SDRERC SMB 445 (Tailscale)" -Port $SmbPort

Write-Host ""
Write-Host "Listo. Estas reglas solo permiten trafico entrante desde la subred de Tailscale ($TailscaleSubnet)." -ForegroundColor Cyan
Write-Host "Las reglas existentes de tu LAN local no fueron tocadas." -ForegroundColor Cyan
