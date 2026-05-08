$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $MyInvocation.MyCommand.Path

$jars = @(
    (Join-Path $root "target\classes"),

    # FlatLaf / UI
    (Join-Path $env:USERPROFILE ".m2\repository\com\formdev\flatlaf\3.6.2\flatlaf-3.6.2.jar"),
    (Join-Path $env:USERPROFILE ".m2\repository\com\formdev\flatlaf-intellij-themes\3.6.2\flatlaf-intellij-themes-3.6.2.jar"),
    (Join-Path $env:USERPROFILE ".m2\repository\com\formdev\flatlaf-extras\3.6.2\flatlaf-extras-3.6.2.jar"),
    (Join-Path $env:USERPROFILE ".m2\repository\com\github\weisj\jsvg\2.0.0\jsvg-2.0.0.jar"),

    # Swing / NetBeans
    (Join-Path $env:USERPROFILE ".m2\repository\com\toedter\jcalendar\1.4\jcalendar-1.4.jar"),
    (Join-Path $env:USERPROFILE ".m2\repository\org\netbeans\external\AbsoluteLayout\RELEASE250\AbsoluteLayout-RELEASE250.jar"),

    # Seguridad / DB
    (Join-Path $env:USERPROFILE ".m2\repository\org\mindrot\jbcrypt\0.4\jbcrypt-0.4.jar"),
    (Join-Path $env:USERPROFILE ".m2\repository\com\oracle\database\jdbc\ojdbc11\23.2.0.0\ojdbc11-23.2.0.0.jar"),

    # Apache POI para DOCX/XWPFDocument
    (Join-Path $env:USERPROFILE ".m2\repository\org\apache\poi\poi\5.2.5\poi-5.2.5.jar"),
    (Join-Path $env:USERPROFILE ".m2\repository\org\apache\poi\poi-ooxml\5.2.5\poi-ooxml-5.2.5.jar"),
    (Join-Path $env:USERPROFILE ".m2\repository\org\apache\poi\poi-ooxml-lite\5.2.5\poi-ooxml-lite-5.2.5.jar"),

    # Dependencias de Apache POI
    (Join-Path $env:USERPROFILE ".m2\repository\org\apache\xmlbeans\xmlbeans\5.2.0\xmlbeans-5.2.0.jar"),
    (Join-Path $env:USERPROFILE ".m2\repository\org\apache\commons\commons-compress\1.25.0\commons-compress-1.25.0.jar"),
    (Join-Path $env:USERPROFILE ".m2\repository\commons-io\commons-io\2.15.0\commons-io-2.15.0.jar"),
    (Join-Path $env:USERPROFILE ".m2\repository\org\apache\commons\commons-collections4\4.4\commons-collections4-4.4.jar"),
    (Join-Path $env:USERPROFILE ".m2\repository\com\github\virtuald\curvesapi\1.08\curvesapi-1.08.jar"),

    # Log4j 2 (requerido por Apache POI)
    (Join-Path $env:USERPROFILE ".m2\repository\org\apache\logging\log4j\log4j-api\2.20.0\log4j-api-2.20.0.jar"),
    (Join-Path $env:USERPROFILE ".m2\repository\org\apache\logging\log4j\log4j-core\2.20.0\log4j-core-2.20.0.jar")
)

$missing = $jars | Where-Object { -not (Test-Path $_) }
if ($missing) {
    Write-Error "Faltan dependencias o clases compiladas:`n$($missing -join "`n")"
}

$preferredJava = "C:\Program Files\JetBrains\IntelliJ IDEA 2026.1\jbr\bin\java.exe"
$java = if (Test-Path $preferredJava) { $preferredJava } else { "java" }

$classpath = $jars -join ";"

$javaVersionOutput = & $java -version 2>&1
$javaVersionLine = ($javaVersionOutput | Select-Object -First 1).ToString()
$javaMajorVersion = 0
if ($javaVersionLine -match '"(?<major>\d+)(?:\.(?<minor>\d+))?') {
    $javaMajorVersion = [int]$matches['major']
    if ($javaMajorVersion -eq 1 -and $matches['minor']) {
        $javaMajorVersion = [int]$matches['minor']
    }
}

$javaArgs = @()
if ($javaMajorVersion -ge 22) {
    $javaArgs += "--enable-native-access=ALL-UNNAMED"
}

Write-Host "Iniciando SDRERC en modo consola..."
Write-Host "Root: $root"
Write-Host "Java: $java"

& $java @javaArgs -cp $classpath com.sdrerc.ui.login.FrmLogin
