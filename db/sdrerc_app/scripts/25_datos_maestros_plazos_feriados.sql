/* ============================================================
   SCRIPT 25 - Datos maestros de plazos y feriados
   Ejecutar conectado como SDRERC_APP cuando se valide la configuracion.
   Script idempotente, no destructivo y sin datos transaccionales.
   No recalcula expedientes historicos.
   ============================================================ */

DECLARE
  v_table_count NUMBER;
  v_column_count NUMBER;

  PROCEDURE add_column_if_missing(p_column_name VARCHAR2, p_definition VARCHAR2) IS
  BEGIN
    SELECT COUNT(*)
      INTO v_column_count
      FROM user_tab_cols
     WHERE table_name = 'PLAZO_CONFIGURACION'
       AND column_name = UPPER(p_column_name);

    IF v_column_count = 0 THEN
      EXECUTE IMMEDIATE 'ALTER TABLE plazo_configuracion ADD ' || p_definition;
    END IF;
  END;
BEGIN
  SELECT COUNT(*) INTO v_table_count FROM user_tables WHERE table_name = 'PLAZO_CONFIGURACION';

  IF v_table_count > 0 THEN
    add_column_if_missing('CODIGO', 'codigo VARCHAR2(80)');
    add_column_if_missing('NOMBRE', 'nombre VARCHAR2(180)');
    add_column_if_missing('AMBITO', 'ambito VARCHAR2(80)');
    add_column_if_missing('UNIDAD_PLAZO', 'unidad_plazo VARCHAR2(20) DEFAULT ''HABILES'' NOT NULL');
    add_column_if_missing('FECHA_VIGENCIA_DESDE', 'fecha_vigencia_desde DATE');
    add_column_if_missing('FECHA_VIGENCIA_HASTA', 'fecha_vigencia_hasta DATE');
    add_column_if_missing('OBSERVACION', 'observacion VARCHAR2(300)');
  END IF;
END;
/

DECLARE
  v_count NUMBER;
BEGIN
  SELECT COUNT(*) INTO v_count FROM user_constraints WHERE constraint_name = 'CK_PLAZO_CONFIG_UNIDAD';
  IF v_count = 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE plazo_configuracion ADD CONSTRAINT ck_plazo_config_unidad CHECK (unidad_plazo IN (''HABILES'', ''CALENDARIO''))';
  END IF;
EXCEPTION
  WHEN OTHERS THEN
    IF SQLCODE != -942 THEN
      RAISE;
    END IF;
END;
/

DECLARE
  v_count NUMBER;
BEGIN
  SELECT COUNT(*) INTO v_count FROM user_constraints WHERE constraint_name = 'CK_PLAZO_CONFIG_ACTIVO';
  IF v_count = 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE plazo_configuracion ADD CONSTRAINT ck_plazo_config_activo CHECK (activo IN (0, 1))';
  END IF;
EXCEPTION
  WHEN OTHERS THEN
    IF SQLCODE != -942 THEN
      RAISE;
    END IF;
END;
/

DECLARE
  v_count NUMBER;
BEGIN
  SELECT COUNT(*) INTO v_count FROM user_indexes WHERE index_name = 'IX_PLAZO_CONFIG_CODIGO';
  IF v_count = 0 THEN
    EXECUTE IMMEDIATE 'CREATE INDEX ix_plazo_config_codigo ON plazo_configuracion (codigo, activo)';
  END IF;
EXCEPTION
  WHEN OTHERS THEN
    IF SQLCODE != -942 THEN
      RAISE;
    END IF;
END;
/

DECLARE
  v_id_etapa_registro NUMBER;
  v_count NUMBER;
BEGIN
  SELECT MAX(id_etapa)
    INTO v_id_etapa_registro
    FROM etapa_expediente
   WHERE UPPER(codigo) = 'REGISTRO'
     AND activo = 1;

  SELECT COUNT(*)
    INTO v_count
    FROM plazo_configuracion
   WHERE UPPER(codigo) = 'SOLICITUD_SDRERC';

  IF v_count = 0 THEN
    INSERT INTO plazo_configuracion (
      codigo,
      nombre,
      ambito,
      id_etapa,
      dias_plazo,
      unidad_plazo,
      fecha_vigencia_desde,
      activo,
      observacion,
      creado_en
    ) VALUES (
      'SOLICITUD_SDRERC',
      'Plazo de atención de solicitudes SDRERC',
      'SOLICITUD_SDRERC',
      v_id_etapa_registro,
      30,
      'HABILES',
      DATE '2026-01-01',
      1,
      'Configuración oficial inicial: 30 días hábiles. No recalcula expedientes históricos.',
      SYSTIMESTAMP
    );
  ELSE
    UPDATE plazo_configuracion
       SET nombre = 'Plazo de atención de solicitudes SDRERC',
           ambito = 'SOLICITUD_SDRERC',
           id_etapa = NVL(id_etapa, v_id_etapa_registro),
           dias_plazo = 30,
           unidad_plazo = 'HABILES',
           fecha_vigencia_desde = NVL(fecha_vigencia_desde, DATE '2026-01-01'),
           activo = 1,
           observacion = NVL(observacion, 'Configuración oficial inicial: 30 días hábiles. No recalcula expedientes históricos.'),
           modificado_en = SYSTIMESTAMP
     WHERE UPPER(codigo) = 'SOLICITUD_SDRERC';
  END IF;
END;
/

SELECT codigo,
       nombre,
       ambito,
       dias_plazo,
       unidad_plazo,
       activo,
       fecha_vigencia_desde,
       fecha_vigencia_hasta
  FROM plazo_configuracion
 WHERE UPPER(codigo) = 'SOLICITUD_SDRERC';

SELECT COUNT(*) AS feriados_registrados
  FROM feriado_nacional;
