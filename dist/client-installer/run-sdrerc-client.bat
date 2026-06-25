@echo off
setlocal

set "BASE_DIR=%~dp0"
set "LAUNCHER=%BASE_DIR%launcher\sdrerc-launcher.ps1"
set "CONFIG=%BASE_DIR%launcher\updater-config.json"

if not exist "%LAUNCHER%" (
    set "LAUNCHER=%BASE_DIR%sdrerc-launcher.ps1"
    set "CONFIG=%BASE_DIR%updater-config.json"
)

if not exist "%LAUNCHER%" (
    echo No se encontro sdrerc-launcher.ps1.
    echo Revise la instalacion del cliente SDRERC.
    pause
    exit /b 1
)

powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%LAUNCHER%" -ConfigPath "%CONFIG%"
set "EXIT_CODE=%ERRORLEVEL%"

if not "%EXIT_CODE%"=="0" (
    echo.
    echo SDRERC no pudo iniciar correctamente. Codigo: %EXIT_CODE%
    echo Revise los logs en C:\SDRERC_CLIENTE\logs.
    pause
)

exit /b %EXIT_CODE%
