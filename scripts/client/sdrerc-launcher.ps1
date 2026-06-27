param(
    [string]$ConfigPath
)

$ErrorActionPreference = "Stop"

function Resolve-FullPath {
    param([string]$PathValue)
    if ([string]::IsNullOrWhiteSpace($PathValue)) {
        return $null
    }
    return [System.IO.Path]::GetFullPath([Environment]::ExpandEnvironmentVariables($PathValue))
}

function Ensure-Directory {
    param([string]$PathValue)
    if (-not (Test-Path -LiteralPath $PathValue)) {
        New-Item -ItemType Directory -Path $PathValue -Force | Out-Null
    }
}

function Write-Log {
    param(
        [string]$Message,
        [string]$Level = "INFO"
    )
    $line = "{0} [{1}] {2}" -f (Get-Date -Format "yyyy-MM-dd HH:mm:ss"), $Level, $Message
    Write-Host $line
    if ($script:LogFile) {
        Add-Content -LiteralPath $script:LogFile -Value $line -Encoding UTF8
    }
}

function Read-JsonFile {
    param([string]$PathValue)
    if (-not (Test-Path -LiteralPath $PathValue)) {
        throw "No existe el archivo de configuracion: $PathValue"
    }
    return (Get-Content -LiteralPath $PathValue -Raw -Encoding UTF8 | ConvertFrom-Json)
}

function Read-JsonText {
    param([string]$JsonText)
    if ([string]::IsNullOrWhiteSpace($JsonText)) {
        throw "El contenido JSON remoto esta vacio."
    }
    return ($JsonText | ConvertFrom-Json)
}

function Compare-VersionText {
    param(
        [string]$Left,
        [string]$Right
    )
    if ([string]::IsNullOrWhiteSpace($Left) -and [string]::IsNullOrWhiteSpace($Right)) { return 0 }
    if ([string]::IsNullOrWhiteSpace($Left)) { return -1 }
    if ([string]::IsNullOrWhiteSpace($Right)) { return 1 }

    try {
        $leftParts = $Left.Split(".") | ForEach-Object { [int]($_ -replace "[^\d].*$", "") }
        $rightParts = $Right.Split(".") | ForEach-Object { [int]($_ -replace "[^\d].*$", "") }
        $max = [Math]::Max($leftParts.Count, $rightParts.Count)
        for ($i = 0; $i -lt $max; $i++) {
            $l = 0
            $r = 0
            if ($i -lt $leftParts.Count) { $l = $leftParts[$i] }
            if ($i -lt $rightParts.Count) { $r = $rightParts[$i] }
            if ($l -gt $r) { return 1 }
            if ($l -lt $r) { return -1 }
        }
        return 0
    }
    catch {
        return [string]::Compare($Left, $Right, $true)
    }
}

function Test-SdrercAlreadyOpen {
    param([string]$JarName)
    try {
        $currentPid = $PID
        $processes = Get-CimInstance Win32_Process -ErrorAction SilentlyContinue |
            Where-Object {
                $_.ProcessId -ne $currentPid -and
                $_.CommandLine -and
                $_.CommandLine.IndexOf($JarName, [StringComparison]::OrdinalIgnoreCase) -ge 0
            }
        return ($processes | Select-Object -First 1) -ne $null
    }
    catch {
        Write-Log "No se pudo verificar si SDRERC esta abierto: $($_.Exception.Message)" "WARN"
        return $false
    }
}

function Get-JavaExecutable {
    param($Config)
    $configuredJava = Resolve-FullPath $Config.javaPath
    if ($configuredJava -and (Test-Path -LiteralPath $configuredJava)) {
        return $configuredJava
    }

    $javaCommand = Get-Command java.exe -ErrorAction SilentlyContinue
    if ($javaCommand) {
        return $javaCommand.Source
    }

    throw "No se encontro Java. Configure javaPath en updater-config.json o instale Java 8+."
}

function Test-ZipFile {
    param([string]$ZipPath)
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    $zip = $null
    try {
        $zip = [System.IO.Compression.ZipFile]::OpenRead($ZipPath)
        return $true
    }
    finally {
        if ($zip) { $zip.Dispose() }
    }
}

function Invoke-HttpTextRequest {
    param([string]$Url)
    try {
        $response = Invoke-WebRequest -Uri $Url -UseBasicParsing -TimeoutSec 20
        return [string]$response.Content
    }
    catch {
        Write-Log "Invoke-WebRequest fallo para $Url: $($_.Exception.Message)" "WARN"
        $client = $null
        try {
            $client = New-Object System.Net.WebClient
            return $client.DownloadString($Url)
        }
        finally {
            if ($client) {
                $client.Dispose()
            }
        }
    }
}

function Download-HttpFile {
    param(
        [string]$Url,
        [string]$DestinationPath
    )
    try {
        Invoke-WebRequest -Uri $Url -OutFile $DestinationPath -UseBasicParsing -TimeoutSec 120
        return
    }
    catch {
        Write-Log "Invoke-WebRequest fallo al descargar $Url: $($_.Exception.Message)" "WARN"
        $client = $null
        try {
            $client = New-Object System.Net.WebClient
            $client.DownloadFile($Url, $DestinationPath)
        }
        finally {
            if ($client) {
                $client.Dispose()
            }
        }
    }
}

function Get-ExpectedChecksumFromText {
    param(
        [string]$ChecksumText,
        [string]$ZipFileName
    )
    if ([string]::IsNullOrWhiteSpace($ChecksumText)) {
        return $null
    }
    foreach ($line in ($ChecksumText -split "`r?`n")) {
        if ($line -match [Regex]::Escape($ZipFileName) -and $line -match "([A-Fa-f0-9]{64})") {
            return $matches[1].ToUpperInvariant()
        }
    }
    return $null
}

function Get-ExpectedChecksumFromFile {
    param(
        [string]$ChecksumFile,
        [string]$ZipFileName
    )
    if (-not (Test-Path -LiteralPath $ChecksumFile)) {
        return $null
    }
    return Get-ExpectedChecksumFromText -ChecksumText (Get-Content -LiteralPath $ChecksumFile -Raw -Encoding UTF8) -ZipFileName $ZipFileName
}

function Restore-Backup {
    param(
        [string]$AppDir,
        [string]$BackupDir
    )
    if (-not $BackupDir -or -not (Test-Path -LiteralPath $BackupDir)) {
        return
    }
    if (Test-Path -LiteralPath $AppDir) {
        Remove-Item -LiteralPath $AppDir -Recurse -Force
    }
    Move-Item -LiteralPath $BackupDir -Destination $AppDir -Force
}

function Get-ReleaseMode {
    param($Config)
    $mode = [string]$Config.remoteReleaseMode
    if ([string]::IsNullOrWhiteSpace($mode)) {
        if (-not [string]::IsNullOrWhiteSpace([string]$Config.remoteVersionUrl)) {
            return "HTTP"
        }
        return "FILE_SHARE"
    }
    return $mode.Trim().ToUpperInvariant()
}

function Get-RemoteReleaseMetadata {
    param($Config)
    $mode = Get-ReleaseMode -Config $Config
    $metadata = [ordered]@{
        Mode = $mode
        RemoteReleasePath = $null
        VersionSource = $null
        ZipSource = $null
        ChecksumSource = $null
        Version = $null
    }

    if ($mode -eq "HTTP" -or $mode -eq "HTTPS") {
        $versionUrl = [string]$Config.remoteVersionUrl
        $zipUrl = [string]$Config.remoteZipUrl
        $checksumsUrl = [string]$Config.remoteChecksumsUrl
        if ([string]::IsNullOrWhiteSpace($versionUrl)) {
            throw "remoteVersionUrl es obligatorio cuando remoteReleaseMode = HTTP."
        }
        if ([string]::IsNullOrWhiteSpace($zipUrl)) {
            throw "remoteZipUrl es obligatorio cuando remoteReleaseMode = HTTP."
        }
        $versionText = Invoke-HttpTextRequest -Url $versionUrl
        $metadata.Version = Read-JsonText -JsonText $versionText
        $metadata.VersionSource = $versionUrl
        $metadata.ZipSource = $zipUrl
        $metadata.ChecksumSource = $checksumsUrl
        return [pscustomobject]$metadata
    }

    $remoteReleasePath = [Environment]::ExpandEnvironmentVariables([string]$Config.remoteReleasePath)
    if ([string]::IsNullOrWhiteSpace($remoteReleasePath)) {
        throw "remoteReleasePath es obligatorio cuando remoteReleaseMode = FILE_SHARE."
    }
    $remoteVersionPath = Join-Path $remoteReleasePath "version.json"
    if (-not (Test-Path -LiteralPath $remoteVersionPath)) {
        throw "No se pudo acceder a version.json en la ruta remota: $remoteVersionPath"
    }
    $metadata.RemoteReleasePath = $remoteReleasePath
    $metadata.VersionSource = $remoteVersionPath
    $metadata.Version = Read-JsonFile -PathValue $remoteVersionPath
    $metadata.ChecksumSource = Join-Path $remoteReleasePath "checksums.txt"
    return [pscustomobject]$metadata
}

function Copy-RemoteZipToLocal {
    param(
        $RemoteMetadata,
        [string]$ZipFileName,
        [string]$DestinationPath
    )
    if ($RemoteMetadata.Mode -eq "HTTP" -or $RemoteMetadata.Mode -eq "HTTPS") {
        Write-Log "Descargando release remoto por HTTP: $($RemoteMetadata.ZipSource)"
        Download-HttpFile -Url $RemoteMetadata.ZipSource -DestinationPath $DestinationPath
        return
    }
    $remoteZip = Join-Path $RemoteMetadata.RemoteReleasePath $ZipFileName
    if (-not (Test-Path -LiteralPath $remoteZip)) {
        throw "No existe el paquete remoto: $remoteZip"
    }
    Write-Log "Copiando release remoto: $remoteZip"
    Copy-Item -LiteralPath $remoteZip -Destination $DestinationPath -Force
}

function Get-RemoteChecksum {
    param(
        $RemoteMetadata,
        [string]$ZipFileName
    )
    if ($RemoteMetadata.Mode -eq "HTTP" -or $RemoteMetadata.Mode -eq "HTTPS") {
        if ([string]::IsNullOrWhiteSpace([string]$RemoteMetadata.ChecksumSource)) {
            return $null
        }
        try {
            $checksumText = Invoke-HttpTextRequest -Url $RemoteMetadata.ChecksumSource
            return Get-ExpectedChecksumFromText -ChecksumText $checksumText -ZipFileName $ZipFileName
        }
        catch {
            Write-Log "No se pudo leer checksums remoto: $($_.Exception.Message)" "WARN"
            return $null
        }
    }
    return Get-ExpectedChecksumFromFile -ChecksumFile $RemoteMetadata.ChecksumSource -ZipFileName $ZipFileName
}

function Invoke-Update {
    param(
        $Config,
        $RemoteMetadata,
        [string]$LocalBasePath,
        [string]$AppDir,
        [string]$UpdatesDir,
        [string]$BackupRoot,
        [string]$LocalVersionPath
    )

    $remoteVersion = $RemoteMetadata.Version
    $zipFileName = $remoteVersion.zipFile
    if ([string]::IsNullOrWhiteSpace($zipFileName)) {
        throw "version.json remoto no define zipFile."
    }

    $timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
    $downloadZip = Join-Path $UpdatesDir ("{0}-{1}" -f $timestamp, $zipFileName)
    $stagingDir = Join-Path $UpdatesDir ("staging-{0}" -f $timestamp)
    $backupDir = Join-Path $BackupRoot ("app-{0}" -f $timestamp)
    $previousConfig = $null
    $configFile = Resolve-FullPath $Config.configFile

    Copy-RemoteZipToLocal -RemoteMetadata $RemoteMetadata -ZipFileName $zipFileName -DestinationPath $downloadZip

    $expectedChecksum = Get-RemoteChecksum -RemoteMetadata $RemoteMetadata -ZipFileName $zipFileName
    if ($expectedChecksum) {
        $actualChecksum = (Get-FileHash -LiteralPath $downloadZip -Algorithm SHA256).Hash.ToUpperInvariant()
        if ($actualChecksum -ne $expectedChecksum) {
            throw "Checksum invalido para $zipFileName. Esperado $expectedChecksum, obtenido $actualChecksum."
        }
        Write-Log "Checksum SHA256 validado."
    }
    else {
        Write-Log "No se encontro checksum para $zipFileName. Se continua validando estructura ZIP." "WARN"
    }

    [void](Test-ZipFile -ZipPath $downloadZip)
    Ensure-Directory $stagingDir
    Expand-Archive -LiteralPath $downloadZip -DestinationPath $stagingDir -Force

    if ($configFile -and (Test-Path -LiteralPath $configFile)) {
        $previousConfig = Join-Path $UpdatesDir ("sdrerc-app-{0}.properties" -f $timestamp)
        Copy-Item -LiteralPath $configFile -Destination $previousConfig -Force
    }

    try {
        if (Test-Path -LiteralPath $AppDir) {
            Ensure-Directory $BackupRoot
            Move-Item -LiteralPath $AppDir -Destination $backupDir -Force
            Write-Log "Backup creado: $backupDir"
        }

        Ensure-Directory $AppDir
        Copy-Item -Path (Join-Path $stagingDir "*") -Destination $AppDir -Recurse -Force

        if ($previousConfig) {
            Ensure-Directory (Split-Path -Parent $configFile)
            Copy-Item -LiteralPath $previousConfig -Destination $configFile -Force
            Write-Log "Configuracion local preservada: $configFile"
        }

        $remoteVersion | ConvertTo-Json -Depth 8 | Set-Content -LiteralPath $LocalVersionPath -Encoding UTF8
        Write-Log "Actualizacion aplicada. Version local: $($remoteVersion.version)"
    }
    catch {
        Write-Log "Fallo la actualizacion. Restaurando backup si existe. Error: $($_.Exception.Message)" "ERROR"
        Restore-Backup -AppDir $AppDir -BackupDir $backupDir
        throw
    }
    finally {
        if (Test-Path -LiteralPath $stagingDir) {
            Remove-Item -LiteralPath $stagingDir -Recurse -Force -ErrorAction SilentlyContinue
        }
    }
}

function Start-Sdrerc {
    param(
        $Config,
        [string]$AppDir
    )
    $java = Get-JavaExecutable -Config $Config
    $mainJarName = $Config.mainJar
    if ([string]::IsNullOrWhiteSpace($mainJarName)) {
        $mainJarName = "SDRERC-V2.jar"
    }
    $mainJar = Join-Path $AppDir $mainJarName
    if (-not (Test-Path -LiteralPath $mainJar)) {
        throw "No existe el JAR local: $mainJar"
    }

    $args = @()
    if ($Config.javaArgs) {
        foreach ($arg in $Config.javaArgs) {
            if (-not [string]::IsNullOrWhiteSpace([string]$arg)) {
                $args += [string]$arg
            }
        }
    }
    $args += "-jar"
    $args += $mainJar

    Write-Log "Iniciando SDRERC desde version local."
    Write-Log "Java: $java"
    Write-Log "JAR: $mainJar"
    $argumentText = ($args | ForEach-Object {
        $value = [string]$_
        if ($value -match "\s" -and -not ($value.StartsWith('"') -and $value.EndsWith('"'))) {
            '"' + $value.Replace('"', '\"') + '"'
        }
        else {
            $value
        }
    }) -join " "
    Start-Process -FilePath $java -ArgumentList $argumentText -WorkingDirectory $AppDir
}

try {
    if ([string]::IsNullOrWhiteSpace($ConfigPath)) {
        $ConfigPath = Join-Path (Split-Path -Parent $MyInvocation.MyCommand.Path) "updater-config.json"
    }

    $config = Read-JsonFile -PathValue $ConfigPath
    $localBasePath = Resolve-FullPath $config.localBasePath
    if (-not $localBasePath) {
        $localBasePath = "C:\SDRERC_CLIENTE"
    }

    $appDirectoryName = [string]$config.appDirectoryName
    if ([string]::IsNullOrWhiteSpace($appDirectoryName)) {
        $appDirectoryName = "app"
    }
    $appDir = Join-Path $localBasePath $appDirectoryName
    $updatesDir = Join-Path $localBasePath "updates"
    $backupRoot = Join-Path $localBasePath "backup"
    $logsDir = Join-Path $localBasePath "logs"
    $localVersionPath = Join-Path $appDir "version-local.json"
    $mainJarName = [string]$config.mainJar
    if ([string]::IsNullOrWhiteSpace($mainJarName)) {
        $mainJarName = "SDRERC-V2.jar"
    }

    Ensure-Directory $localBasePath
    Ensure-Directory $updatesDir
    Ensure-Directory $backupRoot
    Ensure-Directory $logsDir
    $script:LogFile = Join-Path $logsDir ("launcher-{0}.log" -f (Get-Date -Format "yyyyMMdd"))

    $releaseMode = Get-ReleaseMode -Config $config
    Write-Log "Launcher SDRERC iniciado."
    Write-Log "Base local: $localBasePath"
    Write-Log "Modo remoto: $releaseMode"
    if ($releaseMode -eq "HTTP" -or $releaseMode -eq "HTTPS") {
        Write-Log "Version URL: $($config.remoteVersionUrl)"
        Write-Log "ZIP URL: $($config.remoteZipUrl)"
    }
    else {
        Write-Log "Release remoto: $([string]$config.remoteReleasePath)"
    }

    if (Test-SdrercAlreadyOpen -JarName $mainJarName) {
        Write-Log "SDRERC ya esta abierto. No se aplicara actualizacion mientras la aplicacion este en uso." "WARN"
        Write-Host "SDRERC ya esta abierto. Cierre la aplicacion antes de actualizar."
        exit 2
    }

    $localVersion = $null
    if (Test-Path -LiteralPath $localVersionPath) {
        try {
            $localVersion = Read-JsonFile -PathValue $localVersionPath
        }
        catch {
            Write-Log "No se pudo leer version-local.json: $($_.Exception.Message)" "WARN"
        }
    }

    $remoteAvailable = $false
    $remoteMetadata = $null
    try {
        $remoteMetadata = Get-RemoteReleaseMetadata -Config $config
        $remoteAvailable = $true
        Write-Log "Version remota detectada: $($remoteMetadata.Version.version)"
    }
    catch {
        Write-Log "No se pudo acceder a la version remota: $($_.Exception.Message)" "WARN"
    }

    $localJar = Join-Path $appDir $mainJarName
    $needsUpdate = $false
    if ($remoteAvailable) {
        if (-not $localVersion -or -not (Test-Path -LiteralPath $localJar)) {
            $needsUpdate = $true
        }
        else {
            $cmp = Compare-VersionText -Left ([string]$remoteMetadata.Version.version) -Right ([string]$localVersion.version)
            $needsUpdate = $cmp -gt 0
        }
    }

    if ($needsUpdate) {
        Write-Log "Nueva version disponible: $($remoteMetadata.Version.version)"
        Invoke-Update -Config $config -RemoteMetadata $remoteMetadata -LocalBasePath $localBasePath -AppDir $appDir -UpdatesDir $updatesDir -BackupRoot $backupRoot -LocalVersionPath $localVersionPath
    }
    else {
        if ($remoteAvailable) {
            $localVersionText = if ($localVersion) { [string]$localVersion.version } else { "sin version local registrada" }
            Write-Log "No hay actualizacion pendiente. Version local: $localVersionText"
        }
        else {
            Write-Host "No hay conexion con el servidor de releases. Se abrira la ultima version local disponible."
        }
    }

    if (-not (Test-Path -LiteralPath $localJar)) {
        throw "No existe version local instalada y no fue posible descargar una version desde la red."
    }

    Start-Sdrerc -Config $config -AppDir $appDir
    Write-Log "Launcher finalizado."
    exit 0
}
catch {
    if ($script:LogFile) {
        Write-Log $_.Exception.Message "ERROR"
    }
    else {
        Write-Host $_.Exception.Message
    }
    exit 1
}
