/* ============================================================
   SCRIPT 117 - GENERADOR de sentencias ALTER TABLE...RENAME
   CONSTRAINT para PK/UK/FK/CHECK-con-nombre-propio, segun la
   decision ya confirmada (conservar sufijo descriptivo, no pasar
   a secuencia numerica).

   *** REPORTE DE SOLO LECTURA, NO EJECUTABLE DIRECTO ***
   *** DISEÑADO PARA CORRER DESPUES DEL SCRIPT 115 ***
   (necesita que las tablas ya tengan su nombre nuevo GST<X>_... -
   de ahi deriva el nombre viejo embebido en cada constraint).

   Metodo: como el nombre nuevo de tabla es siempre
   'GST<letra>_' (5 caracteres) + el nombre viejo sin abreviar,
   se puede recuperar el nombre viejo con SUBSTR(tabla_nueva, 6)
   y reemplazarlo dentro del nombre del constraint actual.

   Los ~17 CHECK con nombre propio que usan abreviaturas (ej.
   CK_SOL_CORREO_VIRTUAL, donde "SOL" abrevia "EXPEDIENTE_SOLICITUD")
   NO van a matchear por este metodo automatico - quedan marcados
   "REVISAR MANUALMENTE" a proposito, no se les adivina el nombre.
   Los 418 CHECK generados por Oracle (SYS_C%, NOT NULL implicitos)
   se excluyen por completo - no se tocan.
   ============================================================ */

SET PAGESIZE 200 LINESIZE 250 FEEDBACK ON HEADING ON

SELECT
    uc.constraint_type,
    uc.table_name,
    uc.constraint_name AS constraint_actual,
    SUBSTR(uc.table_name, 6) AS nombre_viejo_derivado,
    CASE
        WHEN INSTR(uc.constraint_name, SUBSTR(uc.table_name, 6)) = 0
        THEN 'REVISAR MANUALMENTE - el nombre del constraint no contiene el nombre viejo de tabla tal cual (probable abreviatura)'
        ELSE 'ALTER TABLE ' || uc.table_name || ' RENAME CONSTRAINT ' || uc.constraint_name || ' TO ' ||
             REPLACE(uc.constraint_name, SUBSTR(uc.table_name, 6), uc.table_name) || ';'
    END AS sentencia_o_revision
FROM user_constraints uc
WHERE uc.table_name LIKE 'GST%'                       -- solo corre una vez que el script 115 ya renombro las tablas
  AND uc.constraint_type IN ('P', 'R', 'U')            -- PK, FK, UNIQUE: siempre tienen nombre propio
  AND NOT (uc.constraint_type = 'C' AND uc.constraint_name LIKE 'SYS_C%')
ORDER BY uc.table_name, uc.constraint_type;

-- CHECK con nombre propio (excluye los NOT NULL implicitos SYS_C%)
SELECT
    uc.table_name,
    uc.constraint_name AS constraint_actual,
    CASE
        WHEN INSTR(uc.constraint_name, SUBSTR(uc.table_name, 6)) = 0
        THEN 'REVISAR MANUALMENTE - abreviatura, no matchea nombre viejo de tabla'
        ELSE 'ALTER TABLE ' || uc.table_name || ' RENAME CONSTRAINT ' || uc.constraint_name || ' TO ' ||
             REPLACE(uc.constraint_name, SUBSTR(uc.table_name, 6), uc.table_name) || ';'
    END AS sentencia_o_revision
FROM user_constraints uc
WHERE uc.table_name LIKE 'GST%'
  AND uc.constraint_type = 'C'
  AND uc.constraint_name NOT LIKE 'SYS_C%'
ORDER BY uc.table_name;
