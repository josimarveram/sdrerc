/* ============================================================
   SCRIPT 30 - Datos maestros de plazos por procedimiento
   Ejecutar conectado como SDRERC_APP cuando se valide la configuracion.
   Script idempotente, no destructivo y sin datos transaccionales.
   No recalcula expedientes historicos.

   Plazos funcionales:
   - Rectificacion administrativa: 30 dias habiles.
   - Reconsideracion: 15 dias habiles.
   - Apelacion: 30 dias habiles.
   ============================================================ */

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
   WHERE UPPER(codigo) = 'SOLICITUD_RECTIFICACION_ADMINISTRATIVA';

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
      'SOLICITUD_RECTIFICACION_ADMINISTRATIVA',
      'Plazo de rectificacion administrativa',
      'SOLICITUD_RECTIFICACION_ADMINISTRATIVA',
      v_id_etapa_registro,
      30,
      'HABILES',
      DATE '2026-01-01',
      1,
      'Configuracion oficial: 30 dias habiles. No recalcula expedientes historicos.',
      SYSTIMESTAMP
    );
  ELSE
    UPDATE plazo_configuracion
       SET nombre = 'Plazo de rectificacion administrativa',
           ambito = 'SOLICITUD_RECTIFICACION_ADMINISTRATIVA',
           id_etapa = NVL(id_etapa, v_id_etapa_registro),
           dias_plazo = 30,
           unidad_plazo = 'HABILES',
           fecha_vigencia_desde = NVL(fecha_vigencia_desde, DATE '2026-01-01'),
           activo = 1,
           observacion = NVL(observacion, 'Configuracion oficial: 30 dias habiles. No recalcula expedientes historicos.'),
           modificado_en = SYSTIMESTAMP
     WHERE UPPER(codigo) = 'SOLICITUD_RECTIFICACION_ADMINISTRATIVA';
  END IF;
END;


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
   WHERE UPPER(codigo) = 'SOLICITUD_RECONSIDERACION';

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
      'SOLICITUD_RECONSIDERACION',
      'Plazo de reconsideracion',
      'SOLICITUD_RECONSIDERACION',
      v_id_etapa_registro,
      15,
      'HABILES',
      DATE '2026-01-01',
      1,
      'Configuracion oficial: 15 dias habiles. No recalcula expedientes historicos.',
      SYSTIMESTAMP
    );
  ELSE
    UPDATE plazo_configuracion
       SET nombre = 'Plazo de reconsideracion',
           ambito = 'SOLICITUD_RECONSIDERACION',
           id_etapa = NVL(id_etapa, v_id_etapa_registro),
           dias_plazo = 15,
           unidad_plazo = 'HABILES',
           fecha_vigencia_desde = NVL(fecha_vigencia_desde, DATE '2026-01-01'),
           activo = 1,
           observacion = NVL(observacion, 'Configuracion oficial: 15 dias habiles. No recalcula expedientes historicos.'),
           modificado_en = SYSTIMESTAMP
     WHERE UPPER(codigo) = 'SOLICITUD_RECONSIDERACION';
  END IF;
END;


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
   WHERE UPPER(codigo) = 'SOLICITUD_APELACION';

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
      'SOLICITUD_APELACION',
      'Plazo de apelacion',
      'SOLICITUD_APELACION',
      v_id_etapa_registro,
      30,
      'HABILES',
      DATE '2026-01-01',
      1,
      'Configuracion oficial: 30 dias habiles. No recalcula expedientes historicos.',
      SYSTIMESTAMP
    );
  ELSE
    UPDATE plazo_configuracion
       SET nombre = 'Plazo de apelacion',
           ambito = 'SOLICITUD_APELACION',
           id_etapa = NVL(id_etapa, v_id_etapa_registro),
           dias_plazo = 30,
           unidad_plazo = 'HABILES',
           fecha_vigencia_desde = NVL(fecha_vigencia_desde, DATE '2026-01-01'),
           activo = 1,
           observacion = NVL(observacion, 'Configuracion oficial: 30 dias habiles. No recalcula expedientes historicos.'),
           modificado_en = SYSTIMESTAMP
     WHERE UPPER(codigo) = 'SOLICITUD_APELACION';
  END IF;
END;


SELECT codigo,
       nombre,
       ambito,
       dias_plazo,
       unidad_plazo,
       activo,
       fecha_vigencia_desde,
       fecha_vigencia_hasta
  FROM plazo_configuracion
 WHERE UPPER(codigo) IN (
       'SOLICITUD_RECTIFICACION_ADMINISTRATIVA',
       'SOLICITUD_RECONSIDERACION',
       'SOLICITUD_APELACION')
 ORDER BY codigo;
