# SDRERC V2 - Instalacion y actualizacion automatica en LAN

Esta guia describe el esquema cliente-servidor para distribuir SDRERC V2 en laptops Windows dentro de una red LAN o hotspot movil.

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

El cliente nunca ejecuta el JAR desde la carpeta compartida. El launcher copia la version remota a `C:\SDRERC_CLIENTE\app` y ejecuta siempre la version local.

## Servidor

### 1. Crear carpeta de releases

```powershell
New-Item -ItemType Directory -Force -Path D:\SDRERC_RELEASES\latest
```

Comparta `D:\SDRERC_RELEASES` en la red, por ejemplo como:

```text
\\NOMBRE_SERVIDOR\SDRERC_RELEASES
```

Permisos recomendados:

- Usuarios cliente: lectura.
- Responsable de despliegue: lectura/escritura.
- Evitar permisos de escritura para usuarios finales.

### 2. Publicar primera version

Desde la raiz del proyecto:

```powershell
.\scripts\server\publish-sdrerc-release.ps1 -Version "1.0.0"
```

El script ejecuta:

- `mvn clean compile`
- `mvn clean package`

Luego publica en:

```text
D:\SDRERC_RELEASES\latest
```

Archivos generados:

- `SDRERC-V2.zip`
- `version.json`
- `checksums.txt`

### 3. Publicar una actualizacion

Use una version mayor a la instalada:

```powershell
.\scripts\server\publish-sdrerc-release.ps1 -Version "1.0.1"
```

La laptop cliente detectara la nueva version al abrir SDRERC desde el acceso directo.

### 4. Verificar release

Revise:

```powershell
Get-Content D:\SDRERC_RELEASES\latest\version.json
Get-Content D:\SDRERC_RELEASES\latest\checksums.txt
```

`version.json` debe indicar la version publicada y `checksums.txt` debe contener el SHA256 de `SDRERC-V2.zip`.

## Cliente

### 1. Instalar launcher

Copie la carpeta:

```text
dist\client-installer
```

a la laptop cliente y ejecute:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\install-sdrerc-client.ps1 -RemoteReleasePath "\\NOMBRE_SERVIDOR\SDRERC_RELEASES\latest"
```

El instalador crea:

```text
C:\SDRERC_CLIENTE
```

y un acceso directo en el escritorio llamado:

```text
SDRERC Cliente
```

No requiere permisos de administrador salvo que las politicas locales restrinjan escritura en `C:\`.

### 2. Configurar updater

Archivo:

```text
C:\SDRERC_CLIENTE\launcher\updater-config.json
```

Campos principales:

```json
{
  "remoteReleasePath": "\\\\NOMBRE_SERVIDOR\\SDRERC_RELEASES\\latest",
  "localBasePath": "C:\\SDRERC_CLIENTE",
  "javaPath": "",
  "mainJar": "SDRERC-V2.jar",
  "configFile": "C:\\SDRERC_CLIENTE\\app\\config\\sdrerc-app.properties"
}
```

Si Java no esta en `PATH`, configure `javaPath`, por ejemplo:

```json
"javaPath": "C:\\Program Files\\Java\\jre1.8.0_491\\bin\\java.exe"
```

### 3. Configurar conexion SDRERC

Archivo local:

```text
C:\SDRERC_CLIENTE\app\config\sdrerc-app.properties
```

El instalador crea este archivo si no existe, con password vacio.

Contenido sugerido:

```properties
sdrerc.db.url=jdbc:oracle:thin:@//192.168.43.120:1521/XEPDB1
sdrerc.db.user=SDRERC_APP
sdrerc.db.password=
```

No guarde contrasenas reales en el repositorio ni en la carpeta compartida de releases.

### 4. Primera ejecucion

Abra el acceso directo:

```text
SDRERC Cliente
```

El launcher:

1. Lee `updater-config.json`.
2. Consulta `version.json` en la carpeta LAN.
3. Descarga `SDRERC-V2.zip` a `updates`.
4. Valida SHA256 si existe.
5. Descomprime en staging.
6. Crea backup de `app`.
7. Reemplaza la version local.
8. Preserva `sdrerc-app.properties` local si ya existia.
9. Inicia `SDRERC-V2.jar`.

## Comportamiento ante errores

### Sin red

Si no se puede acceder a `remoteReleasePath`, el launcher muestra un aviso y abre la ultima version local disponible.

Si no existe version local, muestra error claro y no inicia.

### Falla de actualizacion

Si falla la copia, checksum, ZIP o reemplazo:

1. Registra el error en `C:\SDRERC_CLIENTE\logs`.
2. Restaura el backup de `C:\SDRERC_CLIENTE\backup`.
3. Intenta dejar disponible la version anterior.

### Aplicacion ya abierta

Si detecta un proceso usando `SDRERC-V2.jar`, no actualiza. El usuario debe cerrar SDRERC y volver a abrirlo.

## Logs

Ruta:

```text
C:\SDRERC_CLIENTE\logs
```

Formato:

```text
launcher-yyyyMMdd.log
```

Los logs no deben incluir passwords.

## Prueba rapida de conectividad

Desde la laptop cliente:

```powershell
Test-NetConnection NOMBRE_SERVIDOR -Port 445
Test-NetConnection 192.168.43.120 -Port 1521
```

El puerto 445 valida acceso SMB a la carpeta compartida. El puerto 1521 valida conectividad hacia Oracle.

## Convertir a instalador EXE

No se incluye un EXE para evitar dependencias externas. Cuando se requiera, puede empaquetarse `dist\client-installer` con Inno Setup o una herramienta institucional equivalente.

El instalador EXE debe ejecutar `install-sdrerc-client.ps1` y no debe incrustar credenciales reales.

## Archivos del proyecto

- Cliente launcher: `scripts/client/sdrerc-launcher.ps1`
- BAT cliente: `scripts/client/run-sdrerc-client.bat`
- Config ejemplo: `scripts/client/updater-config.json`
- Publicador servidor: `scripts/server/publish-sdrerc-release.ps1`
- Plantilla de version: `scripts/server/version-template.json`
- Instalador copiable: `dist/client-installer`

## Notas de seguridad

- No ejecutar el JAR desde la carpeta compartida.
- No guardar contrasenas reales en repositorio.
- No dar permisos de escritura sobre releases a usuarios finales.
- Mantener backups locales para rollback.
- Usar versiones semanticas simples como `1.0.0`, `1.0.1`.
