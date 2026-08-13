/* ============================================================
   SCRIPT 74 - Transicion nueva para "Destino operativo" de Verificacion
   hacia Equipo Analisis
   Ejecutar conectado como SDRERC_APP.

   Contexto: el combo "Equipo destino" del bloque "Destino operativo" de
   Verificacion (JPanelVerificacionV2) ahora ofrece Equipo Analisis,
   Equipo Ejecucion y Equipo Supervision (ver AGENTS.md, seccion de
   combos "Destino operativo"). Pedido explicito del usuario: el
   expediente debe caer en la etapa/estado real segun el equipo elegido,
   no siempre en Notificacion/Por asignar:
     - Eq. Analisis     -> ANALISIS/OBSERVADO
     - Eq. Ejecucion    -> EJECUCION/EN_EJECUCION (ya existe, misma fila
                           que usa aprobarVerificacionDirecta para
                           resoluciones: codigo_accion APROBACION_VERIFICACION,
                           origen VERIFICACION/EN_VERIFICACION)
     - Eq. Supervision  -> NOTIFICACION/POR_ASIGNAR (comportamiento ya
                           existente, sin cambios)

   Solo falta la fila para "Eq. Analisis": no existia ninguna transicion
   de VERIFICACION/EN_VERIFICACION hacia ANALISIS/OBSERVADO (las 2 filas
   existentes de DEVOLUCION_A_ANALISIS parten de VERIFICACION/REQUIERE_CORRECCION
   y VERIFICACION/DOCUMENTO_INCONSISTENTE, no de EN_VERIFICACION). Se
   reutiliza el mismo codigo_accion DEVOLUCION_A_ANALISIS, agregando una
   fila nueva con origen distinto (mismo patron que las multiples filas
   de DERIVACION_A_NOTIFICACION).

   Idempotente: el INSERT verifica NOT EXISTS por combinacion exacta de
   codigo_accion + etapa/estado origen + etapa/estado destino.
   ============================================================ */

INSERT INTO flujo_transicion (
  id_flujo, id_etapa_origen, id_estado_origen, id_etapa_destino, id_estado_destino,
  codigo_accion, nombre_accion, requiere_comentario, requiere_documento
)
SELECT f.id_flujo, eo.id_etapa, so.id_estado, ed.id_etapa, sd.id_estado,
       'DEVOLUCION_A_ANALISIS', 'Devolver a Analisis desde destino operativo de Verificacion', 0, 0
  FROM flujo f
  JOIN etapa_expediente eo ON eo.codigo = 'VERIFICACION'
  JOIN estado_expediente so ON so.codigo = 'EN_VERIFICACION'
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
