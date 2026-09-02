# Levanta el backend (Spring Boot) y el frontend (Vite) de SDRERC V3 en modo desarrollo.
#
# Requisitos previos (una sola vez, ya hechos en esta maquina):
#   - JDK 17 (Temurin) instalado en "C:\Program Files\Eclipse Adoptium\jdk-17.0.20.8-hotspot"
#   - Variables de entorno de USUARIO ya configuradas (persisten tras reiniciar):
#       SDRERC_V3_DB_URL, SDRERC_V3_DB_USER, SDRERC_V3_DB_PASSWORD, SDRERC_V3_JWT_SECRET
#   - Certificado del interceptor TLS local ya importado en:
#       $env:USERPROFILE\.sdrerc-v3-trust\cacerts
#   - v3/sdrerc-v3-backend/target/sdrerc-v3-backend.jar ya compilado
#     (si hiciste cambios de codigo Java, hay que volver a compilar antes de correr este script:
#      ver comando de build al final de este archivo, comentado)
#
# SMTP (envio del OTP de 2FA por correo, 28/08/2026, bug real corregido):
#   config/sdrerc-app.properties esta commiteado con mail.smtp.user/mail.smtp.password EN BLANCO a
#   proposito (nunca se debe commitear una credencial real ahi, ver el propio comentario del
#   archivo) -- por eso, sin nada mas, el envio de OTP por correo siempre falla con "Debe configurar
#   mail.smtp.host, mail.smtp.user y mail.smtp.password...", no es un bug nuevo ni una credencial que
#   "se borro". Para que funcione de verdad, configura una sola vez (nunca se commitea, vive solo en
#   esta maquina) las variables de entorno de USUARIO reales:
#       [Environment]::SetEnvironmentVariable("SDRERC_APP_SMTP_USER", "<correo>", "User")
#       [Environment]::SetEnvironmentVariable("SDRERC_APP_SMTP_PASSWORD", "<clave de aplicacion>", "User")
#   Este script las prioriza sobre config/sdrerc-app.properties (antes las pisaba siempre con el
#   valor en blanco del archivo, aunque ya estuvieran configuradas -- ver Resolve-SmtpValue abajo).
#
# Uso:
#   powershell -ExecutionPolicy Bypass -File v3\start-dev.ps1

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $PSScriptRoot
if (-not (Test-Path (Join-Path $repoRoot "pom.xml"))) {
    $repoRoot = "D:\2026\FuentesRENIEC\sdrerc_CODIGOS"
}

Write-Output "=== SDRERC V3 - modo desarrollo ==="

# --- 1. Variables de entorno del backend ---
$configPath = Join-Path $repoRoot "config\sdrerc-app.properties"
$smtp = @{}
if (Test-Path $configPath) {
    Get-Content $configPath | ForEach-Object {
        if ($_ -match '^(mail\.smtp\.\w+)\s*=\s*(.*)$') {
            $smtp[$matches[1]] = $matches[2]
        }
    }
} else {
    Write-Output "AVISO: no se encontro $configPath - el envio de OTP por correo no funcionara."
}

$env:SDRERC_V3_DB_URL = [Environment]::GetEnvironmentVariable("SDRERC_V3_DB_URL", "User")
$env:SDRERC_V3_DB_USER = [Environment]::GetEnvironmentVariable("SDRERC_V3_DB_USER", "User")
$env:SDRERC_V3_DB_PASSWORD = [Environment]::GetEnvironmentVariable("SDRERC_V3_DB_PASSWORD", "User")
$env:SDRERC_V3_JWT_SECRET = [Environment]::GetEnvironmentVariable("SDRERC_V3_JWT_SECRET", "User")

# Bug real corregido (28/08/2026): antes esta seccion pisaba SIEMPRE las 5 variables SMTP con el
# valor (en blanco) de config/sdrerc-app.properties, incluso si ya habia una variable de entorno de
# USUARIO persistida con la credencial real -- el backend nunca llegaba a verla, porque
# Start-Process hereda el entorno de ESTE proceso powershell, y aqui ya habia quedado sobreescrita
# a "" antes de lanzar java. Ahora se prioriza la variable de entorno de usuario ya configurada
# (si existe) y solo se cae al archivo de properties cuando no hay ninguna -- mismo orden de
# prioridad que ya usa EmailOtpMailer.java en el backend (system property > env var > archivo).
function Resolve-SmtpValue {
    param([string]$EnvName, [string]$ValorDelArchivo)
    $persistido = [Environment]::GetEnvironmentVariable($EnvName, "User")
    if ($persistido) { return $persistido }
    return $ValorDelArchivo
}

$env:SDRERC_APP_SMTP_HOST = Resolve-SmtpValue "SDRERC_APP_SMTP_HOST" $smtp['mail.smtp.host']
$env:SDRERC_APP_SMTP_PORT = Resolve-SmtpValue "SDRERC_APP_SMTP_PORT" $smtp['mail.smtp.port']
$env:SDRERC_APP_SMTP_USER = Resolve-SmtpValue "SDRERC_APP_SMTP_USER" $smtp['mail.smtp.user']
$env:SDRERC_APP_SMTP_PASSWORD = Resolve-SmtpValue "SDRERC_APP_SMTP_PASSWORD" $smtp['mail.smtp.password']
$env:SDRERC_APP_SMTP_FROM = Resolve-SmtpValue "SDRERC_APP_SMTP_FROM" $smtp['mail.smtp.from']

if (-not $env:SDRERC_APP_SMTP_USER -or -not $env:SDRERC_APP_SMTP_PASSWORD) {
    Write-Output "AVISO: SDRERC_APP_SMTP_USER/SDRERC_APP_SMTP_PASSWORD no estan configurados (ni como"
    Write-Output "variable de entorno de usuario ni en config/sdrerc-app.properties) - el envio de"
    Write-Output "OTP por correo fallara con 'Debe configurar mail.smtp.host...'. Para habilitarlo:"
    Write-Output '  [Environment]::SetEnvironmentVariable("SDRERC_APP_SMTP_USER", "<correo>", "User")'
    Write-Output '  [Environment]::SetEnvironmentVariable("SDRERC_APP_SMTP_PASSWORD", "<clave app>", "User")'
}

if (-not $env:SDRERC_V3_DB_URL -or -not $env:SDRERC_V3_DB_PASSWORD -or -not $env:SDRERC_V3_JWT_SECRET) {
    Write-Output "ERROR: faltan variables de entorno de usuario (SDRERC_V3_DB_URL / SDRERC_V3_DB_PASSWORD / SDRERC_V3_JWT_SECRET)."
    Write-Output "Configuralas con [Environment]::SetEnvironmentVariable(..., 'User') antes de reintentar."
    exit 1
}

# --- 2. Backend ---
$javaExe = "C:\Program Files\Eclipse Adoptium\jdk-17.0.20.8-hotspot\bin\java.exe"
$jar = Join-Path $repoRoot "v3\sdrerc-v3-backend\target\sdrerc-v3-backend.jar"
$trustStore = Join-Path $env:USERPROFILE ".sdrerc-v3-trust\cacerts"
$backendLog = Join-Path $env:USERPROFILE "sdrerc-v3-backend.log"

if (-not (Test-Path $jar)) {
    Write-Output "ERROR: no existe $jar - compila primero con mvn clean package (ver comentario al final de este script)."
    exit 1
}

$backendArgs = @("-Djavax.net.ssl.trustStore=$trustStore", "-Djavax.net.ssl.trustStorePassword=changeit", "-jar", $jar)
$backendProc = Start-Process -FilePath $javaExe -ArgumentList $backendArgs -WorkingDirectory $repoRoot `
    -RedirectStandardOutput $backendLog -RedirectStandardError "$backendLog.err" -PassThru -WindowStyle Hidden
Write-Output "Backend iniciado (PID $($backendProc.Id)), log en $backendLog"

# --- 3. Frontend ---
$frontendDir = Join-Path $repoRoot "v3\sdrerc-v3-frontend"
$frontendProc = Start-Process -FilePath "npm.cmd" -ArgumentList "run", "dev" -WorkingDirectory $frontendDir -PassThru -WindowStyle Hidden
Write-Output "Frontend iniciado (PID $($frontendProc.Id))"

Write-Output ""
Write-Output "Esperando a que el backend levante (puede tardar ~10 segundos)..."
Start-Sleep -Seconds 10
try {
    $health = Invoke-RestMethod -Uri "http://localhost:8081/actuator/health" -TimeoutSec 5
    Write-Output "Backend: $($health.status)"
} catch {
    Write-Output "Backend todavia no responde - revisa $backendLog.err si tarda mas de 20-30 segundos."
}

Write-Output ""
Write-Output "Listo. Abre: http://localhost:5173"
Write-Output "Para detener ambos procesos: Get-CimInstance Win32_Process -Filter \"Name='java.exe' or Name='node.exe'\" | Where-Object CommandLine -match 'sdrerc' | Stop-Process -Force"

# Para recompilar el backend despues de cambios de codigo Java (antes de correr este script):
#   $env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.20.8-hotspot"
#   $env:MAVEN_OPTS = "-Djavax.net.ssl.trustStore=$env:USERPROFILE\.sdrerc-v3-trust\cacerts -Djavax.net.ssl.trustStorePassword=changeit"
#   mvn -f v3\sdrerc-v3-backend\pom.xml clean package -DskipTests
