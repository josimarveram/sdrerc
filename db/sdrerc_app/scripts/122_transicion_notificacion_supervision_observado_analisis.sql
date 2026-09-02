/* ============================================================
   SCRIPT 122 - Transicion nueva para "Destino operativo" del panel de
   Supervision de Notificacion (Bandeja de supervision, resultado
   "Observado") hacia Equipo Analisis
   Ejecutar conectado como SDRERC_APP.

   Contexto: 02/09/2026, pedido explicito del usuario ("restaurar" el
   flujo que existia antes de separar Asignacion/Supervision en 2
   paneles el 28/08/2026) - al elegir resultado "Observado" en el panel
   de Supervision, debe poder derivarse el documento a Eq. Analisis o
   Eq. Ejecucion (reutilizando AsignacionExpedienteDAO.reasignarDesdeCartaRespuesta
   con codigoEstadoDestino="OBSERVADO", ya portado a V3 el 24/08/2026 para
   "Cartas de respuesta" con destino "DERIVADO").

   Eq. Ejecucion ya tiene transicion activa (DocumentoAnalisisDAO#derivarDocumentoNotificacionAEjecucion,
   sin pasar por flujo_transicion de esta forma). Eq. Analisis necesita
   una fila DEVOLUCION_A_ANALISIS con origen real de esta bandeja:
   NOTIFICACION/POR_ASIGNAR (el estado del expediente mientras un
   documento espera en la Bandeja de supervision/asignacion, ver
   CONDICION_ASIGNACION_NOTIFICACION). Verificado con SELECT de solo
   lectura que NO existia ninguna fila DEVOLUCION_A_ANALISIS con origen
   NOTIFICACION/POR_ASIGNAR (solo existian con origen EN_NOTIFICACION,
   NOTIFICADO y POR_VALIDAR).

   Idempotente: el INSERT verifica NOT EXISTS por combinacion exacta de
   codigo_accion + etapa/estado origen + etapa/estado destino (mismo
   patron que el script 74).
   ============================================================ */

INSERT INTO flujo_transicion (
  id_flujo, id_etapa_origen, id_estado_origen, id_etapa_destino, id_estado_destino,
  codigo_accion, nombre_accion, requiere_comentario, requiere_documento
)
SELECT f.id_flujo, eo.id_etapa, so.id_estado, ed.id_etapa, sd.id_estado,
       'DEVOLUCION_A_ANALISIS', 'Devolver a Analisis desde destino operativo de Supervision (Notificacion)', 0, 0
  FROM flujo f
  JOIN etapa_expediente eo ON eo.codigo = 'NOTIFICACION'
  JOIN estado_expediente so ON so.codigo = 'POR_ASIGNAR'
  JOIN etapa_expediente ed ON ed.codigo = 'ANALISIS'
  JOIN estado_expediente sd ON sd.codigo = 'OBSERVADO'
 WHERE f.codigo = 'SDRERC_TO_BE'
   AND NOT EXISTS (
         SELECT 1 FROM flujo_transicion ft2
          WHERE ft2.codigo_accion = 'DEVOLUCION_A_ANALISIS'
            AND ft2.id_etapa_origen = eo.id_etapa
            AND ft2.id_estado_origen = so.id_estado
            AND ft2.id_etapa_destino = ed.id_etapa
            AND ft2.id_estado_destino = sd.id_estado);

COMMIT;

/* ============================================================
   Verificacion posterior
   ============================================================ */

SELECT ft.id_flujo_transicion, ft.codigo_accion, eo.codigo AS etapa_origen, so.codigo AS estado_origen,
       ed.codigo AS etapa_destino, sd.codigo AS estado_destino, ft.activo
  FROM flujo_transicion ft
  JOIN etapa_expediente eo ON eo.id_etapa = ft.id_etapa_origen
  JOIN estado_expediente so ON so.id_estado = ft.id_estado_origen
  JOIN etapa_expediente ed ON ed.id_etapa = ft.id_etapa_destino
  JOIN estado_expediente sd ON sd.id_estado = ft.id_estado_destino
 WHERE ft.codigo_accion = 'DEVOLUCION_A_ANALISIS'
   AND eo.codigo = 'NOTIFICACION'
 ORDER BY so.codigo;
