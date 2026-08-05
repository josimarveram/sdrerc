/* ============================================================
   SCRIPT 93 - Transiciones para el "Destino operativo" del panel
   Asignacion (fusionado con Firma) de la Bandeja Asignacion de
   Notificacion, hacia EQ_ANALISIS y EQ_EJECUCION.
   Ejecutar conectado como SDRERC_APP.

   Contexto: el panel "Asignacion" de Notificacion (JPanelNotificacionV2,
   metodo generarAsignacionNotificacion) ya cargaba 4 equipos en el combo
   "Equipo destino" (EQ_NOTIFICACION, EQ_VALIDACION, EQ_ANALISIS,
   EQ_EJECUCION), pero elegir EQ_ANALISIS o EQ_EJECUCION no hacia nada real.
   El pedido del usuario: cuando un documento FINAL vuelve Observado desde
   el validador (DocumentoAnalisisDAO.registrarResultadoValidacion NO toca
   el estado del expediente al marcar Observado, solo limpia el
   responsable, asi que el expediente se queda en NOTIFICACION/POR_VALIDAR
   con el documento en estado_documento OBSERVADO), el supervisor de
   notificacion pueda derivarlo de verdad a Analisis o a Ejecucion.

   Unico origen real posible (solo los documentos FINAL pasan por
   Validacion, y solo ese resultado deja al documento en OBSERVADO sin
   mover el estado del expediente): NOTIFICACION/POR_VALIDAR. No se
   siembran otros origenes especulativos (POR_ASIGNAR/EN_NOTIFICACION/
   NOTIFICADO) porque hoy no existe ningun camino real del flujo que deje
   un documento Observado en esos estados.

   Destino EQ_ANALISIS -> ANALISIS/OBSERVADO: reutiliza el codigo_accion
   DEVOLUCION_A_ANALISIS ya usado por Verificacion (script 74) y por el
   panel Cartas de Respuesta de Asignacion (script 91).

   Destino EQ_EJECUCION -> EJECUCION/EN_EJECUCION: no existia ningun
   codigo_accion para este caso. Se crea el tipo_movimiento nuevo
   DEVOLUCION_A_EJECUCION (analogo a DEVOLUCION_A_ANALISIS).

   Idempotente: cada INSERT verifica NOT EXISTS por combinacion exacta.
   ============================================================ */

INSERT INTO tipo_movimiento (codigo, nombre)
SELECT 'DEVOLUCION_A_EJECUCION', 'Devolucion a ejecucion'
FROM dual
WHERE NOT EXISTS (
  SELECT 1 FROM tipo_movimiento WHERE codigo = 'DEVOLUCION_A_EJECUCION'
);

COMMIT;

-- Destino EQ_ANALISIS: NOTIFICACION/POR_VALIDAR -> ANALISIS/OBSERVADO.
INSERT INTO flujo_transicion (
  id_flujo, id_etapa_origen, id_estado_origen, id_etapa_destino, id_estado_destino,
  codigo_accion, nombre_accion, requiere_comentario, requiere_documento
)
SELECT f.id_flujo, eo.id_etapa, so.id_estado, ed.id_etapa, sd.id_estado,
       'DEVOLUCION_A_ANALISIS', 'Derivar a Analisis desde Asignacion (Notificacion)', 0, 0
  FROM flujo f
  JOIN etapa_expediente eo ON eo.codigo = 'NOTIFICACION'
  JOIN estado_expediente so ON so.codigo = 'POR_VALIDAR'
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

-- Destino EQ_EJECUCION: NOTIFICACION/POR_VALIDAR -> EJECUCION/EN_EJECUCION.
INSERT INTO flujo_transicion (
  id_flujo, id_etapa_origen, id_estado_origen, id_etapa_destino, id_estado_destino,
  codigo_accion, nombre_accion, requiere_comentario, requiere_documento
)
SELECT f.id_flujo, eo.id_etapa, so.id_estado, ed.id_etapa, sd.id_estado,
       'DEVOLUCION_A_EJECUCION', 'Derivar a Ejecucion desde Asignacion (Notificacion)', 0, 0
  FROM flujo f
  JOIN etapa_expediente eo ON eo.codigo = 'NOTIFICACION'
  JOIN estado_expediente so ON so.codigo = 'POR_VALIDAR'
  JOIN etapa_expediente ed ON ed.codigo = 'EJECUCION'
  JOIN estado_expediente sd ON sd.codigo = 'EN_EJECUCION'
 WHERE f.codigo = 'SDRERC_TO_BE'
   AND NOT EXISTS (
         SELECT 1 FROM flujo_transicion ft2
          WHERE ft2.codigo_accion = 'DEVOLUCION_A_EJECUCION'
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
 WHERE ft.codigo_accion IN ('DEVOLUCION_A_ANALISIS', 'DEVOLUCION_A_EJECUCION')
 ORDER BY ft.codigo_accion, eo.codigo, so.codigo;
