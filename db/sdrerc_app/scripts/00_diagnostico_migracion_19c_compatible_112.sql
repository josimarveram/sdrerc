/* ============================================================================
   SCRIPT 00 - Diagnostico de migracion a Oracle 19c EE con COMPATIBLE = 11.2.0
   ----------------------------------------------------------------------------
   Objetivo:
     Inventariar, en la base ACTUAL (Oracle 21c XE), todo lo que impide o
     condiciona la migracion a Oracle 19c Enterprise Edition con
     COMPATIBLE = 11.2.0 y la estandarizacion de nomenclatura OTI para SGRR
     (codigo de sistema GR, usuarios IDOSGRR / IDUSGRR).

   Alcance:
     SOLO LECTURA. No crea, modifica ni borra objetos ni datos.
     Solo consulta el diccionario de datos del propio esquema (USER_*),
     paquetes publicos (DBMS_UTILITY, DBMS_OUTPUT) y hace COUNT(*) por tabla.

   Donde ejecutarlo:
     UNICAMENTE en la Oracle LOCAL de la laptop (esquema SDRERC_APP, copia de
     produccion). NO ejecutarlo contra produccion (IDUGS en svrgprc2).

   Como ejecutarlo (SQL*Plus, conectado como el dueno del esquema):
     sqlplus SDRERC_APP@localhost:1521/XEPDB1
     SQL> spool %TEMP%\diagnostico_migracion_19c.txt
     SQL> @db/sdrerc_app/scripts/00_diagnostico_migracion_19c_compatible_112.sql
     SQL> spool off
   (La contrasena la pide SQL*Plus; no se escribe en este archivo.)

   Secciones:
     1. Version, compatibilidad y juego de caracteres
     2. Inventario de objetos por tipo (y objetos invalidos)
     3. Columnas IDENTITY (no existen con COMPATIBLE 11.2)
     4. Columnas con DEFAULT secuencia.NEXTVAL o DEFAULT ON NULL (12c+)
     5. Nombres de mas de 30 caracteres (limite con COMPATIBLE 11.2)
     6. Tablas de mas de 21 caracteres (estandar de indices IDXBT_<tabla>_NN)
     7. Otras funciones 12c+ o con costo de licencia
     8. Cumplimiento de la nomenclatura actual (prefijos)
     9. PL/SQL, triggers, sinonimos, links y jobs
    10. Permisos del esquema (para recrearlos en IDOSGRR / IDUSGRR)
    11. Volumen de datos
    12. Conteo exacto de filas por tabla (linea base para validar la migracion)
    13. Resumen final
   ============================================================================ */

SET PAGESIZE 500
SET LINESIZE 220
SET TRIMSPOOL ON
SET TRIMOUT ON
SET FEEDBACK OFF
SET VERIFY OFF
SET LONG 4000
SET SERVEROUTPUT ON SIZE UNLIMITED FORMAT WRAPPED

COLUMN tipo             FORMAT A22
COLUMN nombre           FORMAT A45
COLUMN tabla            FORMAT A40
COLUMN columna          FORMAT A35
COLUMN valor            FORMAT A40
COLUMN parametro        FORMAT A28
COLUMN producto         FORMAT A45
COLUMN version          FORMAT A20
COLUMN generacion       FORMAT A12
COLUMN secuencia        FORMAT A20
COLUMN largo            FORMAT 999
COLUMN cantidad         FORMAT 9999999
COLUMN invalidos        FORMAT 99999
COLUMN mb               FORMAT 9999990.99
COLUMN tipo_dato        FORMAT A20
COLUMN semantica        FORMAT A10
COLUMN privilegio       FORMAT A35
COLUMN destinatario     FORMAT A30
COLUMN securefile       FORMAT A10

PROMPT
PROMPT ============================================================================
PROMPT  DIAGNOSTICO MIGRACION 19c EE / COMPATIBLE 11.2  (solo lectura)
PROMPT ============================================================================

-- ---------------------------------------------------------------------------
PROMPT
PROMPT === 1. Version, compatibilidad y juego de caracteres ===
-- ---------------------------------------------------------------------------
SELECT USER                                AS valor,
       'Esquema conectado'                 AS parametro
FROM dual
UNION ALL
SELECT SYS_CONTEXT('USERENV', 'CON_NAME'), 'Contenedor (PDB/CDB)'
FROM dual;

SELECT product AS producto, version_full AS version
FROM product_component_version;

DECLARE
  v_version       VARCHAR2(100);
  v_compatibilidad VARCHAR2(100);
BEGIN
  DBMS_UTILITY.DB_VERSION(v_version, v_compatibilidad);
  DBMS_OUTPUT.PUT_LINE('Version de la base : ' || v_version);
  DBMS_OUTPUT.PUT_LINE('COMPATIBLE actual  : ' || v_compatibilidad);
END;
/

SELECT parameter AS parametro, value AS valor
FROM nls_database_parameters
WHERE parameter IN ('NLS_CHARACTERSET', 'NLS_NCHAR_CHARACTERSET',
                    'NLS_LENGTH_SEMANTICS', 'NLS_RDBMS_VERSION')
ORDER BY parameter;

PROMPT (El destino 19c se creara con AL32UTF8 / AL16UTF16. Si aqui aparece otro juego de caracteres, revisar antes de migrar.)

-- ---------------------------------------------------------------------------
PROMPT
PROMPT === 2. Inventario de objetos por tipo ===
-- ---------------------------------------------------------------------------
SELECT object_type AS tipo,
       COUNT(*)    AS cantidad,
       SUM(CASE WHEN status <> 'VALID' THEN 1 ELSE 0 END) AS invalidos
FROM user_objects
WHERE object_name NOT LIKE 'BIN$%'
GROUP BY object_type
ORDER BY object_type;

PROMPT Objetos invalidos (deben ser 0 antes de migrar):
SELECT object_type AS tipo, object_name AS nombre
FROM user_objects
WHERE status <> 'VALID'
  AND object_name NOT LIKE 'BIN$%'
ORDER BY object_type, object_name;

-- ---------------------------------------------------------------------------
PROMPT
PROMPT === 3. Columnas IDENTITY (no existen con COMPATIBLE 11.2: pasan a secuencia + trigger) ===
-- ---------------------------------------------------------------------------
SELECT COUNT(*) AS cantidad
FROM user_tab_identity_cols;

SELECT table_name      AS tabla,
       column_name     AS columna,
       generation_type AS generacion,
       sequence_name   AS secuencia
FROM user_tab_identity_cols
ORDER BY table_name;

PROMPT (GENERATION_TYPE = ALWAYS no acepta IDs explicitos; BY DEFAULT si. El trigger de reemplazo
PROMPT  asignara el ID solo cuando venga vacio, como BY DEFAULT.)

-- ---------------------------------------------------------------------------
PROMPT
PROMPT === 4. Columnas con DEFAULT secuencia.NEXTVAL o DEFAULT ON NULL (12c+) ===
-- ---------------------------------------------------------------------------
DECLARE
  v_default VARCHAR2(32767);
  v_total   PLS_INTEGER := 0;
BEGIN
  FOR r IN (SELECT table_name, column_name, data_default, default_on_null
            FROM user_tab_columns
            WHERE identity_column = 'NO'
            ORDER BY table_name, column_id) LOOP
    v_default := r.data_default;
    IF r.default_on_null = 'YES'
       OR UPPER(v_default) LIKE '%NEXTVAL%' THEN
      v_total := v_total + 1;
      DBMS_OUTPUT.PUT_LINE(RPAD(r.table_name || '.' || r.column_name, 60)
                           || ' DEFAULT_ON_NULL=' || r.default_on_null
                           || ' DEFAULT=' || SUBSTR(TRIM(v_default), 1, 80));
    END IF;
  END LOOP;
  DBMS_OUTPUT.PUT_LINE('Total columnas a adaptar: ' || v_total);
END;
/

-- ---------------------------------------------------------------------------
PROMPT
PROMPT === 5. Nombres de mas de 30 caracteres (no se pueden crear con COMPATIBLE 11.2) ===
-- ---------------------------------------------------------------------------
SELECT tipo, nombre, largo
FROM (
  SELECT object_type AS tipo, object_name AS nombre, LENGTH(object_name) AS largo
  FROM user_objects
  WHERE LENGTH(object_name) > 30
    AND object_name NOT LIKE 'BIN$%'
    AND object_name NOT LIKE 'SYS\_%' ESCAPE '\'
  UNION ALL
  SELECT 'COLUMNA', table_name || '.' || column_name, LENGTH(column_name)
  FROM user_tab_columns
  WHERE LENGTH(column_name) > 30
  UNION ALL
  SELECT 'CONSTRAINT (' || constraint_type || ')', constraint_name, LENGTH(constraint_name)
  FROM user_constraints
  WHERE LENGTH(constraint_name) > 30
    AND constraint_name NOT LIKE 'SYS\_%' ESCAPE '\'
    AND constraint_name NOT LIKE 'BIN$%'
)
ORDER BY tipo, largo DESC, nombre;

-- ---------------------------------------------------------------------------
PROMPT
PROMPT === 6. Tablas de mas de 21 caracteres (IDXBT_<tabla>_NN no entra en 30) ===
-- ---------------------------------------------------------------------------
SELECT COUNT(*) AS cantidad
FROM user_tables
WHERE LENGTH(table_name) > 21
  AND table_name NOT LIKE 'BIN$%';

SELECT table_name AS tabla, LENGTH(table_name) AS largo
FROM user_tables
WHERE LENGTH(table_name) > 21
  AND table_name NOT LIKE 'BIN$%'
ORDER BY LENGTH(table_name) DESC, table_name;

-- ---------------------------------------------------------------------------
PROMPT
PROMPT === 7. Otras funciones 12c+ o con costo de licencia ===
-- ---------------------------------------------------------------------------
PROMPT 7.1 Tipos de dato usados (JSON, BOOLEAN u otros nuevos no existen en 11.2):
SELECT data_type AS tipo_dato, COUNT(*) AS cantidad
FROM user_tab_columns
GROUP BY data_type
ORDER BY data_type;

PROMPT 7.2 Columnas VARCHAR2/NVARCHAR2/RAW de mas de 4000 bytes (requieren MAX_STRING_SIZE=EXTENDED):
SELECT table_name AS tabla, column_name AS columna, data_type AS tipo_dato, data_length AS largo
FROM user_tab_columns
WHERE data_type IN ('VARCHAR2', 'NVARCHAR2', 'RAW')
  AND data_length > 4000
ORDER BY table_name, column_name;

PROMPT 7.3 Columnas invisibles (funcion 12c):
SELECT table_name AS tabla, column_name AS columna
FROM user_tab_cols
WHERE hidden_column = 'YES'
  AND user_generated = 'YES'
ORDER BY table_name, column_name;

PROMPT 7.4 Tablas particionadas (Partitioning es una opcion con costo en Enterprise Edition):
SELECT table_name AS tabla, partitioning_type AS tipo
FROM user_part_tables
ORDER BY table_name;

PROMPT 7.5 Columnas LOB (SecureFiles requiere COMPATIBLE >= 11.1, compatible con 11.2):
SELECT table_name AS tabla, column_name AS columna, securefile
FROM user_lobs
ORDER BY table_name, column_name;

PROMPT 7.6 Semantica de longitud de columnas de texto (B = bytes, C = caracteres):
SELECT char_used AS semantica, COUNT(*) AS cantidad
FROM user_tab_columns
WHERE data_type IN ('VARCHAR2', 'CHAR', 'NVARCHAR2', 'NCHAR')
GROUP BY char_used
ORDER BY char_used;

-- ---------------------------------------------------------------------------
PROMPT
PROMPT === 8. Cumplimiento de la nomenclatura actual (prefijos) ===
-- ---------------------------------------------------------------------------
PROMPT 8.1 Tablas y vistas por prefijo (GS* = estandar actual; el resto son vistas de compatibilidad u objetos sin renombrar):
SELECT object_type AS tipo,
       CASE WHEN object_name LIKE 'GS%' THEN 'Prefijo GS' ELSE 'Otro prefijo' END AS valor,
       COUNT(*) AS cantidad
FROM user_objects
WHERE object_type IN ('TABLE', 'VIEW', 'FUNCTION', 'PROCEDURE', 'PACKAGE', 'TRIGGER', 'SEQUENCE')
  AND object_name NOT LIKE 'BIN$%'
GROUP BY object_type, CASE WHEN object_name LIKE 'GS%' THEN 'Prefijo GS' ELSE 'Otro prefijo' END
ORDER BY object_type, valor;

PROMPT 8.2 Tablas sin prefijo GS (deberian ser 0):
SELECT table_name AS tabla
FROM user_tables
WHERE table_name NOT LIKE 'GS%'
  AND table_name NOT LIKE 'BIN$%'
ORDER BY table_name;

PROMPT 8.3 Indices que no siguen el patron estandar (IDX* o nombre de PK/UK):
SELECT COUNT(*) AS cantidad
FROM user_indexes i
WHERE i.index_name NOT LIKE 'IDX%'
  AND i.index_name NOT LIKE 'SYS\_%' ESCAPE '\'
  AND NOT EXISTS (SELECT 1 FROM user_constraints c
                  WHERE c.index_name = i.index_name
                    AND c.constraint_type IN ('P', 'U'));

PROMPT 8.4 Foreign keys sin correlativo numerico final (_NN):
SELECT COUNT(*) AS cantidad
FROM user_constraints
WHERE constraint_type = 'R'
  AND NOT REGEXP_LIKE(constraint_name, '_[0-9]{2}$');

-- ---------------------------------------------------------------------------
PROMPT
PROMPT === 9. PL/SQL, triggers, sinonimos, database links y jobs ===
-- ---------------------------------------------------------------------------
PROMPT 9.1 Codigo PL/SQL (revisar a mano si usa funciones 12c+):
SELECT type AS tipo, name AS nombre, COUNT(*) AS cantidad
FROM user_source
GROUP BY type, name
ORDER BY type, name;

PROMPT 9.2 Triggers existentes:
SELECT trigger_name AS nombre, table_name AS tabla, status AS valor
FROM user_triggers
ORDER BY trigger_name;

PROMPT 9.3 Sinonimos propios:
SELECT synonym_name AS nombre, table_owner || '.' || table_name AS valor
FROM user_synonyms
ORDER BY synonym_name;

PROMPT 9.4 Database links:
SELECT db_link AS nombre, username AS valor
FROM user_db_links
ORDER BY db_link;

PROMPT 9.5 Jobs del scheduler:
SELECT job_name AS nombre, enabled AS valor
FROM user_scheduler_jobs
ORDER BY job_name;

-- ---------------------------------------------------------------------------
PROMPT
PROMPT === 10. Permisos del esquema (para recrearlos en IDOSGRR / IDUSGRR) ===
-- ---------------------------------------------------------------------------
PROMPT 10.1 Privilegios de sistema:
SELECT privilege AS privilegio
FROM user_sys_privs
ORDER BY privilege;

PROMPT 10.2 Roles:
SELECT granted_role AS privilegio
FROM user_role_privs
ORDER BY granted_role;

PROMPT 10.3 Permisos otorgados por este esquema a otros usuarios:
SELECT grantee AS destinatario, table_name AS nombre, privilege AS privilegio
FROM user_tab_privs_made
ORDER BY grantee, table_name, privilege;

PROMPT 10.4 Permisos recibidos de otros esquemas:
SELECT owner || '.' || table_name AS nombre, privilege AS privilegio
FROM user_tab_privs_recd
ORDER BY owner, table_name, privilege;

-- ---------------------------------------------------------------------------
PROMPT
PROMPT === 11. Volumen de datos ===
-- ---------------------------------------------------------------------------
SELECT ROUND(SUM(bytes) / 1024 / 1024, 2) AS mb
FROM user_segments;

PROMPT Los 15 segmentos mas grandes:
SELECT segment_name AS nombre, segment_type AS tipo, mb
FROM (
  SELECT segment_name, segment_type, ROUND(bytes / 1024 / 1024, 2) AS mb
  FROM user_segments
  ORDER BY bytes DESC
)
WHERE ROWNUM <= 15;

-- ---------------------------------------------------------------------------
PROMPT
PROMPT === 12. Conteo exacto de filas por tabla (linea base para validar la migracion) ===
-- ---------------------------------------------------------------------------
DECLARE
  v_filas NUMBER;
  v_total NUMBER := 0;
BEGIN
  FOR t IN (SELECT table_name
            FROM user_tables
            WHERE table_name NOT LIKE 'BIN$%'
              AND nested = 'NO'
              AND secondary = 'N'
              AND (iot_type IS NULL OR iot_type = 'IOT')
            ORDER BY table_name) LOOP
    EXECUTE IMMEDIATE 'SELECT COUNT(*) FROM "' || t.table_name || '"' INTO v_filas;
    v_total := v_total + v_filas;
    DBMS_OUTPUT.PUT_LINE(RPAD(t.table_name, 45) || LPAD(TO_CHAR(v_filas), 12));
  END LOOP;
  DBMS_OUTPUT.PUT_LINE(RPAD('TOTAL FILAS', 45) || LPAD(TO_CHAR(v_total), 12));
END;
/

-- ---------------------------------------------------------------------------
PROMPT
PROMPT === 13. Resumen final ===
-- ---------------------------------------------------------------------------
DECLARE
  v_identity     NUMBER;
  v_largos       NUMBER;
  v_tablas21     NUMBER;
  v_particiones  NUMBER;
  v_extendidos   NUMBER;
  v_invisibles   NUMBER;
  v_invalidos    NUMBER;
  v_charset      VARCHAR2(64);

  PROCEDURE linea(p_texto VARCHAR2, p_valor NUMBER, p_ok BOOLEAN) IS
  BEGIN
    DBMS_OUTPUT.PUT_LINE(RPAD(p_texto, 60) || LPAD(TO_CHAR(p_valor), 8)
                         || CASE WHEN p_ok THEN '   OK' ELSE '   ADAPTAR' END);
  END;
BEGIN
  SELECT COUNT(*) INTO v_identity FROM user_tab_identity_cols;

  SELECT COUNT(*) INTO v_largos FROM (
    SELECT object_name FROM user_objects
     WHERE LENGTH(object_name) > 30 AND object_name NOT LIKE 'BIN$%'
       AND object_name NOT LIKE 'SYS\_%' ESCAPE '\'
    UNION ALL
    SELECT column_name FROM user_tab_columns WHERE LENGTH(column_name) > 30
    UNION ALL
    SELECT constraint_name FROM user_constraints
     WHERE LENGTH(constraint_name) > 30
       AND constraint_name NOT LIKE 'SYS\_%' ESCAPE '\'
       AND constraint_name NOT LIKE 'BIN$%');

  SELECT COUNT(*) INTO v_tablas21 FROM user_tables
   WHERE LENGTH(table_name) > 21 AND table_name NOT LIKE 'BIN$%';
  SELECT COUNT(*) INTO v_particiones FROM user_part_tables;
  SELECT COUNT(*) INTO v_extendidos FROM user_tab_columns
   WHERE data_type IN ('VARCHAR2', 'NVARCHAR2', 'RAW') AND data_length > 4000;
  SELECT COUNT(*) INTO v_invisibles FROM user_tab_cols
   WHERE hidden_column = 'YES' AND user_generated = 'YES';
  SELECT COUNT(*) INTO v_invalidos FROM user_objects
   WHERE status <> 'VALID' AND object_name NOT LIKE 'BIN$%';
  SELECT value INTO v_charset FROM nls_database_parameters
   WHERE parameter = 'NLS_CHARACTERSET';

  DBMS_OUTPUT.PUT_LINE('Juego de caracteres: ' || v_charset
                       || CASE WHEN v_charset = 'AL32UTF8' THEN '   OK' ELSE '   REVISAR' END);
  linea('Columnas IDENTITY (pasan a secuencia + trigger)', v_identity, v_identity = 0);
  linea('Nombres de mas de 30 caracteres', v_largos, v_largos = 0);
  linea('Tablas de mas de 21 caracteres (estandar de indices)', v_tablas21, v_tablas21 = 0);
  linea('Tablas particionadas (opcion con costo)', v_particiones, v_particiones = 0);
  linea('Columnas de texto de mas de 4000 bytes', v_extendidos, v_extendidos = 0);
  linea('Columnas invisibles', v_invisibles, v_invisibles = 0);
  linea('Objetos invalidos', v_invalidos, v_invalidos = 0);
  DBMS_OUTPUT.PUT_LINE('(Ver la seccion 4 para columnas con DEFAULT secuencia o DEFAULT ON NULL.)');
END;
/

PROMPT
PROMPT ============================================================================
PROMPT  Fin del diagnostico. No se modifico ningun objeto ni dato.
PROMPT ============================================================================
