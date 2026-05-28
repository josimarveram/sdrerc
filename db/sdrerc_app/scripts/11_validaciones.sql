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

