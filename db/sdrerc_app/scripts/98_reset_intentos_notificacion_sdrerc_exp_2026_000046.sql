/* ============================================================
   SCRIPT 98 - Reset puntual de intentos de notificacion e historial
   Expediente: SDRERC-EXP-2026-000046

   Contexto: durante el diagnostico de los bugs de fecha_acuse
   duplicada y de enrutamiento entre Bandeja Notificacion/Publicacion
   (ver AGENTS.md, 08/08/2026) se detecto que este documento acumulo
   una gran cantidad de intentos duplicados/huerfanos en
   EXPEDIENTE_NOTIFICACION (10 filas: varias versiones de intento 1,
   2 y 3 con distintos resultados) y de EXPEDIENTE_CARGO_ACUSE (14
   filas, algunas activas apuntando a intentos ya dados de baja). El
   usuario pidio explicitamente borrar todo ese historial y volver a
   registrar los intentos desde cero, en vez de intentar reconciliar
   manualmente cada fila.

   Alcance (baja logica, NO fisica): este script NO usa DELETE.
   Sigue el mismo patron de baja logica que ya usa toda la app
   (ACTIVO=0) para que la accion sea reversible y no rompa integridad
   referencial ni el historial de auditoria subyacente:
     1) EXPEDIENTE_NOTIFICACION: da de baja TODOS los intentos (1, 2 y
        3/Publicacion) del/de los documento(s) de este expediente,
        esten activos o ya inactivos.
     2) EXPEDIENTE_CARGO_ACUSE: da de baja todo cargo/acuse ligado a
        esos intentos (incluye los que quedaron activos apuntando a
        intentos ya inactivos, inconsistencia detectada en el
        diagnostico).
     3) EXPEDIENTE_DOCUMENTO_ANALIZADO: limpia FECHA_ACUSE (a NULL) y
        NOTIFICADO (a 0) del/de los documento(s), ya que sin ningun
        intento activo el documento vuelve a estar genuinamente "no
        notificado". NO toca CONFIRMACION_RESPUESTA / FECHA_RESPUESTA
        / NUMERO_HOJA_ENVIO_RESPUESTA (pertenecen al flujo de Cartas
        de Respuesta del ciudadano, no al historial de intentos) ni
        ID_USUARIO_NOTIFICACION / ID_EQUIPO_NOTIFICACION (asignacion
        del documento, no pedida por el usuario).

   No toca EXPEDIENTE.ID_ETAPA_ACTUAL / ID_ESTADO_ACTUAL: el
   expediente permanece donde este hoy, listo para volver a registrar
   intentos desde la misma bandeja sin rehacer la asignacion.

   Ejecutar conectado como SDRERC_APP. NO ejecutar automaticamente:
   requiere autorizacion explicita separada.
   Idempotente: si ya no hay filas activas que dar de baja ni fechas
   que limpiar, no actualiza nada (los contadores quedan en 0).
   ============================================================ */

DECLARE
  v_numero_expediente   VARCHAR2(60) := 'SDRERC-EXP-2026-000046';
  v_intentos_baja        NUMBER := 0;
  v_cargos_baja          NUMBER := 0;
  v_documentos_limpiados NUMBER := 0;
BEGIN
  UPDATE expediente_cargo_acuse ca
     SET ca.activo = 0,
         ca.modificado_en = SYSTIMESTAMP
   WHERE ca.activo = 1
     AND ca.id_expediente_notificacion IN (
           SELECT n.id_expediente_notificacion
             FROM expediente_notificacion n
             JOIN expediente_documento_analizado da ON da.id_documento_analizado = n.id_documento_analizado
             JOIN expediente e ON e.id_expediente = da.id_expediente
            WHERE e.numero_expediente = v_numero_expediente
              AND e.activo = 1);
  v_cargos_baja := SQL%ROWCOUNT;

  UPDATE expediente_notificacion n
     SET n.activo = 0,
         n.modificado_en = SYSTIMESTAMP
   WHERE n.activo = 1
     AND n.id_documento_analizado IN (
           SELECT da.id_documento_analizado
             FROM expediente_documento_analizado da
             JOIN expediente e ON e.id_expediente = da.id_expediente
            WHERE e.numero_expediente = v_numero_expediente
              AND e.activo = 1);
  v_intentos_baja := SQL%ROWCOUNT;

  UPDATE expediente_documento_analizado da
     SET da.fecha_acuse = NULL,
         da.notificado = 0,
         da.modificado_en = SYSTIMESTAMP
   WHERE da.activo = 1
     AND (da.fecha_acuse IS NOT NULL OR NVL(da.notificado, 0) = 1)
     AND da.id_expediente IN (
           SELECT e.id_expediente
             FROM expediente e
            WHERE e.numero_expediente = v_numero_expediente
              AND e.activo = 1);
  v_documentos_limpiados := SQL%ROWCOUNT;

  IF v_intentos_baja = 0 AND v_cargos_baja = 0 AND v_documentos_limpiados = 0 THEN
    DBMS_OUTPUT.PUT_LINE('Sin cambios: no habia intentos/cargos activos ni fechas que limpiar en ' || v_numero_expediente || '.');
  ELSE
    COMMIT;
    DBMS_OUTPUT.PUT_LINE(
      'Reset completado en ' || v_numero_expediente || ': ' ||
      v_intentos_baja || ' intento(s) dado(s) de baja, ' ||
      v_cargos_baja || ' cargo(s)/acuse(s) dado(s) de baja, ' ||
      v_documentos_limpiados || ' documento(s) con fecha_acuse/notificado reseteados.');
  END IF;
END;
/


/* ============================================================
   Verificacion posterior
   ============================================================ */

SELECT n.id_expediente_notificacion, n.numero_intento, tn.codigo AS tipo_notificacion,
       en.codigo AS estado_notificacion, n.activo
  FROM expediente_notificacion n
  JOIN expediente_documento_analizado da ON da.id_documento_analizado = n.id_documento_analizado
  JOIN expediente e ON e.id_expediente = da.id_expediente
  JOIN tipo_notificacion tn ON tn.id_tipo_notificacion = n.id_tipo_notificacion
  JOIN estado_notificacion en ON en.id_estado_notificacion = n.id_estado_notificacion
 WHERE e.numero_expediente = 'SDRERC-EXP-2026-000046'
 ORDER BY n.numero_intento;

SELECT ca.id_expediente_cargo_acuse, ca.activo
  FROM expediente_cargo_acuse ca
  JOIN expediente_notificacion n ON n.id_expediente_notificacion = ca.id_expediente_notificacion
  JOIN expediente_documento_analizado da ON da.id_documento_analizado = n.id_documento_analizado
  JOIN expediente e ON e.id_expediente = da.id_expediente
 WHERE e.numero_expediente = 'SDRERC-EXP-2026-000046';

SELECT da.id_documento_analizado, da.fecha_acuse, da.notificado
  FROM expediente_documento_analizado da
  JOIN expediente e ON e.id_expediente = da.id_expediente
 WHERE e.numero_expediente = 'SDRERC-EXP-2026-000046'
   AND da.activo = 1;
