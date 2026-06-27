# SDRERC V2 - Actualizacion automatica cliente-servidor

Esta guia describe el esquema cliente-servidor para distribuir SDRERC V2 en laptops Windows, usando:

- `FILE_SHARE` por carpeta compartida SMB/UNC
- `HTTP` o `HTTPS` para acceso remoto por VPN

## Arquitectura

Servidor:

```text
D:\SDRERC_RELEASES
  latest
    version.json
    SDRERC-V2.zip
    checksums.txt
  versions
    1.0.1
      version.json
      SDRERC-V2.zip
      checksums.txt
```

Cliente:

```text
C:\SDRERC_CLIENTE
  run-sdrerc-client.bat
  launcher
    sdrerc-launcher.ps1
    updater-config.json
  app
    SDRERC-V2.jar
    config
      sdrerc-app.properties
    version-local.json
  updates
  backup
  logs
```

El cliente nunca ejecuta el JAR desde la ruta remota. Siempre descarga o copia la release a `C:\SDRERC_CLIENTE\app` y ejecuta la version local.

## Publicacion desde servidor

Desde la raiz del proyecto:

```powershell
.\scripts\server\publish-sdrerc-release.ps1 -Version "1.0.12"
```

El script:

- ejecuta `mvn clean compile`
- ejecuta `mvn clean package`
- genera `SDRERC-V2.zip`
- genera `version.json`
- genera `checksums.txt`
- publica en `D:\SDRERC_RELEASES\latest`

Verificacion rapida:

```powershell
Get-Content D:\SDRERC_RELEASES\latest\version.json
Get-Content D:\SDRERC_RELEASES\latest\checksums.txt
```

## Modo 1: FILE_SHARE / UNC

### Servidor

Comparta:

```text
D:\SDRERC_RELEASES
```

por ejemplo como:

```text
\\NOMBRE_SERVIDOR\SDRERC_RELEASES
```

Permisos recomendados:

- clientes: lectura
- responsable de despliegue: lectura/escritura

### Cliente

`updater-config.json`:

```json
{
  "remoteReleaseMode": "FILE_SHARE",
  "remoteReleasePath": "\\\\DESKTOP-TEOGE91\\SDRERC_RELEASES\\latest",
  "remoteVersionUrl": "",
  "remoteZipUrl": "",
  "remoteChecksumsUrl": "",
  "localBasePath": "C:\\SDRERC_CLIENTE",
  "javaPath": "C:\\Program Files\\Java\\jre1.8.0_491\\bin\\java.exe",
  "mainJar": "SDRERC-V2.jar",
  "appDirectoryName": "app",
  "configFile": "C:\\SDRERC_CLIENTE\\app\\config\\sdrerc-app.properties",
  "javaArgs": [
    "-Dsdrerc.app.config=C:\\SDRERC_CLIENTE\\app\\config\\sdrerc-app.properties"
  ]
}
```

Prueba de conectividad:

```powershell
Test-NetConnection NOMBRE_SERVIDOR -Port 445
Test-Path "\\NOMBRE_SERVIDOR\SDRERC_RELEASES\latest\version.json"
```

## Modo 2: HTTP / HTTPS por VPN

Este modo sirve para clientes remotos conectados por VPN, por ejemplo Tailscale o ZeroTier.

### Servidor

Publique la release como siempre:

```powershell
.\scripts\server\publish-sdrerc-release.ps1 -Version "1.0.12"
```

Luego exponga la carpeta `latest` por HTTP solo dentro de la red VPN.

Ejemplo rapido con Python:

```powershell
cd D:\SDRERC_RELEASES\latest
python -m http.server 8088
```

URLs esperadas:

```text
http://IP_VPN_SERVIDOR:8088/version.json
http://IP_VPN_SERVIDOR:8088/SDRERC-V2.zip
http://IP_VPN_SERVIDOR:8088/checksums.txt
```

### Firewall del servidor

Permitir Oracle y releases HTTP solo en la red privada/VPN:

```powershell
New-NetFirewallRule -DisplayName "SDRERC Oracle 1521 VPN" -Direction Inbound -Protocol TCP -LocalPort 1521 -Action Allow
New-NetFirewallRule -DisplayName "SDRERC Releases HTTP 8088 VPN" -Direction Inbound -Protocol TCP -LocalPort 8088 -Action Allow
```

### Cliente

`updater-config.json`:

```json
{
  "remoteReleaseMode": "HTTP",
  "remoteReleasePath": "",
  "remoteVersionUrl": "http://IP_VPN_SERVIDOR:8088/version.json",
  "remoteZipUrl": "http://IP_VPN_SERVIDOR:8088/SDRERC-V2.zip",
  "remoteChecksumsUrl": "http://IP_VPN_SERVIDOR:8088/checksums.txt",
  "localBasePath": "C:\\SDRERC_CLIENTE",
  "javaPath": "C:\\Program Files\\Java\\jre1.8.0_491\\bin\\java.exe",
  "mainJar": "SDRERC-V2.jar",
  "appDirectoryName": "app",
  "configFile": "C:\\SDRERC_CLIENTE\\app\\config\\sdrerc-app.properties",
  "javaArgs": [
    "-Dsdrerc.app.config=C:\\SDRERC_CLIENTE\\app\\config\\sdrerc-app.properties"
  ]
}
```

Pruebas desde cliente:

```powershell
Test-NetConnection IP_VPN_SERVIDOR -Port 1521
Test-NetConnection IP_VPN_SERVIDOR -Port 8088
Invoke-WebRequest http://IP_VPN_SERVIDOR:8088/version.json
Get-Content C:\SDRERC_CLIENTE\app\version-local.json
```

## Conexion Oracle por VPN

En `C:\SDRERC_CLIENTE\app\config\sdrerc-app.properties`:

```properties
sdrerc.db.url=jdbc:oracle:thin:@IP_VPN_SERVIDOR:1521/XEPDB1
sdrerc.db.user=SDRERC_APP
sdrerc.db.password=
```

No guardar credenciales reales en el repositorio ni en la release compartida.

## Instalacion cliente

### Instalador copiable

Carpeta:

```text
dist\client-installer
```

#### Instalacion FILE_SHARE

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\install-sdrerc-client.ps1 -RemoteReleaseMode "FILE_SHARE" -RemoteReleasePath "\\SERVIDOR\SDRERC_RELEASES\latest"
```

#### Instalacion HTTP / VPN

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\install-sdrerc-client.ps1 -RemoteReleaseMode "HTTP" -RemoteVersionUrl "http://IP_VPN_SERVIDOR:8088/version.json" -RemoteZipUrl "http://IP_VPN_SERVIDOR:8088/SDRERC-V2.zip" -RemoteChecksumsUrl "http://IP_VPN_SERVIDOR:8088/checksums.txt"
```

## Flujo del launcher

Al abrir `run-sdrerc-client.bat`, el launcher:

1. lee `updater-config.json`
2. detecta `remoteReleaseMode`
3. intenta leer `version.json` remoto
4. compara contra `version-local.json`
5. si hay nueva version:
   - descarga o copia `SDRERC-V2.zip`
   - valida checksum si existe
   - valida ZIP
   - crea backup de `app`
   - reemplaza archivos locales
   - preserva `sdrerc-app.properties`
   - actualiza `version-local.json`
6. inicia la version local

## Comportamiento ante fallas

### Sin SMB / sin VPN / sin HTTP

Si no se puede acceder a `version.json` remoto:

- registra advertencia en logs
- intenta abrir la ultima version local

Si no existe version local:

- termina con error claro

### Falla en actualizacion

Si falla copia, descarga, checksum, ZIP o reemplazo:

- registra error en `C:\SDRERC_CLIENTE\logs`
- restaura backup desde `C:\SDRERC_CLIENTE\backup`
- deja operativa la version anterior si existe

### Aplicacion ya abierta

Si detecta `SDRERC-V2.jar` en uso:

- no actualiza
- devuelve codigo 2
- el usuario debe cerrar la app y volver a abrirla

## Logs

Ruta:

```text
C:\SDRERC_CLIENTE\logs
```

Archivo:

```text
launcher-yyyyMMdd.log
```

Los logs no incluyen passwords.

## Archivos involucrados

- launcher cliente: `scripts/client/sdrerc-launcher.ps1`
- bat cliente: `scripts/client/run-sdrerc-client.bat`
- config cliente: `scripts/client/updater-config.json`
- publicador servidor: `scripts/server/publish-sdrerc-release.ps1`
- instalador copiable: `dist/client-installer`

## Seguridad

- no ejecutar el JAR desde la ruta remota
- no publicar Oracle a internet publico
- no guardar contraseñas reales en archivos versionados
- usar VPN privada para clientes remotos
- mantener el puerto 8088 expuesto solo en el segmento VPN o red privada
