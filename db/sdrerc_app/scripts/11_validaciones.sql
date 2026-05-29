/* ============================================================
   SCRIPT 11 - Validaciones
   Ejecutar conectado como SDRERC_APP despues de cargar datos.
   ============================================================ */

SELECT et.codigo AS etapa, COUNT(*) AS cantidad
FROM expediente e
JOIN etapa_expediente et ON et.id_etapa = e.id_etapa_actual
GROUP BY et.codigo
ORDER BY et.codigo;

SELECT es.codigo AS estado, COUNT(*) AS cantidad
FROM expediente e
JOIN estado_expediente es ON es.id_estado = e.id_estado_actual
GROUP BY es.codigo
ORDER BY es.codigo;

SELECT u.username, u.nombre_completo, COUNT(*) AS cantidad
FROM expediente e
LEFT JOIN usuario u ON u.id_usuario = e.id_usuario_responsable_actual
GROUP BY u.username, u.nombre_completo
ORDER BY cantidad DESC;

SELECT u.username, u.nombre_completo, COUNT(*) AS cantidad
FROM expediente e
LEFT JOIN usuario u ON u.id_usuario = e.id_usuario_abogado_inicial
GROUP BY u.username, u.nombre_completo
ORDER BY cantidad DESC;

SELECT id_expediente, COUNT(*) AS asignaciones_activas
FROM expediente_asignacion
WHERE activa = 1
GROUP BY id_expediente
HAVING COUNT(*) > 1
ORDER BY asignaciones_activas DESC;

SELECT e.id_expediente, e.numero_expediente
FROM expediente e
WHERE NOT EXISTS (
  SELECT 1
  FROM expediente_historial h
  WHERE h.id_expediente = e.id_expediente
);

SELECT e.id_expediente, e.numero_expediente
FROM expediente e
WHERE e.id_usuario_abogado_inicial IS NULL
  AND EXISTS (
    SELECT 1
    FROM etapa_expediente et
    WHERE et.id_etapa = e.id_etapa_actual
      AND et.codigo IN ('ANALISIS','VERIFICACION','FIRMA_EMISION','EJECUCION')
  );

SELECT n.id_expediente, COUNT(*) AS intentos
FROM expediente_notificacion n
WHERE n.activo = 1
GROUP BY n.id_expediente
HAVING COUNT(*) > 3;

SELECT n.id_expediente
FROM expediente_notificacion n
JOIN estado_notificacion en ON en.id_estado_notificacion = n.id_estado_notificacion
GROUP BY n.id_expediente
HAVING SUM(CASE WHEN en.codigo = 'FALLIDA' THEN 1 ELSE 0 END) = 3;

SELECT e.id_expediente, e.numero_expediente
FROM expediente e
WHERE e.requiere_publicacion = 1;

SELECT e.id_expediente, e.numero_expediente
FROM expediente e
LEFT JOIN expediente_digital d ON d.id_expediente = e.id_expediente AND d.activo = 1
WHERE NVL(d.completo, 0) = 0;

SELECT tabla_afectada, operacion, COUNT(*) AS cantidad
FROM auditoria_evento
GROUP BY tabla_afectada, operacion
ORDER BY tabla_afectada, operacion;

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
LEFT JOIN etapa_expediente eo ON eo.id_etapa = ft.id_etapa_origen
LEFT JOIN estado_expediente so ON so.id_estado = ft.id_estado_origen
JOIN etapa_expediente ed ON ed.id_etapa = ft.id_etapa_destino
JOIN estado_expediente sd ON sd.id_estado = ft.id_estado_destino
WHERE f.codigo = 'SDRERC_TO_BE'
  AND ft.codigo_accion IN (
    'ENVIO_VERIFICACION',
    'REGISTRO_OBSERVACION_VERIFICACION',
    'REVERSION_ESTADO_DOCUMENTO',
    'DEVOLUCION_A_ANALISIS',
    'CORRECCION_DOCUMENTO',
    'REENVIO_VERIFICACION',
    'APROBACION_VERIFICACION',
    'ENVIO_FIRMA'
  )
ORDER BY ft.codigo_accion, etapa_origen, estado_origen;

SELECT 'ANALISIS_VERIFICACION_ENVIO_INICIAL' AS validacion,
       COUNT(*) AS rutas_encontradas
FROM flujo_transicion ft
JOIN flujo f ON f.id_flujo = ft.id_flujo
JOIN etapa_expediente eo ON eo.id_etapa = ft.id_etapa_origen
JOIN etapa_expediente ed ON ed.id_etapa = ft.id_etapa_destino
JOIN estado_expediente so ON so.id_estado = ft.id_estado_origen
JOIN estado_expediente sd ON sd.id_estado = ft.id_estado_destino
WHERE f.codigo = 'SDRERC_TO_BE'
  AND ft.codigo_accion = 'ENVIO_VERIFICACION'
  AND eo.codigo = 'ANALISIS'
  AND so.codigo = 'ATENDIDO'
  AND ed.codigo = 'VERIFICACION'
  AND sd.codigo = 'EN_VERIFICACION';

SELECT 'VERIFICACION_REGISTRO_OBSERVACION' AS validacion,
       COUNT(*) AS rutas_encontradas
FROM flujo_transicion ft
JOIN flujo f ON f.id_flujo = ft.id_flujo
JOIN etapa_expediente eo ON eo.id_etapa = ft.id_etapa_origen
JOIN etapa_expediente ed ON ed.id_etapa = ft.id_etapa_destino
JOIN estado_expediente so ON so.id_estado = ft.id_estado_origen
JOIN estado_expediente sd ON sd.id_estado = ft.id_estado_destino
WHERE f.codigo = 'SDRERC_TO_BE'
  AND ft.codigo_accion = 'REGISTRO_OBSERVACION_VERIFICACION'
  AND eo.codigo = 'VERIFICACION'
  AND so.codigo = 'EN_VERIFICACION'
  AND ed.codigo = 'VERIFICACION'
  AND sd.codigo = 'REQUIERE_CORRECCION';

SELECT 'VERIFICACION_DOCUMENTO_INCONSISTENTE' AS validacion,
       COUNT(*) AS rutas_encontradas
FROM flujo_transicion ft
JOIN flujo f ON f.id_flujo = ft.id_flujo
JOIN etapa_expediente eo ON eo.id_etapa = ft.id_etapa_origen
JOIN etapa_expediente ed ON ed.id_etapa = ft.id_etapa_destino
JOIN estado_expediente so ON so.id_estado = ft.id_estado_origen
JOIN estado_expediente sd ON sd.id_estado = ft.id_estado_destino
WHERE f.codigo = 'SDRERC_TO_BE'
  AND ft.codigo_accion = 'REVERSION_ESTADO_DOCUMENTO'
  AND eo.codigo = 'VERIFICACION'
  AND so.codigo = 'EN_VERIFICACION'
  AND ed.codigo = 'VERIFICACION'
  AND sd.codigo = 'DOCUMENTO_INCONSISTENTE';

SELECT 'VERIFICACION_ANALISIS_DEVOLUCION' AS validacion,
       COUNT(*) AS rutas_encontradas
FROM flujo_transicion ft
JOIN flujo f ON f.id_flujo = ft.id_flujo
JOIN etapa_expediente eo ON eo.id_etapa = ft.id_etapa_origen
JOIN etapa_expediente ed ON ed.id_etapa = ft.id_etapa_destino
JOIN estado_expediente sd ON sd.id_estado = ft.id_estado_destino
WHERE f.codigo = 'SDRERC_TO_BE'
  AND ft.codigo_accion = 'DEVOLUCION_A_ANALISIS'
  AND eo.codigo = 'VERIFICACION'
  AND ed.codigo = 'ANALISIS'
  AND sd.codigo = 'OBSERVADO';

SELECT 'ANALISIS_ANALISIS_CORRECCION' AS validacion,
       COUNT(*) AS rutas_encontradas
FROM flujo_transicion ft
JOIN flujo f ON f.id_flujo = ft.id_flujo
JOIN etapa_expediente eo ON eo.id_etapa = ft.id_etapa_origen
JOIN etapa_expediente ed ON ed.id_etapa = ft.id_etapa_destino
JOIN estado_expediente so ON so.id_estado = ft.id_estado_origen
JOIN estado_expediente sd ON sd.id_estado = ft.id_estado_destino
WHERE f.codigo = 'SDRERC_TO_BE'
  AND ft.codigo_accion = 'CORRECCION_DOCUMENTO'
  AND eo.codigo = 'ANALISIS'
  AND so.codigo = 'OBSERVADO'
  AND ed.codigo = 'ANALISIS'
  AND sd.codigo = 'SUBSANADO';

SELECT 'ANALISIS_VERIFICACION_REENVIO' AS validacion,
       COUNT(*) AS rutas_encontradas
FROM flujo_transicion ft
JOIN flujo f ON f.id_flujo = ft.id_flujo
JOIN etapa_expediente eo ON eo.id_etapa = ft.id_etapa_origen
JOIN etapa_expediente ed ON ed.id_etapa = ft.id_etapa_destino
JOIN estado_expediente so ON so.id_estado = ft.id_estado_origen
JOIN estado_expediente sd ON sd.id_estado = ft.id_estado_destino
WHERE f.codigo = 'SDRERC_TO_BE'
  AND ft.codigo_accion = 'REENVIO_VERIFICACION'
  AND eo.codigo = 'ANALISIS'
  AND so.codigo = 'SUBSANADO'
  AND ed.codigo = 'VERIFICACION'
  AND sd.codigo = 'EN_VERIFICACION';

SELECT 'VERIFICACION_FIRMA_ENVIO' AS validacion,
       COUNT(*) AS rutas_encontradas
FROM flujo_transicion ft
JOIN flujo f ON f.id_flujo = ft.id_flujo
JOIN etapa_expediente eo ON eo.id_etapa = ft.id_etapa_origen
JOIN etapa_expediente ed ON ed.id_etapa = ft.id_etapa_destino
JOIN estado_expediente so ON so.id_estado = ft.id_estado_origen
JOIN estado_expediente sd ON sd.id_estado = ft.id_estado_destino
WHERE f.codigo = 'SDRERC_TO_BE'
  AND ft.codigo_accion = 'ENVIO_FIRMA'
  AND eo.codigo = 'VERIFICACION'
  AND so.codigo = 'VERIFICADO'
  AND ed.codigo = 'FIRMA_EMISION'
  AND sd.codigo = 'PARA_FIRMA';
