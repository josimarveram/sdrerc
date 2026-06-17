@echo off
setlocal

set "APP_HOME=%~dp0"
set "APP_JAR=%APP_HOME%SDRERC-V2.jar"
set "LIB_DIR=%APP_HOME%lib"
set "CONFIG_DIR=%APP_HOME%config"
set "CONFIG_FILE=%CONFIG_DIR%\sdrerc-client.properties"

if not exist "%APP_JAR%" (
    echo No se encontro el artefacto de SDRERC:
    echo %APP_JAR%
    echo.
    echo Vuelva a generar el paquete cliente con tools\installer-client\build-sdrerc-client-dist.ps1.
    pause
    exit /b 1
)

where java >nul 2>nul
if errorlevel 1 (
    echo No se encontro Java en PATH.
    echo Instale Java 8 o una version compatible, o configure java.exe antes de ejecutar SDRERC.
    pause
    exit /b 1
)

if not exist "%CONFIG_FILE%" (
    echo No existe el archivo de configuracion:
    echo %CONFIG_FILE%
    echo.
    echo Ejecute install-sdrerc-client.ps1 o cree el archivo desde config\sdrerc-client.properties.template.
    pause
    exit /b 1
)

findstr /R /C:"^sdrerc.db.password=$" "%CONFIG_FILE%" >nul 2>nul
if not errorlevel 1 (
    echo Aviso: sdrerc.db.password esta vacio en:
    echo %CONFIG_FILE%
    echo Complete la clave antes de iniciar si la base de datos la requiere.
    echo.
)

cd /d "%APP_HOME%"

echo Iniciando SDRERC Cliente...
echo Configuracion: %CONFIG_FILE%

java ^
    -Dawt.useSystemAAFontSettings=lcd ^
    -Dswing.aatext=true ^
    -Dsun.java2d.dpiaware=true ^
    "-Dsdrerc.app.config=%CONFIG_FILE%" ^
    "-Dsdrerc.config.dir=%CONFIG_DIR%" ^
    -cp "%APP_JAR%;%LIB_DIR%\*" ^
    com.sdrerc.appv2.MainV2

set "EXIT_CODE=%ERRORLEVEL%"
if not "%EXIT_CODE%"=="0" (
    echo.
    echo SDRERC finalizo con codigo %EXIT_CODE%.
    echo Si ve un error de Oracle, revise IP, puerto, servicio, usuario y password en config\sdrerc-client.properties.
    pause
)

exit /b %EXIT_CODE%
