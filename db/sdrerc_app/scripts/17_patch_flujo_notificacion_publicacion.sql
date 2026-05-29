/* ============================================================
   SCRIPT 17 - Patch flujo notificacion y publicacion
   Ejecutar conectado como SDRERC_APP.
   Idempotente: usa MERGE y conserva datos existentes.
   Completa salidas de EN_NOTIFICACION y PENDIENTE_PUBLICACION.
   ============================================================ */

MERGE INTO tipo_movimiento t
USING (
  SELECT 'NOTIFICACION_VIRTUAL' AS codigo, 'Notificacion virtual' AS nombre FROM dual
  UNION ALL SELECT 'NOTIFICACION_PRESENCIAL_1', 'Primera notificacion presencial' FROM dual
  UNION ALL SELECT 'NOTIFICACION_PRESENCIAL_2', 'Segunda notificacion presencial' FROM dual
  UNION ALL SELECT 'RECEPCION_CARGO_ACUSE', 'Recepcion de cargo de acuse' FROM dual
  UNION ALL SELECT 'CONFIRMACION_NOTIFICACION', 'Confirmacion de notificacion' FROM dual
  UNION ALL SELECT 'REGISTRO_NOTIFICACION_FALLIDA', 'Registro de notificacion fallida' FROM dual
  UNION ALL SELECT 'GENERACION_PUBLICACION', 'Generacion de publicacion' FROM dual
  UNION ALL SELECT 'REGISTRO_PUBLICACION', 'Registro de publicacion' FROM dual
  UNION ALL SELECT 'CIERRE', 'Cierre de expediente' FROM dual
) s
ON (t.codigo = s.codigo)
WHEN MATCHED THEN UPDATE SET t.nombre = s.nombre, t.activo = 1
WHEN NOT MATCHED THEN INSERT (codigo, nombre) VALUES (s.codigo, s.nombre);

MERGE INTO estado_expediente t
USING (
  SELECT e.id_etapa, 'EN_NOTIFICACION' AS codigo, 'En notificacion' AS nombre
  FROM etapa_expediente e
  WHERE e.codigo = 'NOTIFICACION'
  UNION ALL
  SELECT e.id_etapa, 'CARGO_PENDIENTE', 'Cargo pendiente'
  FROM etapa_expediente e
  WHERE e.codigo = 'NOTIFICACION'
  UNION ALL
  SELECT e.id_etapa, 'CARGO_RECIBIDO', 'Cargo recibido'
  FROM etapa_expediente e
  WHERE e.codigo = 'NOTIFICACION'
  UNION ALL
  SELECT e.id_etapa, 'NOTIFICADO', 'Notificado'
  FROM etapa_expediente e
  WHERE e.codigo = 'NOTIFICACION'
  UNION ALL
  SELECT e.id_etapa, 'REQUIERE_PUBLICACION', 'Requiere publicacion'
  FROM etapa_expediente e
  WHERE e.codigo = 'NOTIFICACION'
  UNION ALL
  SELECT e.id_etapa, 'PENDIENTE_PUBLICACION', 'Pendiente de publicacion'
  FROM etapa_expediente e
  WHERE e.codigo = 'PUBLICACION_CONDICIONAL'
  UNION ALL
  SELECT e.id_etapa, 'PUBLICACION_REGISTRADA', 'Publicacion registrada'
  FROM etapa_expediente e
  WHERE e.codigo = 'PUBLICACION_CONDICIONAL'
  UNION ALL
  SELECT e.id_etapa, 'CERRADO', 'Cerrado'
  FROM etapa_expediente e
  WHERE e.codigo = 'CIERRE_ARCHIVO'
) s
ON (t.codigo = s.codigo)
WHEN MATCHED THEN UPDATE SET t.id_etapa = s.id_etapa, t.nombre = s.nombre, t.activo = 1
WHEN NOT MATCHED THEN INSERT (id_etapa, codigo, nombre) VALUES (s.id_etapa, s.codigo, s.nombre);

MERGE INTO flujo t
USING (SELECT 'SDRERC_TO_BE' AS codigo, 'Flujo TO BE SDRERC' AS nombre, '1.0' AS version_flujo FROM dual) s
ON (t.codigo = s.codigo)
WHEN MATCHED THEN UPDATE SET t.nombre = s.nombre, t.version_flujo = s.version_flujo, t.activo = 1
WHEN NOT MATCHED THEN INSERT (codigo, nombre, version_flujo) VALUES (s.codigo, s.nombre, s.version_flujo);

MERGE INTO flujo_transicion t
USING (
  SELECT f.id_flujo, eo.id_etapa AS id_etapa_origen, so.id_estado AS id_estado_origen,
         ed.id_etapa AS id_etapa_destino, sd.id_estado AS id_estado_destino,
         'NOTIFICACION_VIRTUAL' AS codigo_accion,
         'Registrar notificacion virtual' AS nombre_accion,
         0 AS requiere_comentario, 1 AS requiere_documento
  FROM flujo f
  JOIN etapa_expediente eo ON eo.codigo = 'NOTIFICACION'
  JOIN estado_expediente so ON so.codigo = 'EN_NOTIFICACION'
  JOIN etapa_expediente ed ON ed.codigo = 'NOTIFICACION'
  JOIN estado_expediente sd ON sd.codigo = 'CARGO_PENDIENTE'
  WHERE f.codigo = 'SDRERC_TO_BE'
  UNION ALL
  SELECT f.id_flujo, eo.id_etapa, so.id_estado,
         ed.id_etapa, sd.id_estado,
         'NOTIFICACION_PRESENCIAL_1',
         'Registrar primera notificacion presencial',
         0, 1
  FROM flujo f
  JOIN etapa_expediente eo ON eo.codigo = 'NOTIFICACION'
  JOIN estado_expediente so ON so.codigo = 'EN_NOTIFICACION'
  JOIN etapa_expediente ed ON ed.codigo = 'NOTIFICACION'
  JOIN estado_expediente sd ON sd.codigo = 'CARGO_PENDIENTE'
  WHERE f.codigo = 'SDRERC_TO_BE'
  UNION ALL
  SELECT f.id_flujo, eo.id_etapa, so.id_estado,
         ed.id_etapa, sd.id_estado,
         'NOTIFICACION_PRESENCIAL_2',
         'Registrar segunda notificacion presencial',
         1, 1
  FROM flujo f
  JOIN etapa_expediente eo ON eo.codigo = 'NOTIFICACION'
  JOIN estado_expediente so ON so.codigo = 'CARGO_PENDIENTE'
  JOIN etapa_expediente ed ON ed.codigo = 'NOTIFICACION'
  JOIN estado_expediente sd ON sd.codigo = 'CARGO_PENDIENTE'
  WHERE f.codigo = 'SDRERC_TO_BE'
  UNION ALL
  SELECT f.id_flujo, eo.id_etapa, so.id_estado,
         ed.id_etapa, sd.id_estado,
         'RECEPCION_CARGO_ACUSE',
         'Registrar recepcion de cargo de acuse',
         0, 1
  FROM flujo f
  JOIN etapa_expediente eo ON eo.codigo = 'NOTIFICACION'
  JOIN estado_expediente so ON so.codigo = 'CARGO_PENDIENTE'
  JOIN etapa_expediente ed ON ed.codigo = 'NOTIFICACION'
  JOIN estado_expediente sd ON sd.codigo = 'CARGO_RECIBIDO'
  WHERE f.codigo = 'SDRERC_TO_BE'
  UNION ALL
  SELECT f.id_flujo, eo.id_etapa, so.id_estado,
         ed.id_etapa, sd.id_estado,
         'CONFIRMACION_NOTIFICACION',
         'Confirmar notificacion con cargo recibido',
         0, 1
  FROM flujo f
  JOIN etapa_expediente eo ON eo.codigo = 'NOTIFICACION'
  JOIN estado_expediente so ON so.codigo = 'CARGO_RECIBIDO'
  JOIN etapa_expediente ed ON ed.codigo = 'NOTIFICACION'
  JOIN estado_expediente sd ON sd.codigo = 'NOTIFICADO'
  WHERE f.codigo = 'SDRERC_TO_BE'
  UNION ALL
  SELECT f.id_flujo, eo.id_etapa, so.id_estado,
         ed.id_etapa, sd.id_estado,
         'REGISTRO_NOTIFICACION_FALLIDA',
         'Registrar notificacion fallida y requerir publicacion',
         1, 1
  FROM flujo f
  JOIN etapa_expediente eo ON eo.codigo = 'NOTIFICACION'
  JOIN estado_expediente so ON so.codigo = 'CARGO_PENDIENTE'
  JOIN etapa_expediente ed ON ed.codigo = 'NOTIFICACION'
  JOIN estado_expediente sd ON sd.codigo = 'REQUIERE_PUBLICACION'
  WHERE f.codigo = 'SDRERC_TO_BE'
  UNION ALL
  SELECT f.id_flujo, eo.id_etapa, so.id_estado,
         ed.id_etapa, sd.id_estado,
         'GENERACION_PUBLICACION',
         'Generar publicacion por notificacion fallida',
         1, 1
  FROM flujo f
  JOIN etapa_expediente eo ON eo.codigo = 'NOTIFICACION'
  JOIN estado_expediente so ON so.codigo = 'REQUIERE_PUBLICACION'
  JOIN etapa_expediente ed ON ed.codigo = 'PUBLICACION_CONDICIONAL'
  JOIN estado_expediente sd ON sd.codigo = 'PENDIENTE_PUBLICACION'
  WHERE f.codigo = 'SDRERC_TO_BE'
  UNION ALL
  SELECT f.id_flujo, eo.id_etapa, so.id_estado,
         ed.id_etapa, sd.id_estado,
         'REGISTRO_PUBLICACION',
         'Registrar publicacion efectuada',
         0, 1
  FROM flujo f
  JOIN etapa_expediente eo ON eo.codigo = 'PUBLICACION_CONDICIONAL'
  JOIN estado_expediente so ON so.codigo = 'PENDIENTE_PUBLICACION'
  JOIN etapa_expediente ed ON ed.codigo = 'PUBLICACION_CONDICIONAL'
  JOIN estado_expediente sd ON sd.codigo = 'PUBLICACION_REGISTRADA'
  WHERE f.codigo = 'SDRERC_TO_BE'
  UNION ALL
  SELECT f.id_flujo, eo.id_etapa, so.id_estado,
         ed.id_etapa, sd.id_estado,
         'CIERRE',
         'Cerrar expediente notificado',
         0, 0
  FROM flujo f
  JOIN etapa_expediente eo ON eo.codigo = 'NOTIFICACION'
  JOIN estado_expediente so ON so.codigo = 'NOTIFICADO'
  JOIN etapa_expediente ed ON ed.codigo = 'CIERRE_ARCHIVO'
  JOIN estado_expediente sd ON sd.codigo = 'CERRADO'
  WHERE f.codigo = 'SDRERC_TO_BE'
  UNION ALL
  SELECT f.id_flujo, eo.id_etapa, so.id_estado,
         ed.id_etapa, sd.id_estado,
         'CIERRE',
         'Cerrar expediente publicado',
         0, 0
  FROM flujo f
  JOIN etapa_expediente eo ON eo.codigo = 'PUBLICACION_CONDICIONAL'
  JOIN estado_expediente so ON so.codigo = 'PUBLICACION_REGISTRADA'
  JOIN etapa_expediente ed ON ed.codigo = 'CIERRE_ARCHIVO'
  JOIN estado_expediente sd ON sd.codigo = 'CERRADO'
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
WHEN MATCHED THEN UPDATE SET t.nombre_accion = s.nombre_accion, t.requiere_comentario = s.requiere_comentario, t.requiere_documento = s.requiere_documento, t.activo = 1
WHEN NOT MATCHED THEN INSERT (id_flujo, id_etapa_origen, id_estado_origen, id_etapa_destino, id_estado_destino, codigo_accion, nombre_accion, requiere_comentario, requiere_documento, activo)
VALUES (s.id_flujo, s.id_etapa_origen, s.id_estado_origen, s.id_etapa_destino, s.id_estado_destino, s.codigo_accion, s.nombre_accion, s.requiere_comentario, s.requiere_documento, 1);

COMMIT;

/* ============================================================
   Validaciones posteriores
   ============================================================ */

SELECT codigo, nombre
FROM etapa_expediente
WHERE UPPER(codigo) LIKE '%VALIDACION%'
   OR UPPER(nombre) LIKE '%VALIDACION%';

SELECT id_flujo, codigo, nombre, activo
FROM flujo
WHERE codigo = 'SDRERC_TO_BE';

SELECT codigo, nombre, activo
FROM tipo_movimiento
WHERE codigo IN (
  'NOTIFICACION_VIRTUAL',
  'NOTIFICACION_PRESENCIAL_1',
  'NOTIFICACION_PRESENCIAL_2',
  'RECEPCION_CARGO_ACUSE',
  'CONFIRMACION_NOTIFICACION',
  'REGISTRO_NOTIFICACION_FALLIDA',
  'GENERACION_PUBLICACION',
  'REGISTRO_PUBLICACION',
  'CIERRE'
)
ORDER BY codigo;

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
  AND eo.codigo IN ('NOTIFICACION', 'PUBLICACION_CONDICIONAL')
ORDER BY eo.codigo, so.codigo, ft.codigo_accion;

SELECT 'NOTIFICACION_EN_NOTIFICACION_CON_SALIDA' AS validacion,
       COUNT(*) AS rutas_activas
FROM flujo_transicion ft
JOIN flujo f ON f.id_flujo = ft.id_flujo
JOIN etapa_expediente eo ON eo.id_etapa = ft.id_etapa_origen
JOIN estado_expediente so ON so.id_estado = ft.id_estado_origen
WHERE f.codigo = 'SDRERC_TO_BE'
  AND eo.codigo = 'NOTIFICACION'
  AND so.codigo = 'EN_NOTIFICACION'
  AND ft.activo = 1;

SELECT 'PUBLICACION_PENDIENTE_CON_SALIDA' AS validacion,
       COUNT(*) AS rutas_activas
FROM flujo_transicion ft
JOIN flujo f ON f.id_flujo = ft.id_flujo
JOIN etapa_expediente eo ON eo.id_etapa = ft.id_etapa_origen
JOIN estado_expediente so ON so.id_estado = ft.id_estado_origen
WHERE f.codigo = 'SDRERC_TO_BE'
  AND eo.codigo = 'PUBLICACION_CONDICIONAL'
  AND so.codigo = 'PENDIENTE_PUBLICACION'
  AND ft.activo = 1;

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
