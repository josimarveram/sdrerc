SDRERC Cliente - Instalador LAN
================================

1. Copie esta carpeta a la laptop cliente.
2. Ejecute PowerShell en esta carpeta:

   powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\install-sdrerc-client.ps1 -RemoteReleasePath "\\SERVIDOR\SDRERC_RELEASES\latest"

3. Edite:

   C:\SDRERC_CLIENTE\launcher\updater-config.json

   Configure remoteReleasePath con la carpeta compartida del servidor.

4. Edite o cree:

   C:\SDRERC_CLIENTE\app\config\sdrerc-app.properties

   Complete la URL JDBC, usuario y password de la base de datos. No deje passwords en archivos compartidos.

5. Ejecute el acceso directo "SDRERC Cliente" del escritorio.

El launcher siempre ejecuta la version local instalada en C:\SDRERC_CLIENTE\app.
Si hay una version nueva en la carpeta compartida, primero la copia localmente, valida el paquete, respalda la version anterior y luego inicia SDRERC.

Logs:

   C:\SDRERC_CLIENTE\logs

Backups:

   C:\SDRERC_CLIENTE\backup
