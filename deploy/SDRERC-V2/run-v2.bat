@echo off
setlocal

set "APP_HOME=%~dp0"
set "APP_JAR=%APP_HOME%SDRERC-V2.jar"
set "LIB_DIR=%APP_HOME%lib"
set "CONFIG_DIR=%APP_HOME%config"
set "CONFIG_FILE=%CONFIG_DIR%\sdrerc-app.properties"

if not exist "%APP_JAR%" (
    echo No se encontro el artefacto de SDRERC V2:
    echo %APP_JAR%
    echo.
    echo Genere el artefacto con mvn clean package y copie target\SDRERC-V2.jar a esta carpeta.
    pause
    exit /b 1
)

where java >nul 2>nul
if errorlevel 1 (
    echo No se encontro Java en PATH.
    echo Instale Java 8 o configure java.exe antes de ejecutar SDRERC.
    pause
    exit /b 1
)

if not exist "%CONFIG_FILE%" (
    echo Aviso: no existe %CONFIG_FILE%
    echo Copie config\sdrerc-app.properties.example como config\sdrerc-app.properties y complete los datos de conexion.
    echo.
)

cd /d "%APP_HOME%"

java ^
    -Dawt.useSystemAAFontSettings=lcd ^
    -Dswing.aatext=true ^
    -Dsun.java2d.dpiaware=true ^
    "-Dsdrerc.config.dir=%CONFIG_DIR%" ^
    "-Dsdrerc.app.config=%CONFIG_FILE%" ^
    -cp "%APP_JAR%;%LIB_DIR%\*" ^
    com.sdrerc.appv2.MainV2

set "EXIT_CODE=%ERRORLEVEL%"
if not "%EXIT_CODE%"=="0" (
    echo.
    echo SDRERC finalizo con codigo %EXIT_CODE%.
    pause
)

exit /b %EXIT_CODE%
