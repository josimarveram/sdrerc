# Importa el certificado raiz exportado por diagnosticar-cert-smtp.ps1 al almacen
# "cacerts" del JRE indicado, para que Java confie en el mismo certificado que
# Windows ya acepta (necesario cuando un antivirus/proxy intercepta TLS).
#
# Debe ejecutarse como Administrador (escribe dentro de "C:\Program Files").
#
# Uso:
#   powershell -ExecutionPolicy Bypass -File importar-cert-jre.ps1 -RutaJre "C:\Program Files\Java\jre1.8.0_491"

param(
    [Parameter(Mandatory = $true)]
    [string]$RutaJre,

    [string]$Certificado = "$PSScriptRoot\smtp-root-cert.cer",

    [string]$Alias = "smtp-mail-shield-root"
)

$keytool = Join-Path $RutaJre "bin\keytool.exe"
$cacerts = Join-Path $RutaJre "lib\security\cacerts"

if (-not (Test-Path $keytool)) {
    Write-Host "No se encontro keytool en: $keytool" -ForegroundColor Red
    exit 1
}
if (-not (Test-Path $cacerts)) {
    Write-Host "No se encontro cacerts en: $cacerts" -ForegroundColor Red
    exit 1
}
if (-not (Test-Path $Certificado)) {
    Write-Host "No se encontro el certificado exportado: $Certificado" -ForegroundColor Red
    Write-Host "Ejecuta primero diagnosticar-cert-smtp.ps1" -ForegroundColor Yellow
    exit 1
}

& $keytool -importcert -keystore $cacerts -trustcacerts -alias $Alias -file $Certificado -storepass changeit -noprompt

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "Certificado importado correctamente en: $cacerts" -ForegroundColor Green
    Write-Host "Vuelve a intentar iniciar sesion en SDRERC." -ForegroundColor Green
} else {
    Write-Host "keytool devolvio un error al importar el certificado." -ForegroundColor Red
}
