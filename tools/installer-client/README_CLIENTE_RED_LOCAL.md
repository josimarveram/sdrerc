# Instalador cliente SDRERC para red local

## Escenario

- Laptop A: servidor con Oracle XE/XEPDB1 y esquema `SDRERC_APP`.
- Laptop B: cliente Windows sin Oracle instalado. Solo ejecuta SDRERC Java Swing.
- La conexion usa JDBC hacia la IP de la laptop servidor, por ejemplo:

```text
jdbc:oracle:thin:@//192.168.43.120:1521/XEPDB1
```

## Decision tecnica

El cliente usa el JAR autocontenido `SDRERC-V2.jar` generado por Maven Shade. No se copia `.m2`, no se requiere IntelliJ y no se instala Oracle en la laptop cliente.

La conexion se configura fuera del codigo Java con:

```text
config/sdrerc-client.properties
```

El password no se guarda en el repositorio. El instalador deja el valor vacio salvo que se pase explicitamente por parametro.

## Generar paquete cliente

Desde la raiz del proyecto:

```powershell
mvn clean package
.\tools\installer-client\build-sdrerc-client-dist.ps1 -SkipBuild
```

O en un solo paso:

```powershell
.\tools\installer-client\build-sdrerc-client-dist.ps1
```

La salida queda en:

```text
dist\sdrerc-client\
  SDRERC-V2.jar
  run-sdrerc-client.bat
  install-sdrerc-client.ps1
  uninstall-sdrerc-client.ps1
  README_CLIENTE_RED_LOCAL.md
  config\
    sdrerc-client.properties.template
  lib\
  logs\
```

## Instalar en laptop cliente

1. Copie la carpeta `dist\sdrerc-client` a la laptop cliente.
2. Abra PowerShell en esa carpeta.
3. Si Windows bloquea scripts solo para la sesion actual:

```powershell
Set-ExecutionPolicy -Scope Process Bypass
```

4. Ejecute:

```powershell
.\install-sdrerc-client.ps1 `
  -ServerIp "192.168.43.120" `
  -Port "1521" `
  -ServiceName "XEPDB1" `
  -DbUser "SDRERC_APP"
```

El instalador crea:

```text
C:\SDRERC_CLIENTE\
  SDRERC-V2.jar
  run-sdrerc-client.bat
  config\
    sdrerc-client.properties
```

Tambien crea el acceso directo `SDRERC Cliente` en el escritorio.

## Configurar password

Edite:

```text
C:\SDRERC_CLIENTE\config\sdrerc-client.properties
```

Complete:

```text
sdrerc.db.password=
```

No guarde passwords reales en el repositorio ni en documentacion.

## Probar red y puerto 1521

En la laptop cliente:

```powershell
Test-NetConnection 192.168.43.120 -Port 1521
```

Si `TcpTestSucceeded` es `False`, revise:

- IP actual de la laptop servidor.
- Firewall de Windows en la laptop servidor.
- Listener Oracle.
- Que ambas laptops esten en la misma red u hotspot.

## Ejecutar SDRERC

Use el acceso directo del escritorio o:

```bat
C:\SDRERC_CLIENTE\run-sdrerc-client.bat
```

El launcher mantiene la consola abierta si ocurre error para poder ver problemas de Java, configuracion u Oracle.

## Errores comunes

- `No se encontro Java en PATH`: instale Java 8 o configure `java.exe`.
- `No existe el archivo de configuracion`: ejecute el instalador o cree `config\sdrerc-client.properties`.
- `sdrerc.db.password esta vacio`: complete el password localmente.
- `No responde el puerto 1521`: revise red/firewall/listener.
- Error Oracle JDBC: revise URL, servicio `XEPDB1`, usuario y password.

## Desinstalar

En la laptop cliente:

```powershell
C:\SDRERC_CLIENTE\uninstall-sdrerc-client.ps1
```

O sin confirmacion:

```powershell
C:\SDRERC_CLIENTE\uninstall-sdrerc-client.ps1 -Force
```
