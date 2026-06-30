# SDRERC V2 - Actualizacion cliente-servidor por red local

Esta guia describe el esquema vigente para distribuir SDRERC V2 en laptops Windows dentro de la misma LAN, usando una carpeta compartida SMB/UNC.

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

El cliente nunca ejecuta el JAR desde la ruta remota. Siempre copia o actualiza la release a `C:\SDRERC_CLIENTE\app` y ejecuta la version local.

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

## Modo vigente: FILE_SHARE / UNC

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

## Instalacion cliente

### Instalador copiable

Carpeta:

```text
dist\client-installer
```

Ejemplo:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\install-sdrerc-client.ps1 -RemoteReleaseMode "FILE_SHARE" -RemoteReleasePath "\\SERVIDOR\SDRERC_RELEASES\latest"
```

## Flujo del launcher

Al abrir `run-sdrerc-client.bat`, el launcher:

1. lee `updater-config.json`
2. valida `remoteReleaseMode`
3. lee `version.json` remoto desde UNC
4. compara contra `version-local.json`
5. si hay nueva version:
   - copia `SDRERC-V2.zip`
   - valida checksum si existe
   - valida ZIP
   - crea backup de `app`
   - reemplaza archivos locales
   - preserva `sdrerc-app.properties`
   - actualiza `version-local.json`
6. inicia la version local

## Comportamiento ante fallas

### Sin SMB

Si no se puede acceder a `version.json` remoto:

- registra advertencia en logs
- intenta abrir la ultima version local

Si no existe version local:

- termina con error claro

### Falla en actualizacion

Si falla copia, checksum, ZIP o reemplazo:

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

## Seguridad

- no ejecutar el JAR desde la ruta remota
- no publicar Oracle a internet publico
- no guardar contraseñas reales en archivos versionados
- usar solo la carpeta compartida SMB/UNC en la red local
