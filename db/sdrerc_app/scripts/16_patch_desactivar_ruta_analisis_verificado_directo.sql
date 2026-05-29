/* ============================================================
   SCRIPT 16 - Desactivar ruta directa analisis a verificado
   Ejecutar conectado como SDRERC_APP.
   Idempotente: usa MERGE y conserva trazabilidad.
   No crea etapa VALIDACION ni nuevas asignaciones obligatorias.
   ============================================================ */

MERGE INTO flujo_transicion t
USING (
  SELECT f.id_flujo,
         eo.id_etapa AS id_etapa_origen,
         so.id_estado AS id_estado_origen,
         ed.id_etapa AS id_etapa_destino,
         sd.id_estado AS id_estado_destino,
         'REGISTRO_RESULTADO_ANALISIS' AS codigo_accion
  FROM flujo f
  JOIN etapa_expediente eo ON eo.codigo = 'ANALISIS'
  JOIN estado_expediente so ON so.codigo = 'ATENDIDO'
  JOIN etapa_expediente ed ON ed.codigo = 'VERIFICACION'
  JOIN estado_expediente sd ON sd.codigo = 'VERIFICADO'
  WHERE f.codigo = 'SDRERC_TO_BE'
) s
ON (
  t.id_flujo = s.id_flujo
  AND t.id_etapa_origen = s.id_etapa_origen
  AND t.id_estado_origen = s.id_estado_origen
  AND t.codigo_accion = s.codigo_accion
  AND t.id_etapa_destino = s.id_etapa_destino
  AND t.id_estado_destino = s.id_estado_destino
)
WHEN MATCHED THEN UPDATE SET
  t.activo = 0,
  t.nombre_accion = 'Ruta obsoleta reemplazada por ENVIO_VERIFICACION hacia EN_VERIFICACION';

COMMIT;

/* ============================================================
   Validaciones posteriores
   ============================================================ */

SELECT ft.codigo_accion,
       ft.nombre_accion,
       eo.codigo AS etapa_origen,
       so.codigo AS estado_origen,
       ed.codigo AS etapa_destino,
       sd.codigo AS estado_destino,
       ft.requiere_comentario,
       ft.requiere_documento,
       ft.activo
FROM flujo_transicion ft
JOIN flujo f ON f.id_flujo = ft.id_flujo
JOIN etapa_expediente eo ON eo.id_etapa = ft.id_etapa_origen
JOIN estado_expediente so ON so.id_estado = ft.id_estado_origen
JOIN etapa_expediente ed ON ed.id_etapa = ft.id_etapa_destino
JOIN estado_expediente sd ON sd.id_estado = ft.id_estado_destino
WHERE f.codigo = 'SDRERC_TO_BE'
  AND eo.codigo = 'ANALISIS'
  AND so.codigo = 'ATENDIDO'
  AND ft.codigo_accion = 'REGISTRO_RESULTADO_ANALISIS'
  AND ed.codigo = 'VERIFICACION'
  AND sd.codigo = 'VERIFICADO';

SELECT COUNT(*) AS rutas_obsoletas_activas
FROM flujo_transicion ft
JOIN flujo f ON f.id_flujo = ft.id_flujo
JOIN etapa_expediente eo ON eo.id_etapa = ft.id_etapa_origen
JOIN estado_expediente so ON so.id_estado = ft.id_estado_origen
JOIN etapa_expediente ed ON ed.id_etapa = ft.id_etapa_destino
JOIN estado_expediente sd ON sd.id_estado = ft.id_estado_destino
WHERE f.codigo = 'SDRERC_TO_BE'
  AND eo.codigo = 'ANALISIS'
  AND so.codigo = 'ATENDIDO'
  AND ft.codigo_accion = 'REGISTRO_RESULTADO_ANALISIS'
  AND ed.codigo = 'VERIFICACION'
  AND sd.codigo = 'VERIFICADO'
  AND ft.activo = 1;

SELECT ft.codigo_accion,
       eo.codigo AS etapa_origen,
       so.codigo AS estado_origen,
       ed.codigo AS etapa_destino,
       sd.codigo AS estado_destino,
       ft.requiere_comentario,
       ft.requiere_documento,
       ft.activo
FROM flujo_transicion ft
JOIN flujo f ON f.id_flujo = ft.id_flujo
JOIN etapa_expediente eo ON eo.id_etapa = ft.id_etapa_origen
JOIN estado_expediente so ON so.id_estado = ft.id_estado_origen
JOIN etapa_expediente ed ON ed.id_etapa = ft.id_etapa_destino
JOIN estado_expediente sd ON sd.id_estado = ft.id_estado_destino
WHERE f.codigo = 'SDRERC_TO_BE'
  AND eo.codigo = 'ANALISIS'
  AND so.codigo = 'ATENDIDO'
  AND ft.codigo_accion = 'ENVIO_VERIFICACION'
  AND ed.codigo = 'VERIFICACION'
  AND sd.codigo = 'EN_VERIFICACION';

SELECT id_flujo,
       id_etapa_origen,
       id_estado_origen,
       codigo_accion,
       id_etapa_destino,
       id_estado_destino,
       COUNT(*) AS cantidad
FROM flujo_transicion
GROUP BY id_flujo,
         id_etapa_origen,
         id_estado_origen,
         codigo_accion,
         id_etapa_destino,
         id_estado_destino
HAVING COUNT(*) > 1
ORDER BY cantidad DESC;
