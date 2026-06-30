# SDRERC V2 - Resumen operativo LAN

Este documento resume el modo operativo vigente para cliente-servidor en la misma red local.

## Escenario soportado

- Release en `D:\SDRERC_RELEASES\latest`
- Cliente por `FILE_SHARE`
- Ruta remota UNC:

```text
\\SERVIDOR\SDRERC_RELEASES\latest
```

## Publicar release

```powershell
.\scripts\server\publish-sdrerc-release.ps1 -Version "1.0.12"
```

## Verificacion

```powershell
Get-Content D:\SDRERC_RELEASES\latest\version.json
Get-Content D:\SDRERC_RELEASES\latest\checksums.txt
Test-NetConnection NOMBRE_SERVIDOR -Port 445
Test-Path "\\NOMBRE_SERVIDOR\SDRERC_RELEASES\latest\version.json"
```

## Config cliente

```json
{
  "remoteReleaseMode": "FILE_SHARE",
  "remoteReleasePath": "\\\\SERVIDOR\\SDRERC_RELEASES\\latest",
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

## Regla operativa

- el cliente siempre ejecuta desde `C:\SDRERC_CLIENTE\app`
- si no hay conectividad SMB, abre la ultima version local
- si falla una actualizacion, hace rollback desde `backup`
- no usar modos alternos; este esquema es solo SMB/UNC en la LAN
