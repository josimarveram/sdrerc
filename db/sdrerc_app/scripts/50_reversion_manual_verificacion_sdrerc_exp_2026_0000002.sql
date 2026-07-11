/* ============================================================
   SCRIPT 50 - Reversion puntual: volver a Verificacion / En verificacion
   Expediente: SDRERC-EXP-2026-000002

   Contexto: correccion manual para pruebas. Revierte el expediente a
   VERIFICACION/EN_VERIFICACION sin importar en que etapa/estado se
   encuentre actualmente (por ejemplo si quedo en EJECUCION/EN_EJECUCION,
   VERIFICACION/VERIFICADO o ANALISIS/OBSERVADO tras una prueba).

   No modifica id_usuario_responsable_actual ni id_equipo_responsable_actual.
   Registra el movimiento en EXPEDIENTE_HISTORIAL para trazabilidad
   (tipo_movimiento REENVIO_VERIFICACION, ya existente en el catalogo).

   Ejecutar conectado como SDRERC_APP cuando se autorice.
   Idempotente: si el expediente ya esta en VERIFICACION/EN_VERIFICACION,
   no hace ningun cambio.
   No ejecutar automaticamente: requiere instruccion explicita aparte.
   ============================================================ */

DECLARE
  v_numero_expediente     VARCHAR2(60) := 'SDRERC-EXP-2026-000002';
  v_id_expediente          NUMBER;
  v_etapa_actual           NUMBER;
  v_estado_actual          NUMBER;
  v_id_etapa_verificacion  NUMBER;
  v_id_estado_en_verif     NUMBER;
  v_id_tipo_movimiento     NUMBER;
BEGIN
  SELECT id_expediente, id_etapa_actual, id_estado_actual
    INTO v_id_expediente, v_etapa_actual, v_estado_actual
    FROM expediente
   WHERE numero_expediente = v_numero_expediente
     AND activo = 1;

  SELECT id_etapa INTO v_id_etapa_verificacion FROM etapa_expediente WHERE codigo = 'VERIFICACION';
  SELECT id_estado INTO v_id_estado_en_verif FROM estado_expediente WHERE codigo = 'EN_VERIFICACION';
  SELECT id_tipo_movimiento INTO v_id_tipo_movimiento FROM tipo_movimiento WHERE codigo = 'REENVIO_VERIFICACION';

  IF v_etapa_actual = v_id_etapa_verificacion AND v_estado_actual = v_id_estado_en_verif THEN
    DBMS_OUTPUT.PUT_LINE(
      'Sin cambios: el expediente ' || v_numero_expediente ||
      ' ya esta en VERIFICACION/EN_VERIFICACION.');
  ELSE
    INSERT INTO expediente_historial (
      id_expediente, id_tipo_movimiento, fecha_movimiento,
      id_etapa_origen, id_estado_origen, id_etapa_destino, id_estado_destino,
      comentario, motivo, activo, creado_en
    ) VALUES (
      v_id_expediente, v_id_tipo_movimiento, SYSTIMESTAMP,
      v_etapa_actual, v_estado_actual, v_id_etapa_verificacion, v_id_estado_en_verif,
      'Reversion manual para pruebas: se regresa el expediente a Verificacion / En verificacion.',
      'REVERSION_MANUAL_VERIFICACION', 1, SYSTIMESTAMP
    );

    UPDATE expediente
       SET id_etapa_actual = v_id_etapa_verificacion,
           id_estado_actual = v_id_estado_en_verif,
           fecha_ultimo_movimiento = SYSTIMESTAMP,
           modificado_en = SYSTIMESTAMP
     WHERE id_expediente = v_id_expediente;

    COMMIT;
    DBMS_OUTPUT.PUT_LINE(
      'Expediente ' || v_numero_expediente || ' revertido a VERIFICACION/EN_VERIFICACION correctamente.');
  END IF;
END;


/* ============================================================
   Verificacion posterior
   ============================================================ */

SELECT e.numero_expediente, et.codigo AS etapa_actual, es.codigo AS estado_actual,
       e.fecha_ultimo_movimiento
  FROM expediente e
  JOIN etapa_expediente et ON et.id_etapa = e.id_etapa_actual
  JOIN estado_expediente es ON es.id_estado = e.id_estado_actual
 WHERE e.numero_expediente = 'SDRERC-EXP-2026-000002';

SELECT eh.fecha_movimiento, tm.codigo AS movimiento,
       eo.codigo AS etapa_origen, so.codigo AS estado_origen,
       ed.codigo AS etapa_destino, sd.codigo AS estado_destino,
       eh.comentario
  FROM expediente_historial eh
  JOIN expediente e ON e.id_expediente = eh.id_expediente
  JOIN tipo_movimiento tm ON tm.id_tipo_movimiento = eh.id_tipo_movimiento
  LEFT JOIN etapa_expediente eo ON eo.id_etapa = eh.id_etapa_origen
  LEFT JOIN estado_expediente so ON so.id_estado = eh.id_estado_origen
  JOIN etapa_expediente ed ON ed.id_etapa = eh.id_etapa_destino
  JOIN estado_expediente sd ON sd.id_estado = eh.id_estado_destino
 WHERE e.numero_expediente = 'SDRERC-EXP-2026-000002'
 ORDER BY eh.fecha_movimiento DESC;
