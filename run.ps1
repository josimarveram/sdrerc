$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $MyInvocation.MyCommand.Path

$jars = @(
    (Join-Path $root "target\classes"),
    (Join-Path $env:USERPROFILE ".m2\repository\com\formdev\flatlaf\3.6.2\flatlaf-3.6.2.jar"),
    (Join-Path $env:USERPROFILE ".m2\repository\com\formdev\flatlaf-intellij-themes\3.6.2\flatlaf-intellij-themes-3.6.2.jar"),
    (Join-Path $env:USERPROFILE ".m2\repository\com\toedter\jcalendar\1.4\jcalendar-1.4.jar"),
    (Join-Path $env:USERPROFILE ".m2\repository\org\netbeans\external\AbsoluteLayout\RELEASE250\AbsoluteLayout-RELEASE250.jar"),
    (Join-Path $env:USERPROFILE ".m2\repository\org\mindrot\jbcrypt\0.4\jbcrypt-0.4.jar"),
    (Join-Path $env:USERPROFILE ".m2\repository\com\oracle\database\jdbc\ojdbc11\23.2.0.0\ojdbc11-23.2.0.0.jar")
)

$missing = $jars | Where-Object { -not (Test-Path $_) }
if ($missing) {
    Write-Error "Faltan dependencias o clases compiladas:`n$($missing -join "`n")"
}

$preferredJavaw = "C:\Program Files\JetBrains\IntelliJ IDEA 2026.1\jbr\bin\javaw.exe"
$javaw = if (Test-Path $preferredJavaw) { $preferredJavaw } else { "javaw" }

$classpath = $jars -join ";"
Start-Process $javaw -WorkingDirectory $root -ArgumentList "-cp", $classpath, "com.sdrerc.ui.login.FrmLogin"

Write-Host "SDRERC iniciado."
