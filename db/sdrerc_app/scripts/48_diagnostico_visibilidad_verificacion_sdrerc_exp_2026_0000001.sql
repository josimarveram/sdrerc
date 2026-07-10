/* ============================================================
   SCRIPT 48 - Diagnostico (SOLO LECTURA): visibilidad en bandeja
   de Verificacion para el expediente SDRERC-EXP-2026-000001.

   Contexto: el usuario reporta que el expediente dejo de aparecer
   en la Bandeja de Verificacion despues de editar el Estado y la
   Fecha Emision de un documento en el panel "Documentos verificados",
   sin haber presionado "Registrar Verificacion".

   Causa raiz identificada en codigo (VerificacionExpedienteDAO.
   buscarExpedientes): hasta antes de esta correccion, la bandeja
   exigia (INNER JOIN) que el expediente tuviera al menos un
   documento activo en estado EN_DESPACHO. Si el supervisor cambiaba
   el Estado del documento (por ejemplo a EMITIDO) desde el icono de
   guardar de la grilla, sin completar "Registrar Verificacion", el
   expediente dejaba de cumplir esa condicion y desaparecia de la
   bandeja aunque su etapa/estado (VERIFICACION/EN_VERIFICACION)
   seguia intacta. Esto ya se corrigio en VerificacionExpedienteDAO
   (LEFT JOIN + filtro por etapa/estado restaurado).

   Este script NO modifica datos. Solo confirma el estado real en
   base de datos para este expediente puntual antes de considerar
   el caso cerrado.

   Ejecutar conectado como SDRERC_APP.
   ============================================================ */

-- 1. Etapa/estado actual del expediente (deberia seguir en
--    VERIFICACION/EN_VERIFICACION si nadie lo movio manualmente).
SELECT e.id_expediente, e.numero_expediente, et.codigo AS etapa_actual,
       es.codigo AS estado_actual, e.activo, e.fecha_ultimo_movimiento
  FROM expediente e
  JOIN etapa_expediente et ON et.id_etapa = e.id_etapa_actual
  JOIN estado_expediente es ON es.id_estado = e.id_estado_actual
 WHERE e.numero_expediente = 'SDRERC-EXP-2026-000001';

-- 2. Documentos analizados activos del expediente y su estado actual
--    (para ver a que estado quedo el documento que se edito).
SELECT da.id_documento_analizado, da.id_expediente, tda.nombre AS tipo_documento,
       ed.codigo AS estado_documento_codigo, ed.nombre AS estado_documento_nombre,
       da.numero_documento, da.fecha_documento, da.activo, da.modificado_en
  FROM expediente_documento_analizado da
  JOIN expediente e ON e.id_expediente = da.id_expediente
  LEFT JOIN tipo_documento_adjunto tda ON tda.id_tipo_documento_adjunto = da.id_tipo_documento_adjunto
  LEFT JOIN estado_documento ed ON ed.id_estado_documento = da.id_estado_documento
 WHERE e.numero_expediente = 'SDRERC-EXP-2026-000001'
   AND da.activo = 1
 ORDER BY da.modificado_en DESC NULLS LAST, da.id_documento_analizado DESC;

-- 3. Ultimos movimientos de historial (para confirmar que no hubo
--    ninguna transicion real fuera de Verificacion).
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
 WHERE e.numero_expediente = 'SDRERC-EXP-2026-000001'
   AND eh.activo = 1
 ORDER BY eh.fecha_movimiento DESC;
