/* ============================================================
   SCRIPT 118 - EJECUCION del renombrado GS con vistas de
   compatibilidad (autorizado explicitamente por el usuario,
   26/08/2026). Deja los OBJETOS REALES con nombre GS... y crea
   una vista con el nombre VIEJO por cada tabla, para que V2/V3
   sigan funcionando sin ningun cambio de codigo.

   Orden: 1) tablas  2) columnas (regla ya aprobada)  3) constraints
          4) vistas de compatibilidad (usando la bitacora de 1-2)

   Bitacora propia: GSTL_RENOMBRADO_MAPEO (tipo_objeto, tabla_nueva,
   nombre_viejo, nombre_nuevo) - permite reconstruir exactamente que
   se hizo y armar las vistas de compatibilidad sin adivinar nada.
   ============================================================ */

SET SERVEROUTPUT ON SIZE UNLIMITED

-- 0. Bitacora
BEGIN
  EXECUTE IMMEDIATE 'CREATE TABLE GSTL_RENOMBRADO_MAPEO (
    id_renombrado    NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tipo_objeto      VARCHAR2(20) NOT NULL,
    tabla_nueva      VARCHAR2(128),
    nombre_viejo     VARCHAR2(128) NOT NULL,
    nombre_nuevo     VARCHAR2(128) NOT NULL,
    cambio_real      NUMBER(1) DEFAULT 1,
    creado_en        TIMESTAMP DEFAULT SYSTIMESTAMP
  )';
EXCEPTION
  WHEN OTHERS THEN
    IF SQLCODE = -955 THEN NULL; -- ya existe, re-ejecucion
    ELSE RAISE;
    END IF;
END;
/

DECLARE
  TYPE t_col_rec IS RECORD (tabla VARCHAR2(128), col VARCHAR2(128), tipo VARCHAR2(30));
  TYPE t_col_tab IS TABLE OF t_col_rec;
  v_cols t_col_tab;
  v_nuevo_col VARCHAR2(128);

  TYPE t_con_rec IS RECORD (tabla VARCHAR2(128), nombre VARCHAR2(128));
  TYPE t_con_tab IS TABLE OF t_con_rec;
  v_cons t_con_tab;
  v_bare VARCHAR2(128);
  v_nuevo_con VARCHAR2(128);

  CURSOR c_tablas_gs IS SELECT DISTINCT tabla_nueva FROM GSTL_RENOMBRADO_MAPEO WHERE tipo_objeto = 'TABLA';
  v_sel_list VARCHAR2(32000);
  v_first BOOLEAN;

  PROCEDURE log_reg(p_tipo VARCHAR2, p_tabla VARCHAR2, p_viejo VARCHAR2, p_nuevo VARCHAR2, p_real NUMBER DEFAULT 1) IS
  BEGIN
    INSERT INTO GSTL_RENOMBRADO_MAPEO (tipo_objeto, tabla_nueva, nombre_viejo, nombre_nuevo, cambio_real)
    VALUES (p_tipo, p_tabla, p_viejo, p_nuevo, p_real);
  END;

  PROCEDURE renombrar_tabla(p_actual VARCHAR2, p_nuevo VARCHAR2) IS
    v_existe NUMBER;
  BEGIN
    SELECT COUNT(*) INTO v_existe FROM user_tables WHERE table_name = UPPER(p_actual);
    IF v_existe = 0 THEN
      DBMS_OUTPUT.PUT_LINE('TABLA omitida (no existe / ya renombrada): ' || p_actual);
      RETURN;
    END IF;
    EXECUTE IMMEDIATE 'RENAME ' || p_actual || ' TO ' || p_nuevo;
    log_reg('TABLA', p_nuevo, p_actual, p_nuevo);
    DBMS_OUTPUT.PUT_LINE('TABLA OK: ' || p_actual || ' -> ' || p_nuevo);
  END;

  -- Calcula el nombre nuevo de columna segun la regla ya aprobada (mismo criterio que script 116).
  -- Devuelve NULL si no matchea ningun patron conocido (no se renombra, se deja igual).
  FUNCTION calcular_columna_nueva(p_nombre VARCHAR2, p_tipo_dato VARCHAR2) RETURN VARCHAR2 IS
  BEGIN
    IF REGEXP_LIKE(p_nombre, '^ID_') THEN RETURN p_nombre;
    ELSIF REGEXP_LIKE(p_nombre, '^NUMERO_') THEN RETURN 'NU_' || SUBSTR(p_nombre, 8);
    ELSIF REGEXP_LIKE(p_nombre, '^FECHA_') THEN RETURN 'FE_' || SUBSTR(p_nombre, 7);
    ELSIF p_nombre IN ('CREADO_EN', 'MODIFICADO_EN') THEN RETURN 'FE_' || p_nombre;
    ELSIF p_nombre = 'CODIGO' THEN RETURN 'CO_CODIGO';
    ELSIF REGEXP_LIKE(p_nombre, '_CODIGO$') THEN RETURN 'CO_' || p_nombre;
    ELSIF p_nombre IN ('NOMBRE', 'NOMBRE_COMPLETO', 'RAZON_SOCIAL', 'NOMBRES') THEN RETURN 'NO_' || p_nombre;
    ELSIF REGEXP_LIKE(p_nombre, '^NOMBRE_') THEN RETURN 'NO_' || SUBSTR(p_nombre, 8);
    ELSIF REGEXP_LIKE(p_nombre, '^APELLIDO_') THEN RETURN 'AP_' || SUBSTR(p_nombre, 10);
    ELSIF p_nombre IN ('DESCRIPCION', 'OBSERVACION', 'OBSERVACIONES', 'COMENTARIO', 'FUNDAMENTO',
                        'MOTIVO', 'DETALLE_OBSERVACION', 'ASUNTO', 'MENSAJE') THEN RETURN 'DE_' || p_nombre;
    ELSIF p_nombre = 'ACTIVO' THEN RETURN 'IN_ACTIVO';
    ELSIF REGEXP_LIKE(p_nombre, '^(ES_|REQUIERE_|TIENE_|CUMPLE_|NOTIFICADO$|ARCHIVADO$|CERRADO$)')
          AND p_tipo_dato = 'NUMBER' THEN RETURN 'IN_' || p_nombre;
    ELSIF REGEXP_LIKE(p_nombre, '^TIPO_') THEN RETURN 'TI_' || SUBSTR(p_nombre, 6);
    ELSIF p_nombre IN ('CREADO_POR', 'MODIFICADO_POR', 'USERNAME', 'RECIBIDO_POR') THEN RETURN 'US_' || p_nombre;
    ELSIF p_nombre IN ('DIRECCION', 'UBICACION') THEN RETURN 'DI_' || p_nombre;
    ELSIF REGEXP_LIKE(p_nombre, 'PASSWORD') THEN RETURN 'PW_' || p_nombre;
    ELSIF REGEXP_LIKE(p_nombre, '^MONTO') THEN RETURN 'MO_' || p_nombre;
    ELSIF REGEXP_LIKE(p_nombre, '^CANTIDAD') THEN RETURN 'CA_' || p_nombre;
    ELSIF REGEXP_LIKE(p_nombre, 'PORCENTAJE') THEN RETURN 'PC_' || p_nombre;
    ELSIF p_tipo_dato = 'DATE' OR p_tipo_dato LIKE 'TIMESTAMP%' THEN RETURN 'FE_' || p_nombre;
    ELSE RETURN NULL;
    END IF;
  END;
BEGIN
  ----------------------------------------------------------------
  -- 1. TABLAS (72)
  ----------------------------------------------------------------
  renombrar_tabla('AREA', 'GSTM_AREA');
  renombrar_tabla('CANAL_RECEPCION', 'GSTM_CANAL_RECEPCION');
  renombrar_tabla('CENTROS_ATENCION', 'GSTM_CENTROS_ATENCION');
  renombrar_tabla('ENTIDAD_EXTERNA', 'GSTM_ENTIDAD_EXTERNA');
  renombrar_tabla('EQUIPO', 'GSTM_EQUIPO');
  renombrar_tabla('ESTADO_CARGO_ACUSE', 'GSTM_ESTADO_CARGO_ACUSE');
  renombrar_tabla('ESTADO_DOCUMENTO', 'GSTM_ESTADO_DOCUMENTO');
  renombrar_tabla('ESTADO_EXPEDIENTE', 'GSTM_ESTADO_EXPEDIENTE');
  renombrar_tabla('ESTADO_NOTIFICACION', 'GSTM_ESTADO_NOTIFICACION');
  renombrar_tabla('ETAPA_EXPEDIENTE', 'GSTM_ETAPA_EXPEDIENTE');
  renombrar_tabla('FERIADO_NACIONAL', 'GSTM_FERIADO_NACIONAL');
  renombrar_tabla('FLUJO', 'GSTM_FLUJO');
  renombrar_tabla('FLUJO_TRANSICION', 'GSTM_FLUJO_TRANSICION');
  renombrar_tabla('MOTIVO_ARCHIVO', 'GSTM_MOTIVO_ARCHIVO');
  renombrar_tabla('MOTIVO_CORRECCION', 'GSTM_MOTIVO_CORRECCION');
  renombrar_tabla('MOTIVO_NO_CORRESPONDE', 'GSTM_MOTIVO_NO_CORRESPONDE');
  renombrar_tabla('MUNICIPALIDADES', 'GSTM_MUNICIPALIDADES');
  renombrar_tabla('PERMISO', 'GSTM_PERMISO');
  renombrar_tabla('PERSONA', 'GSTM_PERSONA');
  renombrar_tabla('PLAZO_CONFIGURACION', 'GSTM_PLAZO_CONFIGURACION');
  renombrar_tabla('PROCEDIMIENTO_REGISTRAL', 'GSTM_PROCEDIMIENTO_REGISTRAL');
  renombrar_tabla('ROL', 'GSTM_ROL');
  renombrar_tabla('TIPO_ACTA', 'GSTM_TIPO_ACTA');
  renombrar_tabla('TIPO_DOCUMENTO', 'GSTM_TIPO_DOCUMENTO');
  renombrar_tabla('TIPO_DOCUMENTO_ADJUNTO', 'GSTM_TIPO_DOCUMENTO_ADJUNTO');
  renombrar_tabla('TIPO_MOVIMIENTO', 'GSTM_TIPO_MOVIMIENTO');
  renombrar_tabla('TIPO_NOTIFICACION', 'GSTM_TIPO_NOTIFICACION');
  renombrar_tabla('TIPO_OBSERVACION', 'GSTM_TIPO_OBSERVACION');
  renombrar_tabla('TIPO_RESOLUCION', 'GSTM_TIPO_RESOLUCION');
  renombrar_tabla('TIPO_RESULTADO_EJECUCION', 'GSTM_TIPO_RESULTADO_EJECUCION');
  renombrar_tabla('TIPO_RESULTADO_EVALUACION', 'GSTM_TIPO_RESULTADO_EVALUACION');
  renombrar_tabla('UBIGEO_DEPARTAMENTO', 'GSTM_UBIGEO_DEPARTAMENTO');
  renombrar_tabla('UBIGEO_DISTRITO', 'GSTM_UBIGEO_DISTRITO');
  renombrar_tabla('UBIGEO_PROVINCIA', 'GSTM_UBIGEO_PROVINCIA');
  renombrar_tabla('UNIDADES_ORGANICAS', 'GSTM_UNIDADES_ORGANICAS');
  renombrar_tabla('USUARIO', 'GSTM_USUARIO');
  renombrar_tabla('EXPEDIENTE', 'GSTC_EXPEDIENTE');
  renombrar_tabla('EXPEDIENTE_ACTA', 'GSTD_EXPEDIENTE_ACTA');
  renombrar_tabla('EXPEDIENTE_ALERTA', 'GSTD_EXPEDIENTE_ALERTA');
  renombrar_tabla('EXPEDIENTE_ANALISIS', 'GSTD_EXPEDIENTE_ANALISIS');
  renombrar_tabla('EXPEDIENTE_ASIGNACION', 'GSTD_EXPEDIENTE_ASIGNACION');
  renombrar_tabla('EXPEDIENTE_CARGO_ACUSE', 'GSTD_EXPEDIENTE_CARGO_ACUSE');
  renombrar_tabla('EXPEDIENTE_DERIVACION_EXTERNA', 'GSTD_EXPEDIENTE_DERIVACION_EXTERNA');
  renombrar_tabla('EXPEDIENTE_DIGITAL', 'GSTD_EXPEDIENTE_DIGITAL');
  renombrar_tabla('EXPEDIENTE_DOCUMENTO', 'GSTD_EXPEDIENTE_DOCUMENTO');
  renombrar_tabla('EXPEDIENTE_DOCUMENTO_ANALIZADO', 'GSTD_EXPEDIENTE_DOCUMENTO_ANALIZADO');
  renombrar_tabla('EXPEDIENTE_EVALUACION', 'GSTD_EXPEDIENTE_EVALUACION');
  renombrar_tabla('EXPEDIENTE_NOTIFICACION', 'GSTD_EXPEDIENTE_NOTIFICACION');
  renombrar_tabla('EXPEDIENTE_OBSERVACION', 'GSTD_EXPEDIENTE_OBSERVACION');
  renombrar_tabla('EXPEDIENTE_PERSONA', 'GSTD_EXPEDIENTE_PERSONA');
  renombrar_tabla('EXPEDIENTE_PUBLICACION', 'GSTD_EXPEDIENTE_PUBLICACION');
  renombrar_tabla('EXPEDIENTE_RELACION', 'GSTD_EXPEDIENTE_RELACION');
  renombrar_tabla('EXPEDIENTE_RESOLUCION', 'GSTD_EXPEDIENTE_RESOLUCION');
  renombrar_tabla('EXPEDIENTE_SOLICITUD', 'GSTD_EXPEDIENTE_SOLICITUD');
  renombrar_tabla('FLUJO_TRANSICION_EQUIPO', 'GSTD_FLUJO_TRANSICION_EQUIPO');
  renombrar_tabla('FLUJO_TRANSICION_ROL', 'GSTD_FLUJO_TRANSICION_ROL');
  renombrar_tabla('PLANTILLA_BLOQUE', 'GSTD_PLANTILLA_BLOQUE');
  renombrar_tabla('ROL_PERMISO', 'GSTD_ROL_PERMISO');
  renombrar_tabla('EQUIPO_USUARIO', 'GSTD_EQUIPO_USUARIO');
  renombrar_tabla('USUARIO_EMAIL_OTP', 'GSTD_USUARIO_EMAIL_OTP');
  renombrar_tabla('USUARIO_ROL', 'GSTD_USUARIO_ROL');
  renombrar_tabla('USUARIO_SUPERVISION', 'GSTD_USUARIO_SUPERVISION');
  renombrar_tabla('USUARIO_TOTP_BACKUP_CODE', 'GSTD_USUARIO_TOTP_BACKUP_CODE');
  renombrar_tabla('AUDITORIA_EVENTO', 'GSTL_AUDITORIA_EVENTO');
  renombrar_tabla('CARGA_INICIAL_LOG', 'GSTL_CARGA_INICIAL_LOG');
  renombrar_tabla('EXPEDIENTE_HISTORIAL', 'GSTL_EXPEDIENTE_HISTORIAL');
  renombrar_tabla('EXPEDIENTE_METRICA_ETAPA', 'GSTX_EXPEDIENTE_METRICA_ETAPA');
  renombrar_tabla('EXPEDIENTE_PLAZO', 'GSTX_EXPEDIENTE_PLAZO');
  renombrar_tabla('GRUPO_FAMILIAR', 'GSTX_GRUPO_FAMILIAR');
  renombrar_tabla('LEGACY_CATALOGO_MAP', 'GSTX_LEGACY_CATALOGO_MAP');
  renombrar_tabla('LEGACY_ESTADO_MAP', 'GSTX_LEGACY_ESTADO_MAP');
  renombrar_tabla('PLANTILLA_DOCUMENTO', 'GSTX_PLANTILLA_DOCUMENTO');

  DBMS_OUTPUT.PUT_LINE('--- Paso 1 (tablas) finalizado ---');

  ----------------------------------------------------------------
  -- 2. COLUMNAS - por cada tabla ya renombrada (GST%), bulk collect
  --    de sus columnas ANTES de alterar, para no depender de un
  --    cursor sobre un diccionario que va cambiando en el loop.
  ----------------------------------------------------------------
  SELECT table_name, column_name, data_type
    BULK COLLECT INTO v_cols
    FROM user_tab_columns
   WHERE table_name LIKE 'GST%'
     AND table_name != 'GSTL_RENOMBRADO_MAPEO'  -- nunca procesar la propia bitacora (bug real ya corregido)
   ORDER BY table_name, column_id;

  FOR i IN 1 .. v_cols.COUNT LOOP
    v_nuevo_col := calcular_columna_nueva(v_cols(i).col, v_cols(i).tipo);
    IF v_nuevo_col IS NULL OR v_nuevo_col = v_cols(i).col THEN
      log_reg('COLUMNA', v_cols(i).tabla, v_cols(i).col, v_cols(i).col, 0);
    ELSE
      BEGIN
        EXECUTE IMMEDIATE 'ALTER TABLE ' || v_cols(i).tabla || ' RENAME COLUMN ' || v_cols(i).col || ' TO ' || v_nuevo_col;
        log_reg('COLUMNA', v_cols(i).tabla, v_cols(i).col, v_nuevo_col, 1);
      EXCEPTION
        WHEN OTHERS THEN
          DBMS_OUTPUT.PUT_LINE('ERROR columna ' || v_cols(i).tabla || '.' || v_cols(i).col || ': ' || SQLERRM);
          log_reg('COLUMNA', v_cols(i).tabla, v_cols(i).col, v_cols(i).col, 0);
      END;
    END IF;
  END LOOP;

  DBMS_OUTPUT.PUT_LINE('--- Paso 2 (columnas) finalizado: ' || v_cols.COUNT || ' procesadas ---');

  ----------------------------------------------------------------
  -- 3. CONSTRAINTS con nombre propio (PK/UK/FK/CHECK-no-sistema)
  ----------------------------------------------------------------
  SELECT table_name, constraint_name
    BULK COLLECT INTO v_cons
    FROM user_constraints
   WHERE table_name LIKE 'GST%'
     AND table_name != 'GSTL_RENOMBRADO_MAPEO'  -- nunca procesar la propia bitacora
     AND (constraint_type IN ('P', 'R', 'U')
          OR (constraint_type = 'C' AND constraint_name NOT LIKE 'SYS_C%'));

  FOR i IN 1 .. v_cons.COUNT LOOP
    v_bare := SUBSTR(v_cons(i).tabla, 6); -- quita 'GST<letra>_' (5 chars)
    IF INSTR(v_cons(i).nombre, v_bare) = 0 THEN
      DBMS_OUTPUT.PUT_LINE('CONSTRAINT sin match (abreviatura), sin cambio: ' || v_cons(i).nombre);
      log_reg('CONSTRAINT', v_cons(i).tabla, v_cons(i).nombre, v_cons(i).nombre, 0);
    ELSE
      v_nuevo_con := REPLACE(v_cons(i).nombre, v_bare, v_cons(i).tabla);
      BEGIN
        EXECUTE IMMEDIATE 'ALTER TABLE ' || v_cons(i).tabla || ' RENAME CONSTRAINT ' || v_cons(i).nombre || ' TO ' || v_nuevo_con;
        log_reg('CONSTRAINT', v_cons(i).tabla, v_cons(i).nombre, v_nuevo_con, 1);
      EXCEPTION
        WHEN OTHERS THEN
          DBMS_OUTPUT.PUT_LINE('ERROR constraint ' || v_cons(i).nombre || ': ' || SQLERRM);
          log_reg('CONSTRAINT', v_cons(i).tabla, v_cons(i).nombre, v_cons(i).nombre, 0);
      END;
    END IF;
  END LOOP;

  DBMS_OUTPUT.PUT_LINE('--- Paso 3 (constraints) finalizado: ' || v_cons.COUNT || ' procesados ---');

  ----------------------------------------------------------------
  -- 4. VISTAS DE COMPATIBILIDAD: 1 por tabla, con el nombre VIEJO,
  --    columnas re-alias a su nombre viejo.
  ----------------------------------------------------------------
  FOR t IN c_tablas_gs LOOP
    DECLARE
      v_tabla_vieja VARCHAR2(128);
    BEGIN
      SELECT nombre_viejo INTO v_tabla_vieja
        FROM GSTL_RENOMBRADO_MAPEO
       WHERE tipo_objeto = 'TABLA' AND tabla_nueva = t.tabla_nueva
         AND ROWNUM = 1;

      v_sel_list := NULL;
      v_first := TRUE;
      FOR c IN (SELECT nombre_nuevo, nombre_viejo FROM GSTL_RENOMBRADO_MAPEO
                 WHERE tipo_objeto = 'COLUMNA' AND tabla_nueva = t.tabla_nueva
                 ORDER BY id_renombrado) LOOP
        IF NOT v_first THEN v_sel_list := v_sel_list || ', '; END IF;
        v_sel_list := v_sel_list || c.nombre_nuevo || ' AS ' || c.nombre_viejo;
        v_first := FALSE;
      END LOOP;

      IF v_sel_list IS NOT NULL THEN
        EXECUTE IMMEDIATE 'CREATE OR REPLACE VIEW ' || v_tabla_vieja || ' AS SELECT ' || v_sel_list || ' FROM ' || t.tabla_nueva;
        DBMS_OUTPUT.PUT_LINE('VISTA compat OK: ' || v_tabla_vieja || ' -> ' || t.tabla_nueva);
      ELSE
        DBMS_OUTPUT.PUT_LINE('VISTA compat OMITIDA (sin columnas mapeadas): ' || v_tabla_vieja);
      END IF;
    EXCEPTION
      WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('ERROR vista compat ' || t.tabla_nueva || ': ' || SQLERRM);
    END;
  END LOOP;

  DBMS_OUTPUT.PUT_LINE('--- Paso 4 (vistas de compatibilidad) finalizado ---');
END;
/
