/* ============================================================
   SCRIPT 116 - GENERADOR de sentencias ALTER TABLE...RENAME COLUMN
   para las 871 columnas del esquema, segun la regla de prefijo ya
   aprobada (docs/nomenclatura_bd/mapeo_nomenclatura_gs.md, seccion 8).

   *** ESTO ES UN REPORTE DE SOLO LECTURA, NO UN SCRIPT EJECUTABLE ***
   Genera el TEXTO de cada sentencia candidata para que se revise
   fila por fila antes de correr nada - especialmente las marcadas
   "REVISAR MANUALMENTE" (columnas que no calzan con ningun patron
   conocido; NUNCA se les adivina un prefijo).

   Funciona con los nombres de tabla ACTUALES o ya renombrados a GS -
   usa el nombre de tabla que exista en ese momento en user_tab_columns,
   no depende de que el script 115 ya se haya corrido.

   Uso: correr este SELECT, revisar la columna PROPUESTA/REVISION,
   ajustar a mano lo que haga falta, y recien ahi construir los
   ALTER TABLE reales (no se generan automaticamente para evitar
   ejecutar 871 renombrados sin revision humana).
   ============================================================ */

SET PAGESIZE 200 LINESIZE 250 FEEDBACK ON HEADING ON

SELECT
    utc.table_name,
    utc.column_name AS columna_actual,
    CASE
        -- FK / PK numerica
        WHEN REGEXP_LIKE(utc.column_name, '^ID_') THEN utc.column_name
        -- Numero de documento/expediente/etc
        WHEN REGEXP_LIKE(utc.column_name, '^NUMERO_') THEN 'NU_' || SUBSTR(utc.column_name, 8)
        -- Fecha explicita
        WHEN REGEXP_LIKE(utc.column_name, '^FECHA_') THEN 'FE_' || SUBSTR(utc.column_name, 7)
        -- Timestamps de auditoria sin prefijo "fecha_"
        WHEN utc.column_name IN ('CREADO_EN', 'MODIFICADO_EN') THEN 'FE_' || utc.column_name
        -- Codigo
        WHEN utc.column_name = 'CODIGO' THEN 'CO_CODIGO'
        WHEN REGEXP_LIKE(utc.column_name, '_CODIGO$') THEN 'CO_' || utc.column_name
        -- Nombre / razon social
        WHEN utc.column_name IN ('NOMBRE', 'NOMBRE_COMPLETO', 'RAZON_SOCIAL', 'NOMBRES') THEN 'NO_' || utc.column_name
        WHEN REGEXP_LIKE(utc.column_name, '^NOMBRE_') THEN 'NO_' || SUBSTR(utc.column_name, 8)
        -- Apellidos
        WHEN REGEXP_LIKE(utc.column_name, '^APELLIDO_') THEN 'AP_' || SUBSTR(utc.column_name, 10)
        -- Texto libre / descripcion
        WHEN utc.column_name IN ('DESCRIPCION', 'OBSERVACION', 'OBSERVACIONES', 'COMENTARIO', 'FUNDAMENTO',
                                  'MOTIVO', 'DETALLE_OBSERVACION', 'ASUNTO', 'MENSAJE') THEN 'DE_' || utc.column_name
        -- Indicadores booleanos (0/1)
        WHEN utc.column_name = 'ACTIVO' THEN 'IN_ACTIVO'
        WHEN REGEXP_LIKE(utc.column_name, '^(ES_|REQUIERE_|TIENE_|CUMPLE_|NOTIFICADO$|ARCHIVADO$|CERRADO$)')
             AND utc.data_type IN ('NUMBER') THEN 'IN_' || utc.column_name
        -- Tipo/clasificacion textual (no FK, no empieza con ID_)
        WHEN REGEXP_LIKE(utc.column_name, '^TIPO_') THEN 'TI_' || SUBSTR(utc.column_name, 6)
        -- Usuario / auditoria de autor
        WHEN utc.column_name IN ('CREADO_POR', 'MODIFICADO_POR', 'USERNAME', 'RECIBIDO_POR') THEN 'US_' || utc.column_name
        -- Direccion / ubicacion
        WHEN utc.column_name IN ('DIRECCION', 'UBICACION') THEN 'DI_' || utc.column_name
        -- Password
        WHEN REGEXP_LIKE(utc.column_name, 'PASSWORD') THEN 'PW_' || utc.column_name
        -- Monto / cantidad / porcentaje
        WHEN REGEXP_LIKE(utc.column_name, '^MONTO') THEN 'MO_' || utc.column_name
        WHEN REGEXP_LIKE(utc.column_name, '^CANTIDAD') THEN 'CA_' || utc.column_name
        WHEN REGEXP_LIKE(utc.column_name, 'PORCENTAJE') THEN 'PC_' || utc.column_name
        -- Fechas por tipo de dato (fallback cuando el nombre no empieza con FECHA_)
        WHEN utc.data_type IN ('DATE') OR utc.data_type LIKE 'TIMESTAMP%' THEN 'FE_' || utc.column_name
        ELSE NULL
    END AS columna_propuesta,
    CASE
        WHEN CASE
                WHEN REGEXP_LIKE(utc.column_name, '^ID_') THEN utc.column_name
                WHEN REGEXP_LIKE(utc.column_name, '^NUMERO_') THEN 'NU_' || SUBSTR(utc.column_name, 8)
                WHEN REGEXP_LIKE(utc.column_name, '^FECHA_') THEN 'FE_' || SUBSTR(utc.column_name, 7)
                WHEN utc.column_name IN ('CREADO_EN', 'MODIFICADO_EN') THEN 'FE_' || utc.column_name
                WHEN utc.column_name = 'CODIGO' THEN 'CO_CODIGO'
                WHEN REGEXP_LIKE(utc.column_name, '_CODIGO$') THEN 'CO_' || utc.column_name
                WHEN utc.column_name IN ('NOMBRE', 'NOMBRE_COMPLETO', 'RAZON_SOCIAL', 'NOMBRES') THEN 'NO_' || utc.column_name
                WHEN REGEXP_LIKE(utc.column_name, '^NOMBRE_') THEN 'NO_' || SUBSTR(utc.column_name, 8)
                WHEN REGEXP_LIKE(utc.column_name, '^APELLIDO_') THEN 'AP_' || SUBSTR(utc.column_name, 10)
                WHEN utc.column_name IN ('DESCRIPCION', 'OBSERVACION', 'OBSERVACIONES', 'COMENTARIO', 'FUNDAMENTO',
                                          'MOTIVO', 'DETALLE_OBSERVACION', 'ASUNTO', 'MENSAJE') THEN 'DE_' || utc.column_name
                WHEN utc.column_name = 'ACTIVO' THEN 'IN_ACTIVO'
                WHEN REGEXP_LIKE(utc.column_name, '^(ES_|REQUIERE_|TIENE_|CUMPLE_|NOTIFICADO$|ARCHIVADO$|CERRADO$)')
                     AND utc.data_type IN ('NUMBER') THEN 'IN_' || utc.column_name
                WHEN REGEXP_LIKE(utc.column_name, '^TIPO_') THEN 'TI_' || SUBSTR(utc.column_name, 6)
                WHEN utc.column_name IN ('CREADO_POR', 'MODIFICADO_POR', 'USERNAME', 'RECIBIDO_POR') THEN 'US_' || utc.column_name
                WHEN utc.column_name IN ('DIRECCION', 'UBICACION') THEN 'DI_' || utc.column_name
                WHEN REGEXP_LIKE(utc.column_name, 'PASSWORD') THEN 'PW_' || utc.column_name
                WHEN REGEXP_LIKE(utc.column_name, '^MONTO') THEN 'MO_' || utc.column_name
                WHEN REGEXP_LIKE(utc.column_name, '^CANTIDAD') THEN 'CA_' || utc.column_name
                WHEN REGEXP_LIKE(utc.column_name, 'PORCENTAJE') THEN 'PC_' || utc.column_name
                WHEN utc.data_type IN ('DATE') OR utc.data_type LIKE 'TIMESTAMP%' THEN 'FE_' || utc.column_name
                ELSE NULL
             END IS NULL
        THEN 'REVISAR MANUALMENTE - sin patron conocido'
        ELSE 'ALTER TABLE ' || utc.table_name || ' RENAME COLUMN ' || utc.column_name || ' TO ' ||
             CASE
                WHEN REGEXP_LIKE(utc.column_name, '^ID_') THEN utc.column_name
                WHEN REGEXP_LIKE(utc.column_name, '^NUMERO_') THEN 'NU_' || SUBSTR(utc.column_name, 8)
                WHEN REGEXP_LIKE(utc.column_name, '^FECHA_') THEN 'FE_' || SUBSTR(utc.column_name, 7)
                WHEN utc.column_name IN ('CREADO_EN', 'MODIFICADO_EN') THEN 'FE_' || utc.column_name
                WHEN utc.column_name = 'CODIGO' THEN 'CO_CODIGO'
                WHEN REGEXP_LIKE(utc.column_name, '_CODIGO$') THEN 'CO_' || utc.column_name
                WHEN utc.column_name IN ('NOMBRE', 'NOMBRE_COMPLETO', 'RAZON_SOCIAL', 'NOMBRES') THEN 'NO_' || utc.column_name
                WHEN REGEXP_LIKE(utc.column_name, '^NOMBRE_') THEN 'NO_' || SUBSTR(utc.column_name, 8)
                WHEN REGEXP_LIKE(utc.column_name, '^APELLIDO_') THEN 'AP_' || SUBSTR(utc.column_name, 10)
                WHEN utc.column_name IN ('DESCRIPCION', 'OBSERVACION', 'OBSERVACIONES', 'COMENTARIO', 'FUNDAMENTO',
                                          'MOTIVO', 'DETALLE_OBSERVACION', 'ASUNTO', 'MENSAJE') THEN 'DE_' || utc.column_name
                WHEN utc.column_name = 'ACTIVO' THEN 'IN_ACTIVO'
                WHEN REGEXP_LIKE(utc.column_name, '^(ES_|REQUIERE_|TIENE_|CUMPLE_|NOTIFICADO$|ARCHIVADO$|CERRADO$)')
                     AND utc.data_type IN ('NUMBER') THEN 'IN_' || utc.column_name
                WHEN REGEXP_LIKE(utc.column_name, '^TIPO_') THEN 'TI_' || SUBSTR(utc.column_name, 6)
                WHEN utc.column_name IN ('CREADO_POR', 'MODIFICADO_POR', 'USERNAME', 'RECIBIDO_POR') THEN 'US_' || utc.column_name
                WHEN utc.column_name IN ('DIRECCION', 'UBICACION') THEN 'DI_' || utc.column_name
                WHEN REGEXP_LIKE(utc.column_name, 'PASSWORD') THEN 'PW_' || utc.column_name
                WHEN REGEXP_LIKE(utc.column_name, '^MONTO') THEN 'MO_' || utc.column_name
                WHEN REGEXP_LIKE(utc.column_name, '^CANTIDAD') THEN 'CA_' || utc.column_name
                WHEN REGEXP_LIKE(utc.column_name, 'PORCENTAJE') THEN 'PC_' || utc.column_name
                WHEN utc.data_type IN ('DATE') OR utc.data_type LIKE 'TIMESTAMP%' THEN 'FE_' || utc.column_name
             END || ';'
    END AS sentencia_o_revision
FROM user_tab_columns utc
JOIN user_tables ut ON ut.table_name = utc.table_name
ORDER BY utc.table_name, utc.column_id;
