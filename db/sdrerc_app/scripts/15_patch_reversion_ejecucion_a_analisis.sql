/* ============================================================
   SCRIPT 15 - Patch reversion desde ejecucion a analisis
   Ejecutar conectado como SDRERC_APP.
   Idempotente: usa MERGE y conserva datos existentes.
   No crea etapa VALIDACION ni nuevas asignaciones obligatorias.
   ============================================================ */

MERGE INTO tipo_movimiento t
USING (
  SELECT 'REVERSION_ESTADO_DOCUMENTO_EJECUCION' AS codigo, 'Reversion de estado de documento en ejecucion' AS nombre FROM dual
  UNION ALL SELECT 'OBSERVACION_EJECUCION', 'Observacion en ejecucion' FROM dual
  UNION ALL SELECT 'DEVOLUCION_A_ANALISIS', 'Devolucion a analisis' FROM dual
  UNION ALL SELECT 'CORRECCION_DOCUMENTO', 'Correccion de documento' FROM dual
  UNION ALL SELECT 'REENVIO_VERIFICACION', 'Reenvio a verificacion' FROM dual
  UNION ALL SELECT 'APROBACION_VERIFICACION', 'Aprobacion de verificacion' FROM dual
  UNION ALL SELECT 'ENVIO_FIRMA', 'Envio a firma' FROM dual
  UNION ALL SELECT 'FIRMA_DOCUMENTO', 'Firma de documento' FROM dual
) s
ON (t.codigo = s.codigo)
WHEN MATCHED THEN UPDATE SET t.nombre = s.nombre, t.activo = 1
WHEN NOT MATCHED THEN INSERT (codigo, nombre) VALUES (s.codigo, s.nombre);

MERGE INTO estado_expediente t
USING (
  SELECT e.id_etapa, 'EN_EJECUCION' AS codigo, 'En ejecucion' AS nombre
  FROM etapa_expediente e
  WHERE e.codigo = 'EJECUCION'
  UNION ALL
  SELECT e.id_etapa, 'DOCUMENTO_INCONSISTENTE', 'Documento inconsistente'
  FROM etapa_expediente e
  WHERE e.codigo = 'VERIFICACION'
  UNION ALL
  SELECT e.id_etapa, 'REQUIERE_CORRECCION', 'Requiere correccion'
  FROM etapa_expediente e
  WHERE e.codigo = 'VERIFICACION'
  UNION ALL
  SELECT e.id_etapa, 'OBSERVADO', 'Observado'
  FROM etapa_expediente e
  WHERE e.codigo = 'ANALISIS'
  UNION ALL
  SELECT e.id_etapa, 'SUBSANADO', 'Subsanado'
  FROM etapa_expediente e
  WHERE e.codigo = 'ANALISIS'
  UNION ALL
  SELECT e.id_etapa, 'EN_VERIFICACION', 'En verificacion'
  FROM etapa_expediente e
  WHERE e.codigo = 'VERIFICACION'
  UNION ALL
  SELECT e.id_etapa, 'VERIFICADO', 'Verificado'
  FROM etapa_expediente e
  WHERE e.codigo = 'VERIFICACION'
  UNION ALL
  SELECT e.id_etapa, 'PARA_FIRMA', 'Para firma'
  FROM etapa_expediente e
  WHERE e.codigo = 'FIRMA_EMISION'
  UNION ALL
  SELECT e.id_etapa, 'FIRMADO', 'Firmado'
  FROM etapa_expediente e
  WHERE e.codigo = 'FIRMA_EMISION'
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
         'REVERSION_ESTADO_DOCUMENTO_EJECUCION' AS codigo_accion,
         'Registrar documento inconsistente en ejecucion' AS nombre_accion,
         1 AS requiere_comentario, 1 AS requiere_documento
  FROM flujo f
  JOIN etapa_expediente eo ON eo.codigo = 'EJECUCION'
  JOIN estado_expediente so ON so.codigo = 'EN_EJECUCION'
  JOIN etapa_expediente ed ON ed.codigo = 'EJECUCION'
  JOIN estado_expediente sd ON sd.codigo = 'DOCUMENTO_INCONSISTENTE'
  WHERE f.codigo = 'SDRERC_TO_BE'
  UNION ALL
  SELECT f.id_flujo, eo.id_etapa, so.id_estado,
         ed.id_etapa, sd.id_estado,
         'OBSERVACION_EJECUCION',
         'Registrar observacion de ejecucion',
         1, 1
  FROM flujo f
  JOIN etapa_expediente eo ON eo.codigo = 'EJECUCION'
  JOIN estado_expediente so ON so.codigo = 'EN_EJECUCION'
  JOIN etapa_expediente ed ON ed.codigo = 'EJECUCION'
  JOIN estado_expediente sd ON sd.codigo = 'REQUIERE_CORRECCION'
  WHERE f.codigo = 'SDRERC_TO_BE'
  UNION ALL
  SELECT f.id_flujo, eo.id_etapa, so.id_estado,
         ed.id_etapa, sd.id_estado,
         'DEVOLUCION_A_ANALISIS',
         'Devolver documento inconsistente de ejecucion a analisis',
         1, 1
  FROM flujo f
  JOIN etapa_expediente eo ON eo.codigo = 'EJECUCION'
  JOIN estado_expediente so ON so.codigo = 'DOCUMENTO_INCONSISTENTE'
  JOIN etapa_expediente ed ON ed.codigo = 'ANALISIS'
  JOIN estado_expediente sd ON sd.codigo = 'OBSERVADO'
  WHERE f.codigo = 'SDRERC_TO_BE'
  UNION ALL
  SELECT f.id_flujo, eo.id_etapa, so.id_estado,
         ed.id_etapa, sd.id_estado,
         'DEVOLUCION_A_ANALISIS',
         'Devolver observacion de ejecucion a analisis',
         1, 1
  FROM flujo f
  JOIN etapa_expediente eo ON eo.codigo = 'EJECUCION'
  JOIN estado_expediente so ON so.codigo = 'REQUIERE_CORRECCION'
  JOIN etapa_expediente ed ON ed.codigo = 'ANALISIS'
  JOIN estado_expediente sd ON sd.codigo = 'OBSERVADO'
  WHERE f.codigo = 'SDRERC_TO_BE'
  UNION ALL
  SELECT f.id_flujo, eo.id_etapa, so.id_estado,
         ed.id_etapa, sd.id_estado,
         'CORRECCION_DOCUMENTO',
         'Registrar correccion de documento',
         1, 1
  FROM flujo f
  JOIN etapa_expediente eo ON eo.codigo = 'ANALISIS'
  JOIN estado_expediente so ON so.codigo = 'OBSERVADO'
  JOIN etapa_expediente ed ON ed.codigo = 'ANALISIS'
  JOIN estado_expediente sd ON sd.codigo = 'SUBSANADO'
  WHERE f.codigo = 'SDRERC_TO_BE'
  UNION ALL
  SELECT f.id_flujo, eo.id_etapa, so.id_estado,
         ed.id_etapa, sd.id_estado,
         'REENVIO_VERIFICACION',
         'Reenviar correccion a verificacion',
         0, 1
  FROM flujo f
  JOIN etapa_expediente eo ON eo.codigo = 'ANALISIS'
  JOIN estado_expediente so ON so.codigo = 'SUBSANADO'
  JOIN etapa_expediente ed ON ed.codigo = 'VERIFICACION'
  JOIN estado_expediente sd ON sd.codigo = 'EN_VERIFICACION'
  WHERE f.codigo = 'SDRERC_TO_BE'
  UNION ALL
  SELECT f.id_flujo, eo.id_etapa, so.id_estado,
         ed.id_etapa, sd.id_estado,
         'APROBACION_VERIFICACION',
         'Aprobar verificacion',
         0, 1
  FROM flujo f
  JOIN etapa_expediente eo ON eo.codigo = 'VERIFICACION'
  JOIN estado_expediente so ON so.codigo = 'EN_VERIFICACION'
  JOIN etapa_expediente ed ON ed.codigo = 'VERIFICACION'
  JOIN estado_expediente sd ON sd.codigo = 'VERIFICADO'
  WHERE f.codigo = 'SDRERC_TO_BE'
  UNION ALL
  SELECT f.id_flujo, eo.id_etapa, so.id_estado,
         ed.id_etapa, sd.id_estado,
         'ENVIO_FIRMA',
         'Enviar expediente verificado a firma',
         0, 1
  FROM flujo f
  JOIN etapa_expediente eo ON eo.codigo = 'VERIFICACION'
  JOIN estado_expediente so ON so.codigo = 'VERIFICADO'
  JOIN etapa_expediente ed ON ed.codigo = 'FIRMA_EMISION'
  JOIN estado_expediente sd ON sd.codigo = 'PARA_FIRMA'
  WHERE f.codigo = 'SDRERC_TO_BE'
  UNION ALL
  SELECT f.id_flujo, eo.id_etapa, so.id_estado,
         ed.id_etapa, sd.id_estado,
         'FIRMA_DOCUMENTO',
         'Firmar documento',
         0, 1
  FROM flujo f
  JOIN etapa_expediente eo ON eo.codigo = 'FIRMA_EMISION'
  JOIN estado_expediente so ON so.codigo = 'PARA_FIRMA'
  JOIN etapa_expediente ed ON ed.codigo = 'FIRMA_EMISION'
  JOIN estado_expediente sd ON sd.codigo = 'FIRMADO'
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
  'REVERSION_ESTADO_DOCUMENTO_EJECUCION',
  'OBSERVACION_EJECUCION',
  'DEVOLUCION_A_ANALISIS',
  'CORRECCION_DOCUMENTO',
  'REENVIO_VERIFICACION',
  'APROBACION_VERIFICACION',
  'ENVIO_FIRMA',
  'FIRMA_DOCUMENTO'
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
  AND (
    (
      eo.codigo = 'EJECUCION'
      AND so.codigo IN ('DOCUMENTO_INCONSISTENTE', 'REQUIERE_CORRECCION')
      AND ft.codigo_accion = 'DEVOLUCION_A_ANALISIS'
      AND ed.codigo = 'ANALISIS'
      AND sd.codigo = 'OBSERVADO'
    )
    OR ft.codigo_accion IN (
      'REVERSION_ESTADO_DOCUMENTO_EJECUCION',
      'OBSERVACION_EJECUCION'
    )
  )
ORDER BY etapa_origen, estado_origen, ft.codigo_accion;

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

SELECT ft.codigo_accion,
       eo.codigo AS etapa_origen,
       so.codigo AS estado_origen,
       ed.codigo AS etapa_destino,
       sd.codigo AS estado_destino,
       ft.activo
FROM flujo_transicion ft
JOIN flujo f ON f.id_flujo = ft.id_flujo
JOIN etapa_expediente eo ON eo.id_etapa = ft.id_etapa_origen
JOIN estado_expediente so ON so.id_estado = ft.id_estado_origen
JOIN etapa_expediente ed ON ed.id_etapa = ft.id_etapa_destino
JOIN estado_expediente sd ON sd.id_estado = ft.id_estado_destino
WHERE f.codigo = 'SDRERC_TO_BE'
  AND eo.codigo = 'EJECUCION'
  AND so.codigo IN ('EN_EJECUCION', 'DOCUMENTO_INCONSISTENTE', 'REQUIERE_CORRECCION')
  AND ed.codigo = 'NOTIFICACION'
  AND ft.activo = 1
ORDER BY so.codigo, ft.codigo_accion;
