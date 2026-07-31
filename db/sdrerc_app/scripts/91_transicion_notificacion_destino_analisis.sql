/* ============================================================
   SCRIPT 91 - Transicion nueva para "Registrar Asignacion" del panel
   Cartas de Respuesta (Asignacion) hacia Equipo Analisis
   Ejecutar conectado como SDRERC_APP.

   Contexto: el boton "Registrar Asignacion" del bloque "Destino operativo"
   del panel Cartas de Respuesta (JPanelAsignacionV2.registrarAsignacionCarta,
   siempre contra el equipo EQ_ANALISIS) ahora debe derivar el expediente de
   vuelta a la Bandeja Analisis del abogado elegido, no solo reasignar el
   responsable (ver AGENTS.md, entrada "Registrar Asignacion desde Cartas de
   Respuesta deriva a Bandeja Analisis"). Igual que el patron ya usado en
   Verificacion (script 74), se reutiliza el codigo_accion DEVOLUCION_A_ANALISIS
   con destino fijo ANALISIS/OBSERVADO, agregando filas de origen nuevas.

   El expediente llega a la Bandeja Cartas de Respuesta por una condicion
   puramente documental (expediente_documento_analizado.requiere_respuesta=1
   AND notificado=1, ver DocumentoAnalisisDAO.listarCartasRespuestaPendientes),
   sin filtro de etapa/estado del expediente. La etapa real en ese punto no es
   unica: el flujo de Notificacion deja al expediente en NOTIFICACION/EN_NOTIFICACION
   ("Por notificar", donde se registran los intentos que marcan notificado=1) y,
   si ya se confirmo el cierre, en NOTIFICACION/NOTIFICADO. Se siembran ambas
   filas de origen para cubrir los 2 casos reales.

   Idempotente: cada INSERT verifica NOT EXISTS por combinacion exacta de
   codigo_accion + etapa/estado origen + etapa/estado destino.
   ============================================================ */

INSERT INTO flujo_transicion (
  id_flujo, id_etapa_origen, id_estado_origen, id_etapa_destino, id_estado_destino,
  codigo_accion, nombre_accion, requiere_comentario, requiere_documento
)
SELECT f.id_flujo, eo.id_etapa, so.id_estado, ed.id_etapa, sd.id_estado,
       'DEVOLUCION_A_ANALISIS', 'Derivar a Analisis desde Cartas de Respuesta (Asignacion)', 0, 0
  FROM flujo f
  JOIN etapa_expediente eo ON eo.codigo = 'NOTIFICACION'
  JOIN estado_expediente so ON so.codigo = 'EN_NOTIFICACION'
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

INSERT INTO flujo_transicion (
  id_flujo, id_etapa_origen, id_estado_origen, id_etapa_destino, id_estado_destino,
  codigo_accion, nombre_accion, requiere_comentario, requiere_documento
)
SELECT f.id_flujo, eo.id_etapa, so.id_estado, ed.id_etapa, sd.id_estado,
       'DEVOLUCION_A_ANALISIS', 'Derivar a Analisis desde Cartas de Respuesta (Asignacion)', 0, 0
  FROM flujo f
  JOIN etapa_expediente eo ON eo.codigo = 'NOTIFICACION'
  JOIN estado_expediente so ON so.codigo = 'NOTIFICADO'
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
 ORDER BY eo.codigo, so.codigo;
