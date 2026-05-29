/* ============================================================
   SCRIPT 14 - Patch flujo verificacion, correccion y reenvio
   Ejecutar conectado como SDRERC_APP.
   Idempotente: usa MERGE y no borra datos.
   No crea etapa VALIDACION ni nuevas asignaciones obligatorias.
   ============================================================ */

MERGE INTO tipo_movimiento t
USING (SELECT 'ENVIO_VERIFICACION' AS codigo, 'Envio a verificacion' AS nombre FROM dual) s
ON (t.codigo = s.codigo)
WHEN MATCHED THEN UPDATE SET t.nombre = s.nombre, t.activo = 1
WHEN NOT MATCHED THEN INSERT (codigo, nombre) VALUES (s.codigo, s.nombre);

MERGE INTO tipo_movimiento t
USING (SELECT 'REGISTRO_OBSERVACION_VERIFICACION' AS codigo, 'Registro de observacion en verificacion' AS nombre FROM dual) s
ON (t.codigo = s.codigo)
WHEN MATCHED THEN UPDATE SET t.nombre = s.nombre, t.activo = 1
WHEN NOT MATCHED THEN INSERT (codigo, nombre) VALUES (s.codigo, s.nombre);

MERGE INTO tipo_movimiento t
USING (SELECT 'DEVOLUCION_A_ANALISIS' AS codigo, 'Devolucion a analisis' AS nombre FROM dual) s
ON (t.codigo = s.codigo)
WHEN MATCHED THEN UPDATE SET t.nombre = s.nombre, t.activo = 1
WHEN NOT MATCHED THEN INSERT (codigo, nombre) VALUES (s.codigo, s.nombre);

MERGE INTO tipo_movimiento t
USING (SELECT 'CORRECCION_DOCUMENTO' AS codigo, 'Correccion de documento' AS nombre FROM dual) s
ON (t.codigo = s.codigo)
WHEN MATCHED THEN UPDATE SET t.nombre = s.nombre, t.activo = 1
WHEN NOT MATCHED THEN INSERT (codigo, nombre) VALUES (s.codigo, s.nombre);

MERGE INTO tipo_movimiento t
USING (SELECT 'REENVIO_VERIFICACION' AS codigo, 'Reenvio a verificacion' AS nombre FROM dual) s
ON (t.codigo = s.codigo)
WHEN MATCHED THEN UPDATE SET t.nombre = s.nombre, t.activo = 1
WHEN NOT MATCHED THEN INSERT (codigo, nombre) VALUES (s.codigo, s.nombre);

MERGE INTO tipo_movimiento t
USING (SELECT 'APROBACION_VERIFICACION' AS codigo, 'Aprobacion de verificacion' AS nombre FROM dual) s
ON (t.codigo = s.codigo)
WHEN MATCHED THEN UPDATE SET t.nombre = s.nombre, t.activo = 1
WHEN NOT MATCHED THEN INSERT (codigo, nombre) VALUES (s.codigo, s.nombre);

MERGE INTO tipo_movimiento t
USING (SELECT 'ENVIO_FIRMA' AS codigo, 'Envio a firma' AS nombre FROM dual) s
ON (t.codigo = s.codigo)
WHEN MATCHED THEN UPDATE SET t.nombre = s.nombre, t.activo = 1
WHEN NOT MATCHED THEN INSERT (codigo, nombre) VALUES (s.codigo, s.nombre);

MERGE INTO tipo_movimiento t
USING (SELECT 'FIRMA_DOCUMENTO' AS codigo, 'Firma de documento' AS nombre FROM dual) s
ON (t.codigo = s.codigo)
WHEN MATCHED THEN UPDATE SET t.nombre = s.nombre, t.activo = 1
WHEN NOT MATCHED THEN INSERT (codigo, nombre) VALUES (s.codigo, s.nombre);

MERGE INTO tipo_movimiento t
USING (SELECT 'REVERSION_ESTADO_DOCUMENTO' AS codigo, 'Reversion de estado de documento' AS nombre FROM dual) s
ON (t.codigo = s.codigo)
WHEN MATCHED THEN UPDATE SET t.nombre = s.nombre, t.activo = 1
WHEN NOT MATCHED THEN INSERT (codigo, nombre) VALUES (s.codigo, s.nombre);

MERGE INTO estado_expediente t
USING (SELECT e.id_etapa, 'EN_VERIFICACION' AS codigo, 'En verificacion' AS nombre FROM etapa_expediente e WHERE e.codigo = 'VERIFICACION') s
ON (t.codigo = s.codigo)
WHEN MATCHED THEN UPDATE SET t.id_etapa = s.id_etapa, t.nombre = s.nombre, t.activo = 1
WHEN NOT MATCHED THEN INSERT (id_etapa, codigo, nombre) VALUES (s.id_etapa, s.codigo, s.nombre);

MERGE INTO estado_expediente t
USING (SELECT e.id_etapa, 'REQUIERE_CORRECCION' AS codigo, 'Requiere correccion' AS nombre FROM etapa_expediente e WHERE e.codigo = 'VERIFICACION') s
ON (t.codigo = s.codigo)
WHEN MATCHED THEN UPDATE SET t.id_etapa = s.id_etapa, t.nombre = s.nombre, t.activo = 1
WHEN NOT MATCHED THEN INSERT (id_etapa, codigo, nombre) VALUES (s.id_etapa, s.codigo, s.nombre);

MERGE INTO estado_expediente t
USING (SELECT e.id_etapa, 'DOCUMENTO_INCONSISTENTE' AS codigo, 'Documento inconsistente' AS nombre FROM etapa_expediente e WHERE e.codigo = 'VERIFICACION') s
ON (t.codigo = s.codigo)
WHEN MATCHED THEN UPDATE SET t.id_etapa = s.id_etapa, t.nombre = s.nombre, t.activo = 1
WHEN NOT MATCHED THEN INSERT (id_etapa, codigo, nombre) VALUES (s.id_etapa, s.codigo, s.nombre);

MERGE INTO estado_expediente t
USING (SELECT e.id_etapa, 'OBSERVADO' AS codigo, 'Observado' AS nombre FROM etapa_expediente e WHERE e.codigo = 'ANALISIS') s
ON (t.codigo = s.codigo)
WHEN MATCHED THEN UPDATE SET t.id_etapa = s.id_etapa, t.nombre = s.nombre, t.activo = 1
WHEN NOT MATCHED THEN INSERT (id_etapa, codigo, nombre) VALUES (s.id_etapa, s.codigo, s.nombre);

MERGE INTO estado_expediente t
USING (SELECT e.id_etapa, 'SUBSANADO' AS codigo, 'Subsanado' AS nombre FROM etapa_expediente e WHERE e.codigo = 'ANALISIS') s
ON (t.codigo = s.codigo)
WHEN MATCHED THEN UPDATE SET t.id_etapa = s.id_etapa, t.nombre = s.nombre, t.activo = 1
WHEN NOT MATCHED THEN INSERT (id_etapa, codigo, nombre) VALUES (s.id_etapa, s.codigo, s.nombre);

MERGE INTO estado_expediente t
USING (SELECT e.id_etapa, 'VERIFICADO' AS codigo, 'Verificado' AS nombre FROM etapa_expediente e WHERE e.codigo = 'VERIFICACION') s
ON (t.codigo = s.codigo)
WHEN MATCHED THEN UPDATE SET t.id_etapa = s.id_etapa, t.nombre = s.nombre, t.activo = 1
WHEN NOT MATCHED THEN INSERT (id_etapa, codigo, nombre) VALUES (s.id_etapa, s.codigo, s.nombre);

MERGE INTO estado_expediente t
USING (SELECT e.id_etapa, 'PARA_FIRMA' AS codigo, 'Para firma' AS nombre FROM etapa_expediente e WHERE e.codigo = 'FIRMA_EMISION') s
ON (t.codigo = s.codigo)
WHEN MATCHED THEN UPDATE SET t.id_etapa = s.id_etapa, t.nombre = s.nombre, t.activo = 1
WHEN NOT MATCHED THEN INSERT (id_etapa, codigo, nombre) VALUES (s.id_etapa, s.codigo, s.nombre);

MERGE INTO estado_expediente t
USING (SELECT e.id_etapa, 'FIRMADO' AS codigo, 'Firmado' AS nombre FROM etapa_expediente e WHERE e.codigo = 'FIRMA_EMISION') s
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
         'REENVIO_VERIFICACION' AS codigo_accion
  FROM flujo f
  JOIN etapa_expediente eo ON eo.codigo = 'ANALISIS'
  JOIN estado_expediente so ON so.codigo = 'ATENDIDO'
  JOIN etapa_expediente ed ON ed.codigo = 'VERIFICACION'
  JOIN estado_expediente sd ON sd.codigo = 'EN_VERIFICACION'
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
WHEN MATCHED THEN UPDATE SET t.activo = 0, t.nombre_accion = 'Ruta obsoleta reemplazada por ENVIO_VERIFICACION';

MERGE INTO flujo_transicion t
USING (
  SELECT f.id_flujo, eo.id_etapa AS id_etapa_origen, so.id_estado AS id_estado_origen,
         ed.id_etapa AS id_etapa_destino, sd.id_estado AS id_estado_destino,
         'REVISION_SUPERVISOR' AS codigo_accion
  FROM flujo f
  JOIN etapa_expediente eo ON eo.codigo = 'VERIFICACION'
  JOIN estado_expediente so ON so.codigo = 'VERIFICADO'
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
WHEN MATCHED THEN UPDATE SET t.activo = 0, t.nombre_accion = 'Ruta obsoleta reemplazada por ENVIO_FIRMA hacia PARA_FIRMA';

MERGE INTO flujo_transicion t
USING (
  SELECT f.id_flujo, eo.id_etapa AS id_etapa_origen, so.id_estado AS id_estado_origen,
         ed.id_etapa AS id_etapa_destino, sd.id_estado AS id_estado_destino,
         'ENVIO_VERIFICACION' AS codigo_accion, 'Enviar analisis a verificacion' AS nombre_accion,
         0 AS requiere_comentario, 1 AS requiere_documento
  FROM flujo f
  JOIN etapa_expediente eo ON eo.codigo = 'ANALISIS'
  JOIN estado_expediente so ON so.codigo = 'ATENDIDO'
  JOIN etapa_expediente ed ON ed.codigo = 'VERIFICACION'
  JOIN estado_expediente sd ON sd.codigo = 'EN_VERIFICACION'
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

MERGE INTO flujo_transicion t
USING (
  SELECT f.id_flujo, eo.id_etapa AS id_etapa_origen, so.id_estado AS id_estado_origen,
         ed.id_etapa AS id_etapa_destino, sd.id_estado AS id_estado_destino,
         'REGISTRO_OBSERVACION_VERIFICACION' AS codigo_accion, 'Registrar observacion de verificacion' AS nombre_accion,
         1 AS requiere_comentario, 1 AS requiere_documento
  FROM flujo f
  JOIN etapa_expediente eo ON eo.codigo = 'VERIFICACION'
  JOIN estado_expediente so ON so.codigo = 'EN_VERIFICACION'
  JOIN etapa_expediente ed ON ed.codigo = 'VERIFICACION'
  JOIN estado_expediente sd ON sd.codigo = 'REQUIERE_CORRECCION'
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

MERGE INTO flujo_transicion t
USING (
  SELECT f.id_flujo, eo.id_etapa AS id_etapa_origen, so.id_estado AS id_estado_origen,
         ed.id_etapa AS id_etapa_destino, sd.id_estado AS id_estado_destino,
         'REVERSION_ESTADO_DOCUMENTO' AS codigo_accion, 'Registrar documento inconsistente' AS nombre_accion,
         1 AS requiere_comentario, 1 AS requiere_documento
  FROM flujo f
  JOIN etapa_expediente eo ON eo.codigo = 'VERIFICACION'
  JOIN estado_expediente so ON so.codigo = 'EN_VERIFICACION'
  JOIN etapa_expediente ed ON ed.codigo = 'VERIFICACION'
  JOIN estado_expediente sd ON sd.codigo = 'DOCUMENTO_INCONSISTENTE'
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

MERGE INTO flujo_transicion t
USING (
  SELECT f.id_flujo, eo.id_etapa AS id_etapa_origen, so.id_estado AS id_estado_origen,
         ed.id_etapa AS id_etapa_destino, sd.id_estado AS id_estado_destino,
         'DEVOLUCION_A_ANALISIS' AS codigo_accion, 'Devolver expediente a analisis por correccion' AS nombre_accion,
         1 AS requiere_comentario, 1 AS requiere_documento
  FROM flujo f
  JOIN etapa_expediente eo ON eo.codigo = 'VERIFICACION'
  JOIN estado_expediente so ON so.codigo = 'REQUIERE_CORRECCION'
  JOIN etapa_expediente ed ON ed.codigo = 'ANALISIS'
  JOIN estado_expediente sd ON sd.codigo = 'OBSERVADO'
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

MERGE INTO flujo_transicion t
USING (
  SELECT f.id_flujo, eo.id_etapa AS id_etapa_origen, so.id_estado AS id_estado_origen,
         ed.id_etapa AS id_etapa_destino, sd.id_estado AS id_estado_destino,
         'DEVOLUCION_A_ANALISIS' AS codigo_accion, 'Devolver documento inconsistente a analisis' AS nombre_accion,
         1 AS requiere_comentario, 1 AS requiere_documento
  FROM flujo f
  JOIN etapa_expediente eo ON eo.codigo = 'VERIFICACION'
  JOIN estado_expediente so ON so.codigo = 'DOCUMENTO_INCONSISTENTE'
  JOIN etapa_expediente ed ON ed.codigo = 'ANALISIS'
  JOIN estado_expediente sd ON sd.codigo = 'OBSERVADO'
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

MERGE INTO flujo_transicion t
USING (
  SELECT f.id_flujo, eo.id_etapa AS id_etapa_origen, so.id_estado AS id_estado_origen,
         ed.id_etapa AS id_etapa_destino, sd.id_estado AS id_estado_destino,
         'CORRECCION_DOCUMENTO' AS codigo_accion, 'Registrar correccion de documento' AS nombre_accion,
         1 AS requiere_comentario, 1 AS requiere_documento
  FROM flujo f
  JOIN etapa_expediente eo ON eo.codigo = 'ANALISIS'
  JOIN estado_expediente so ON so.codigo = 'OBSERVADO'
  JOIN etapa_expediente ed ON ed.codigo = 'ANALISIS'
  JOIN estado_expediente sd ON sd.codigo = 'SUBSANADO'
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

MERGE INTO flujo_transicion t
USING (
  SELECT f.id_flujo, eo.id_etapa AS id_etapa_origen, so.id_estado AS id_estado_origen,
         ed.id_etapa AS id_etapa_destino, sd.id_estado AS id_estado_destino,
         'REENVIO_VERIFICACION' AS codigo_accion, 'Reenviar correccion a verificacion' AS nombre_accion,
         0 AS requiere_comentario, 1 AS requiere_documento
  FROM flujo f
  JOIN etapa_expediente eo ON eo.codigo = 'ANALISIS'
  JOIN estado_expediente so ON so.codigo = 'SUBSANADO'
  JOIN etapa_expediente ed ON ed.codigo = 'VERIFICACION'
  JOIN estado_expediente sd ON sd.codigo = 'EN_VERIFICACION'
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

MERGE INTO flujo_transicion t
USING (
  SELECT f.id_flujo, eo.id_etapa AS id_etapa_origen, so.id_estado AS id_estado_origen,
         ed.id_etapa AS id_etapa_destino, sd.id_estado AS id_estado_destino,
         'APROBACION_VERIFICACION' AS codigo_accion, 'Aprobar verificacion' AS nombre_accion,
         0 AS requiere_comentario, 1 AS requiere_documento
  FROM flujo f
  JOIN etapa_expediente eo ON eo.codigo = 'VERIFICACION'
  JOIN estado_expediente so ON so.codigo = 'EN_VERIFICACION'
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
WHEN MATCHED THEN UPDATE SET t.nombre_accion = s.nombre_accion, t.requiere_comentario = s.requiere_comentario, t.requiere_documento = s.requiere_documento, t.activo = 1
WHEN NOT MATCHED THEN INSERT (id_flujo, id_etapa_origen, id_estado_origen, id_etapa_destino, id_estado_destino, codigo_accion, nombre_accion, requiere_comentario, requiere_documento, activo)
VALUES (s.id_flujo, s.id_etapa_origen, s.id_estado_origen, s.id_etapa_destino, s.id_estado_destino, s.codigo_accion, s.nombre_accion, s.requiere_comentario, s.requiere_documento, 1);

MERGE INTO flujo_transicion t
USING (
  SELECT f.id_flujo, eo.id_etapa AS id_etapa_origen, so.id_estado AS id_estado_origen,
         ed.id_etapa AS id_etapa_destino, sd.id_estado AS id_estado_destino,
         'ENVIO_FIRMA' AS codigo_accion, 'Enviar expediente verificado a firma' AS nombre_accion,
         0 AS requiere_comentario, 1 AS requiere_documento
  FROM flujo f
  JOIN etapa_expediente eo ON eo.codigo = 'VERIFICACION'
  JOIN estado_expediente so ON so.codigo = 'VERIFICADO'
  JOIN etapa_expediente ed ON ed.codigo = 'FIRMA_EMISION'
  JOIN estado_expediente sd ON sd.codigo = 'PARA_FIRMA'
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

MERGE INTO flujo_transicion t
USING (
  SELECT f.id_flujo, eo.id_etapa AS id_etapa_origen, so.id_estado AS id_estado_origen,
         ed.id_etapa AS id_etapa_destino, sd.id_estado AS id_estado_destino,
         'FIRMA_DOCUMENTO' AS codigo_accion, 'Firmar documento' AS nombre_accion,
         0 AS requiere_comentario, 1 AS requiere_documento
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
WHERE codigo = 'VALIDACION';

SELECT id_flujo, codigo, nombre, activo
FROM flujo
WHERE codigo = 'SDRERC_TO_BE';

SELECT codigo, nombre, activo
FROM tipo_movimiento
WHERE codigo IN (
  'ENVIO_VERIFICACION',
  'REGISTRO_OBSERVACION_VERIFICACION',
  'DEVOLUCION_A_ANALISIS',
  'CORRECCION_DOCUMENTO',
  'REENVIO_VERIFICACION',
  'APROBACION_VERIFICACION',
  'ENVIO_FIRMA',
  'FIRMA_DOCUMENTO',
  'REVERSION_ESTADO_DOCUMENTO'
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
  AND ft.codigo_accion IN (
    'ENVIO_VERIFICACION',
    'REGISTRO_OBSERVACION_VERIFICACION',
    'REVERSION_ESTADO_DOCUMENTO',
    'DEVOLUCION_A_ANALISIS',
    'CORRECCION_DOCUMENTO',
    'REENVIO_VERIFICACION',
    'APROBACION_VERIFICACION',
    'ENVIO_FIRMA',
    'FIRMA_DOCUMENTO'
  )
ORDER BY ft.codigo_accion, etapa_origen, estado_origen, etapa_destino, estado_destino;

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

