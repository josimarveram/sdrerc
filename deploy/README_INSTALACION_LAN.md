# Instalacion LAN de SDRERC

## Artefacto oficial

El aplicativo distribuible de SDRERC usa el main `com.sdrerc.appv2.MainV2`.
El JAR legacy `UserManagementApp-1.0.0.jar` no corresponde al despliegue final de SDRERC V2.

Se eligio JAR autocontenido con Maven Shade para evitar dependencias de `.m2/repository`,
IntelliJ IDEA o `target/classes` en PCs cliente. La carpeta `lib` queda disponible para
dependencias externas futuras y el launcher la incluye siempre en el classpath.

## Generar el deploy

Desde la raiz del proyecto:

```powershell
mvn clean package
Copy-Item target\SDRERC-V2.jar deploy\SDRERC-V2\SDRERC-V2.jar -Force
```

La carpeta minima de despliegue queda asi:

```text
deploy\SDRERC-V2\
  SDRERC-V2.jar
  run-v2.bat
  config\
    sdrerc-app.properties.example
  lib\
  logs\
```

## Configuracion de conexion

Copie el archivo de ejemplo:

```powershell
Copy-Item deploy\SDRERC-V2\config\sdrerc-app.properties.example deploy\SDRERC-V2\config\sdrerc-app.properties
```

Edite `deploy\SDRERC-V2\config\sdrerc-app.properties` con los valores del ambiente LAN.
No guarde passwords reales en la documentacion ni en control de versiones.

Tambien puede configurar la conexion mediante variables de entorno:

```text
SDRERC_APP_DB_URL
SDRERC_APP_DB_USER
SDRERC_APP_DB_PASSWORD
SDRERC_APP_CONFIG
```

## Ejecutar en una PC cliente

1. Instale Java 8 o una version compatible disponible en `PATH`.
2. Copie la carpeta `deploy\SDRERC-V2` a la PC cliente o a una ruta local como `C:\SDRERC`.
3. Complete `config\sdrerc-app.properties`.
4. Ejecute `run-v2.bat`.

El launcher usa solo rutas relativas a su propia carpeta: `%~dp0`, `lib\*` y `config\`.

## Compilar instalador Inno Setup

Instale Inno Setup y compile:

```powershell
ISCC deploy\installer\sdrerc-v2.iss
```

El instalador copia el JAR correcto, `run-v2.bat`, `config`, `lib` y `logs`, y crea accesos
directos en escritorio y menu inicio.
