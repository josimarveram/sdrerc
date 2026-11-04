# Guía de instalación de Oracle 19c EE en la laptop (entorno de prueba SGRR)

**Objetivo:** dejar en la laptop de desarrollo una base Oracle 19c Enterprise Edition **idéntica en configuración** a la que luego se creará en svrgprc2. Sirve para ensayar la migración y probar la aplicación en el puerto 8082, en paralelo a la versión actual.

**Regla de oro:** todo lo que se haga aquí se repite igual en svrgprc2. Anota cualquier diferencia o problema.

---

## 0. Resumen de la configuración objetivo

| Parámetro | Valor |
|---|---|
| Edición | Oracle Database 19c Enterprise Edition |
| Licencia en la laptop | OTN Developer License (gratuita, solo desarrollo y pruebas) |
| Oracle Home (19c) | `D:\app\oracle19\product\19.0.0\dbhome_1` |
| Oracle Base (19c) | `D:\app\oracle19` |
| Tipo de base | **non-CDB** (sin contenedores) |
| Nombre / SID / servicio | `SGRR` |
| `COMPATIBLE` | **`11.2.0`** (no se puede bajar después) |
| Juego de caracteres | **AL32UTF8** / nacional AL16UTF16 |
| Listener | `LISTENER19`, puerto **1522** |
| Memoria | ~2 GB (SGA 1.5 GB + PGA 512 MB) |
| URL JDBC resultante | `jdbc:oracle:thin:@//localhost:1522/SGRR` |

**Lo que NO se toca:** la Oracle 21c Express actual (`D:\app\joel_\product\21c\dbhomeXE`, servicio `OracleServiceXE`, listener en el puerto 1521, esquema `SDRERC_APP`). Las dos bases conviven en la laptop.

**Estado de la laptop (verificado el 04/11/2026):** Windows 10 Home 22H2, 16 GB de RAM, 53 GB libres en `D:`. Alcanza para las dos bases.

---

## 1. Requisitos previos

1. **Usuario administrador de Windows** en la laptop.
2. **Espacio:** ~10 GB para el software y ~5 GB para la base (se usa el disco `D:`).
3. **Descargar el instalador:**
   - Página: <https://www.oracle.com/database/technologies/oracle19c-windows-downloads.html>
   - Archivo: **`WINDOWS.X64_193000_db_home.zip`** (Oracle Database 19c 19.3 para Windows x64, ~2.9 GB).
   - Pide iniciar sesión con una cuenta Oracle (gratuita) y aceptar la licencia OTN.
   - En la laptop basta la versión 19.3. Lo que importa para la compatibilidad es `COMPATIBLE = 11.2.0`, que es igual en 19.3 y en 19.25. En svrgprc2, OTI instalará la 19.25.
4. **Cerrar** el backend local (`sge-backend`), SQL Developer y cualquier programa conectado a Oracle mientras se instala.

> **Windows 10 Home:** Oracle certifica 19c en Windows 10 Pro, Enterprise y Education. En Home normalmente funciona, pero el instalador puede mostrar la advertencia `[INS-13001] Environment does not meet minimum requirements`. Se responde **Sí / Yes** para continuar.

---

## 2. Instalar el software (sin crear base)

Igual que en svrgprc2: primero solo el software, y la base se crea aparte con `dbca`.

1. Crear la carpeta `D:\app\oracle19\product\19.0.0\dbhome_1`.
2. **Descomprimir** `WINDOWS.X64_193000_db_home.zip` **dentro de esa carpeta**. En 19c la carpeta donde se descomprime pasa a ser el Oracle Home, así que no se mueve después.
3. Clic derecho en `D:\app\oracle19\product\19.0.0\dbhome_1\setup.exe` → **Ejecutar como administrador**.
4. En el asistente:

| Paso | Elegir |
|---|---|
| Configuration Option | **Set Up Software Only** |
| Database Installation Option | Single instance database installation |
| Database Edition | **Enterprise Edition** |
| Oracle Home User | **Use Virtual Account** |
| Oracle Base | `D:\app\oracle19` |
| Prerequisite Checks | Si solo aparece la advertencia de Windows 10 Home, marcar *Ignore All* y continuar |

5. Esperar a que termine (10 a 20 minutos).

### 2.1 Cuidar el PATH (importante)

El instalador agrega `D:\app\oracle19\product\19.0.0\dbhome_1\bin` **al inicio** del PATH. Desde ese momento, `sqlplus`, `expdp` e `impdp` serían los de la 19c, y eso puede afectar los scripts actuales, como `scripts\dev\traer-bd-produccion-a-local.ps1`, que trabajan con la 21c.

Para no alterar lo que ya funciona:

1. Abrir *Variables de entorno* (Sistema) → `Path`.
2. **Subir** `D:\app\joel_\product\21c\dbhomeXE\bin` **por encima** de la entrada de la 19c.
3. Abrir una consola nueva y verificar:

```powershell
where.exe sqlplus
# La primera línea debe ser D:\app\joel_\product\21c\dbhomeXE\bin\sqlplus.exe
```

Para trabajar con la 19c se usan sus herramientas con la ruta completa (ver el paso 6).

---

## 3. Crear el listener en el puerto 1522

La 21c ya ocupa el 1521, así que la 19c lleva su propio listener.

1. Ejecutar como administrador: `D:\app\oracle19\product\19.0.0\dbhome_1\bin\netca.bat`
2. Elegir **Listener configuration** → **Add**.
3. Nombre: **`LISTENER19`** · Protocolo: TCP · Puerto: **1522** (no usar el estándar 1521).
4. Terminar. Se crea el servicio de Windows `OracleOraDB19Home1TNSListenerLISTENER19`.

---

## 4. Crear la base con dbca

Ejecutar como administrador: `D:\app\oracle19\product\19.0.0\dbhome_1\bin\dbca.bat`

| Paso de dbca | Elegir |
|---|---|
| Database Operation | Create a database |
| Creation Mode | **Advanced configuration** |
| Deployment Type | Oracle Single Instance database · Plantilla **Custom Database** |
| Database Identification | Global database name: **`SGRR`** · SID: **`SGRR`** · **Desmarcar** *Create as Container database* |
| Storage Option | File system · ubicación `{ORACLE_BASE}\oradata\{DB_UNIQUE_NAME}` |
| Fast Recovery Option | Sin Fast Recovery Area · sin archivelog (es un ambiente de prueba) |
| Network Configuration | Marcar **`LISTENER19`** (puerto 1522) |
| Data Vault Option | No configurar |
| Configuration Options → Memory | Automatic Shared Memory Management · SGA ~1536 MB · PGA ~512 MB |
| Configuration Options → Sizing | Processes: **300** |
| Configuration Options → Character sets | **Use Unicode (AL32UTF8)** · National: **AL16UTF16** |
| Configuration Options → Connection mode | Dedicated server |
| Configuration Options → **All Initialization Parameters** | Buscar **`compatible`** y poner **`11.2.0`** (marcar la casilla *Include in spfile* si aparece) |
| Database Options (componentes) | **Desmarcar todos los opcionales** (Oracle JVM, Oracle Text, Multimedia, OLAP, Spatial, Label Security, Database Vault, Application Express). La aplicación no los usa |
| Management Options | Sin Enterprise Manager |
| User Credentials | Contraseñas de **SYS** y **SYSTEM** (guardarlas en un lugar seguro; no escribirlas en archivos del proyecto) |
| Creation Option | Marcar **Create database** y también **Generate database creation scripts** (guardar en `D:\app\oracle19\admin\SGRR\scripts`) |

**Por qué "Custom Database":** las plantillas *General Purpose* y *Data Warehouse* restauran archivos ya creados con compatibilidad 19. Como `COMPATIBLE` no se puede bajar, con ellas no se puede dejar en 11.2. *Custom Database* crea la base desde cero.

**Por qué generar los scripts:** son la "receta" exacta de esta base. Sirven para crear la de svrgprc2 de forma idéntica. Antes de copiarlos a otro lado, revisar que no contengan contraseñas.

> **Si dbca no acepta `compatible = 11.2.0`:** en el último paso elegir solo *Generate database creation scripts* (sin *Create database*), editar el archivo `init.ora` generado poniendo `compatible='11.2.0'` y ejecutar el `.bat` generado como administrador.

La creación tarda de 15 a 40 minutos.

---

## 5. Ahorrar memoria (opcional, recomendado)

Las dos bases juntas usan unos 4 a 5 GB de RAM. Para que la 19c solo consuma memoria cuando se usa:

1. `services.msc`
2. Cambiar a **Manual**: `OracleServiceSGRR` y `OracleOraDB19Home1TNSListenerLISTENER19`.
3. Para trabajar con la 19c, iniciarlos a mano, o desde una consola de administrador:

```powershell
Start-Service OracleOraDB19Home1TNSListenerLISTENER19, OracleServiceSGRR
```

---

## 6. Verificación

Conectarse con el sqlplus **de la 19c** (ruta completa) como SYSTEM:

```powershell
& "D:\app\oracle19\product\19.0.0\dbhome_1\bin\sqlplus.exe" system@//localhost:1522/SGRR
```

Ejecutar:

```sql
-- Versión
SELECT banner_full FROM v$version;

-- Debe decir 11.2.0
SELECT value FROM v$parameter WHERE name = 'compatible';

-- Debe decir NO (non-CDB)
SELECT cdb, name, log_mode FROM v$database;

-- Debe decir AL32UTF8 y AL16UTF16
SELECT parameter, value FROM nls_database_parameters
WHERE parameter IN ('NLS_CHARACTERSET', 'NLS_NCHAR_CHARACTERSET');

-- Prueba de la restricción de 11.2 (debe FALLAR: confirma que la compatibilidad quedó bien)
CREATE TABLE prueba_identity (id NUMBER GENERATED BY DEFAULT AS IDENTITY);
-- Esperado: error indicando que la funcionalidad requiere un COMPATIBLE mayor.
-- Si la tabla se crea, COMPATIBLE NO quedó en 11.2: borrarla (DROP TABLE prueba_identity) y revisar el paso 4.
```

Verificar también el listener:

```powershell
& "D:\app\oracle19\product\19.0.0\dbhome_1\bin\lsnrctl.exe" status LISTENER19
# Debe mostrar el puerto 1522 y el servicio "SGRR"
```

**Checklist final:**

- [ ] Versión 19c Enterprise Edition
- [ ] `compatible` = 11.2.0
- [ ] `cdb` = NO
- [ ] AL32UTF8 / AL16UTF16
- [ ] La tabla con IDENTITY **no** se puede crear
- [ ] Listener `LISTENER19` en el puerto 1522
- [ ] La 21c sigue funcionando: `sqlplus SDRERC_APP@localhost:1521/XEPDB1` conecta y la app actual (puerto 8081) abre normalmente
- [ ] `where.exe sqlplus` sigue mostrando primero el de la 21c

---

## 7. Qué sigue (no es parte de esta guía)

Después de la instalación, en los días 1 y 2 del plan:

1. Ejecutar el diagnóstico de solo lectura `db/sdrerc_app/scripts/00_diagnostico_migracion_19c_compatible_112.sql` sobre la 21c local.
2. Crear los usuarios **`IDOSGRR`** (dueño de los datos) e **`IDUSGRR`** (acceso de la aplicación) en la 19c, con los scripts SQL nuevos de la migración.
3. Migrar los datos de la copia local (`SDRERC_APP`) a `IDOSGRR` con los nombres GR.
4. Levantar la aplicación SGRR en el puerto **8082** apuntando a `jdbc:oracle:thin:@//localhost:1522/SGRR` con el usuario `IDUSGRR`.

Los pasos 2 a 4 se harán con scripts nuevos. Los scripts existentes no se modifican.

---

## 8. Problemas frecuentes

| Síntoma | Causa probable | Solución |
|---|---|---|
| `[INS-13001]` al iniciar el instalador | Windows 10 Home no está certificado | Continuar (*Yes*) |
| `ORA-12541: TNS:no listener` en 1522 | El servicio del listener está detenido | `Start-Service OracleOraDB19Home1TNSListenerLISTENER19` |
| `ORA-12514: listener does not currently know of service` | La base aún no se registró en el listener | Esperar un minuto o, como SYSTEM: `ALTER SYSTEM REGISTER;` |
| `ORA-12638` al conectar | Autenticación de Windows (NTS) | Usar un `sqlnet.ora` con `SQLNET.AUTHENTICATION_SERVICES=(NONE)` (igual que en los scripts actuales) |
| Los scripts actuales usan el sqlplus de la 19c | El PATH quedó con la 19c primero | Repetir el paso 2.1 |
| La laptop se pone lenta | Dos bases encendidas a la vez | Paso 5: dejar la 19c en inicio manual |
