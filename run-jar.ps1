$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $MyInvocation.MyCommand.Path

$preferredJavaCandidates = @(
    "C:\Program Files\JetBrains\IntelliJ IDEA 2026.1.2\jbr\bin\java.exe",
    "C:\Program Files\JetBrains\IntelliJ IDEA 2026.1\jbr\bin\java.exe"
)

$detectedJetBrainsJava = Get-ChildItem "C:\Program Files\JetBrains" -Directory -Filter "IntelliJ IDEA*" -ErrorAction SilentlyContinue |
    Sort-Object LastWriteTime -Descending |
    ForEach-Object { Join-Path $_.FullName "jbr\bin\java.exe" } |
    Where-Object { Test-Path $_ } |
    Select-Object -First 1

if ($detectedJetBrainsJava) {
    $preferredJavaCandidates = @($detectedJetBrainsJava) + $preferredJavaCandidates
}

$java = $preferredJavaCandidates | Where-Object { Test-Path $_ } | Select-Object -First 1

if (-not $java) {
    $java = "java"
}

$jar = Join-Path $root "target\UserManagementApp-1.0.0.jar"

if (-not (Test-Path $jar)) {
    Write-Error "No existe el JAR: $jar. Ejecuta primero: mvn clean package"
}

$previousErrorActionPreference = $ErrorActionPreference
$ErrorActionPreference = "Continue"

try {
    $javaVersionOutput = & $java -version 2>&1
}
finally {
    $ErrorActionPreference = $previousErrorActionPreference
}

$javaVersionLine = ($javaVersionOutput | Select-Object -First 1).ToString()

$javaMajorVersion = 0

if ($javaVersionLine -match '"(?<major>\d+)(?:\.(?<minor>\d+))?') {
    $javaMajorVersion = [int]$matches['major']

    if ($javaMajorVersion -eq 1 -and $matches['minor']) {
        $javaMajorVersion = [int]$matches['minor']
    }
}

$javaArgs = @(
    "-Dawt.useSystemAAFontSettings=lcd",
    "-Dswing.aatext=true"
)

if ($javaMajorVersion -ge 9) {
    $javaArgs += "-Dsun.java2d.dpiaware=true"
} else {
    $javaArgs += "-Dsun.java2d.dpiaware=false"
}

if ($javaMajorVersion -ge 22) {
    $javaArgs = @("--enable-native-access=ALL-UNNAMED") + $javaArgs
}

Write-Host "Iniciando SDRERC desde JAR..."
Write-Host "Root: $root"
Write-Host "Java: $java"
Write-Host "JAR:  $jar"

& $java @javaArgs -jar $jar
