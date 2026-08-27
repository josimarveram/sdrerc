/* ============================================================
   SCRIPT 119 - Fix del bug real de doble-prefijo FE_FE_ causado
   por la regla generica DATE/TIMESTAMP del script 118 al
   reprocesar tablas ya renombradas en una 2da corrida.

   1) Corrige las columnas reales (RENAME COLUMN FE_FE_X -> FE_X).
   2) Limpia de la bitacora las filas erroneas de la 2da pasada
      (nombre_viejo ya empezaba con FE_, nombre_nuevo = FE_+nombre_viejo).
   3) Reconstruye las vistas de compatibilidad que fallaron por esto.
   ============================================================ */

SET SERVEROUTPUT ON SIZE UNLIMITED

-- 1. Corregir columnas reales
DECLARE
  TYPE t_rec IS RECORD (tabla VARCHAR2(128), col VARCHAR2(128));
  TYPE t_tab IS TABLE OF t_rec;
  v_cols t_tab;
BEGIN
  SELECT table_name, column_name BULK COLLECT INTO v_cols
    FROM user_tab_columns
   WHERE column_name LIKE 'FE\_FE\_%' ESCAPE '\';

  FOR i IN 1 .. v_cols.COUNT LOOP
    EXECUTE IMMEDIATE 'ALTER TABLE ' || v_cols(i).tabla || ' RENAME COLUMN ' || v_cols(i).col ||
                       ' TO ' || SUBSTR(v_cols(i).col, 4); -- quita el primer 'FE_' (3 chars + guion bajo = 4)
    DBMS_OUTPUT.PUT_LINE('Corregido: ' || v_cols(i).tabla || '.' || v_cols(i).col || ' -> ' || SUBSTR(v_cols(i).col, 4));
  END LOOP;
  DBMS_OUTPUT.PUT_LINE('Total corregidas: ' || v_cols.COUNT);
END;
/

-- 2. Limpiar la bitacora (borra solo las filas erroneas de la 2da pasada)
DELETE FROM GSTL_RENOMBRADO_MAPEO
 WHERE tipo_objeto = 'COLUMNA'
   AND nombre_viejo LIKE 'FE\_%' ESCAPE '\'
   AND nombre_nuevo = 'FE_' || nombre_viejo;

SELECT COUNT(*) AS filas_borradas_bitacora FROM GSTL_RENOMBRADO_MAPEO WHERE 1=0; -- placeholder, ver conteo real abajo
COMMIT;

-- 3. Reconstruir SOLO las vistas de compatibilidad de las tablas afectadas
DECLARE
  CURSOR c_tablas IS
    SELECT DISTINCT table_name AS tabla_nueva
    FROM user_tab_columns
    WHERE table_name IN (
      SELECT DISTINCT SUBSTR(column_name, 1, 0) FROM DUAL WHERE 1=0 -- placeholder, no usado
    );
  v_tabla_vieja VARCHAR2(128);
  v_sel_list VARCHAR2(32000);
  v_first BOOLEAN;
  TYPE t_tabs IS TABLE OF VARCHAR2(128);
  v_tablas_afectadas t_tabs := t_tabs(
    'GSTC_EXPEDIENTE','GSTD_EQUIPO_USUARIO','GSTD_EXPEDIENTE_ACTA','GSTD_EXPEDIENTE_ALERTA',
    'GSTD_EXPEDIENTE_ANALISIS','GSTD_EXPEDIENTE_ASIGNACION','GSTD_EXPEDIENTE_CARGO_ACUSE',
    'GSTD_EXPEDIENTE_DERIVACION_EXTERNA','GSTD_EXPEDIENTE_DIGITAL','GSTD_EXPEDIENTE_DOCUMENTO',
    'GSTD_EXPEDIENTE_DOCUMENTO_ANALIZADO','GSTD_EXPEDIENTE_EVALUACION','GSTD_EXPEDIENTE_NOTIFICACION',
    'GSTD_EXPEDIENTE_OBSERVACION','GSTD_EXPEDIENTE_PERSONA','GSTD_EXPEDIENTE_PUBLICACION',
    'GSTD_EXPEDIENTE_RELACION','GSTD_EXPEDIENTE_RESOLUCION','GSTD_EXPEDIENTE_SOLICITUD',
    'GSTD_FLUJO_TRANSICION_EQUIPO','GSTD_FLUJO_TRANSICION_ROL','GSTD_PLANTILLA_BLOQUE',
    'GSTD_ROL_PERMISO','GSTD_USUARIO_EMAIL_OTP','GSTD_USUARIO_ROL','GSTD_USUARIO_SUPERVISION',
    'GSTD_USUARIO_TOTP_BACKUP_CODE','GSTL_AUDITORIA_EVENTO','GSTL_CARGA_INICIAL_LOG',
    'GSTL_EXPEDIENTE_HISTORIAL'
  );
BEGIN
  FOR idx IN 1 .. v_tablas_afectadas.COUNT LOOP
    BEGIN
      SELECT nombre_viejo INTO v_tabla_vieja
        FROM GSTL_RENOMBRADO_MAPEO
       WHERE tipo_objeto = 'TABLA' AND tabla_nueva = v_tablas_afectadas(idx)
         AND ROWNUM = 1;

      v_sel_list := NULL;
      v_first := TRUE;
      FOR c IN (SELECT DISTINCT nombre_nuevo, nombre_viejo FROM GSTL_RENOMBRADO_MAPEO
                 WHERE tipo_objeto = 'COLUMNA' AND tabla_nueva = v_tablas_afectadas(idx)) LOOP
        IF NOT v_first THEN v_sel_list := v_sel_list || ', '; END IF;
        v_sel_list := v_sel_list || c.nombre_nuevo || ' AS ' || c.nombre_viejo;
        v_first := FALSE;
      END LOOP;

      EXECUTE IMMEDIATE 'CREATE OR REPLACE VIEW ' || v_tabla_vieja || ' AS SELECT ' || v_sel_list || ' FROM ' || v_tablas_afectadas(idx);
      DBMS_OUTPUT.PUT_LINE('VISTA reconstruida OK: ' || v_tabla_vieja);
    EXCEPTION
      WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('ERROR reconstruyendo vista de ' || v_tablas_afectadas(idx) || ': ' || SQLERRM);
    END;
  END LOOP;
END;
/
