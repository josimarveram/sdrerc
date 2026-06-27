# SDRERC V2 - Resumen operativo LAN / VPN

## Escenarios soportados

### LAN / misma red

- Release en `D:\SDRERC_RELEASES\latest`
- Cliente por `FILE_SHARE`
- Ruta remota:

```text
\\SERVIDOR\SDRERC_RELEASES\latest
```

### VPN privada

- Release en `D:\SDRERC_RELEASES\latest`
- Cliente por `HTTP`
- URLs:

```text
http://IP_VPN_SERVIDOR:8088/version.json
http://IP_VPN_SERVIDOR:8088/SDRERC-V2.zip
http://IP_VPN_SERVIDOR:8088/checksums.txt
```

## Publicar release

```powershell
.\scripts\server\publish-sdrerc-release.ps1 -Version "1.0.12"
```

## Servir release por HTTP

```powershell
cd D:\SDRERC_RELEASES\latest
python -m http.server 8088
```

## Firewall recomendado

```powershell
New-NetFirewallRule -DisplayName "SDRERC Oracle 1521 VPN" -Direction Inbound -Protocol TCP -LocalPort 1521 -Action Allow
New-NetFirewallRule -DisplayName "SDRERC Releases HTTP 8088 VPN" -Direction Inbound -Protocol TCP -LocalPort 8088 -Action Allow
```

## Cliente: prueba minima

```powershell
Test-NetConnection IP_VPN_SERVIDOR -Port 1521
Test-NetConnection IP_VPN_SERVIDOR -Port 8088
Invoke-WebRequest http://IP_VPN_SERVIDOR:8088/version.json
Get-Content C:\SDRERC_CLIENTE\app\version-local.json
```

## Config Oracle cliente por VPN

```properties
sdrerc.db.url=jdbc:oracle:thin:@IP_VPN_SERVIDOR:1521/XEPDB1
sdrerc.db.user=SDRERC_APP
sdrerc.db.password=
```

## Regla operativa

- el cliente siempre ejecuta desde `C:\SDRERC_CLIENTE\app`
- si no hay conectividad remota, abre la ultima version local
- si falla una actualizacion, hace rollback desde `backup`
