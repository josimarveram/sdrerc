/* ============================================================
   SCRIPT 19 - Datos maestros de estabilizacion SDRERC V2
   Ejecutar conectado como SDRERC_APP.

   Alcance:
   - Poblar catalogos operativos usados por combos y acciones V2.
   - Idempotente mediante MERGE.
   - No crea tablas, no elimina datos y no reestructura el modelo.
   ============================================================ */

MERGE INTO tipo_documento_adjunto dst
USING (
  SELECT 'SOLICITUD' AS codigo, 'Solicitud' AS nombre FROM dual
  UNION ALL SELECT 'ACTA_REGISTRAL', 'Acta registral' FROM dual
  UNION ALL SELECT 'INFORME_ANALISIS', 'Informe de análisis' FROM dual
  UNION ALL SELECT 'INFORME_VERIFICACION', 'Informe de verificación' FROM dual
  UNION ALL SELECT 'RESOLUCION', 'Resolución' FROM dual
  UNION ALL SELECT 'CARGO_ACUSE', 'Cargo de acuse' FROM dual
  UNION ALL SELECT 'PUBLICACION', 'Publicación' FROM dual
  UNION ALL SELECT 'OTRO', 'Otro documento' FROM dual
) src
ON (UPPER(dst.codigo) = src.codigo)
WHEN MATCHED THEN UPDATE
  SET dst.nombre = src.nombre,
      dst.activo = 1,
      dst.modificado_en = SYSTIMESTAMP
WHEN NOT MATCHED THEN
  INSERT (codigo, nombre, activo)
  VALUES (src.codigo, src.nombre, 1);

MERGE INTO tipo_observacion dst
USING (
  SELECT 'DOCUMENTO_INCOMPLETO' AS codigo, 'Documento incompleto' AS nombre FROM dual
  UNION ALL SELECT 'INFORMACION_INCONSISTENTE', 'Información inconsistente' FROM dual
  UNION ALL SELECT 'REQUIERE_SUBSANACION', 'Requiere subsanación' FROM dual
  UNION ALL SELECT 'OBSERVACION_ADMINISTRATIVA', 'Observación administrativa' FROM dual
  UNION ALL SELECT 'DOCUMENTO_INCONSISTENTE', 'Documento inconsistente' FROM dual
  UNION ALL SELECT 'CORRECCION_ANALISIS', 'Corrección de análisis' FROM dual
) src
ON (UPPER(dst.codigo) = src.codigo)
WHEN MATCHED THEN UPDATE
  SET dst.nombre = src.nombre,
      dst.activo = 1,
      dst.modificado_en = SYSTIMESTAMP
WHEN NOT MATCHED THEN
  INSERT (codigo, nombre, activo)
  VALUES (src.codigo, src.nombre, 1);

MERGE INTO motivo_no_corresponde dst
USING (
  SELECT 'FUERA_COMPETENCIA' AS codigo, 'Fuera de competencia SDRERC' AS nombre FROM dual
  UNION ALL SELECT 'ACTA_NO_CORRESPONDE', 'Acta no corresponde al procedimiento' FROM dual
  UNION ALL SELECT 'PROCEDIMIENTO_NO_APLICA', 'Procedimiento no aplica' FROM dual
  UNION ALL SELECT 'DERIVACION_EXTERNA', 'Derivación externa requerida' FROM dual
  UNION ALL SELECT 'OTRO_MOTIVO', 'Otro motivo' FROM dual
) src
ON (UPPER(dst.codigo) = src.codigo)
WHEN MATCHED THEN UPDATE
  SET dst.nombre = src.nombre,
      dst.activo = 1,
      dst.modificado_en = SYSTIMESTAMP
WHEN NOT MATCHED THEN
  INSERT (codigo, nombre, activo)
  VALUES (src.codigo, src.nombre, 1);

MERGE INTO motivo_correccion dst
USING (
  SELECT 'DOCUMENTO_INCOMPLETO' AS codigo, 'Documento incompleto' AS nombre FROM dual
  UNION ALL SELECT 'DATOS_INCONSISTENTES', 'Datos inconsistentes' FROM dual
  UNION ALL SELECT 'REQUIERE_SUBSANACION', 'Requiere subsanación' FROM dual
  UNION ALL SELECT 'ERROR_MATERIAL', 'Error material' FROM dual
  UNION ALL SELECT 'DOCUMENTO_INCONSISTENTE', 'Documento inconsistente' FROM dual
  UNION ALL SELECT 'OTRO_MOTIVO', 'Otro motivo' FROM dual
) src
ON (UPPER(dst.codigo) = src.codigo)
WHEN MATCHED THEN UPDATE
  SET dst.nombre = src.nombre,
      dst.activo = 1,
      dst.modificado_en = SYSTIMESTAMP
WHEN NOT MATCHED THEN
  INSERT (codigo, nombre, activo)
  VALUES (src.codigo, src.nombre, 1);

MERGE INTO tipo_resolucion dst
USING (
  SELECT 'RESOLUCION_DIRECTORAL' AS codigo, 'Resolución directoral' AS nombre FROM dual
  UNION ALL SELECT 'RESOLUCION_SUBDIRECTORAL', 'Resolución subdirectoral' FROM dual
  UNION ALL SELECT 'RESOLUCION_ADMINISTRATIVA', 'Resolución administrativa' FROM dual
  UNION ALL SELECT 'OFICIO', 'Oficio' FROM dual
  UNION ALL SELECT 'INFORME', 'Informe' FROM dual
) src
ON (UPPER(dst.codigo) = src.codigo)
WHEN MATCHED THEN UPDATE
  SET dst.nombre = src.nombre,
      dst.activo = 1,
      dst.modificado_en = SYSTIMESTAMP
WHEN NOT MATCHED THEN
  INSERT (codigo, nombre, activo)
  VALUES (src.codigo, src.nombre, 1);

COMMIT;

SELECT 'TIPO_DOCUMENTO_ADJUNTO' AS catalogo, COUNT(*) AS activos
FROM tipo_documento_adjunto
WHERE activo = 1
UNION ALL
SELECT 'TIPO_OBSERVACION', COUNT(*)
FROM tipo_observacion
WHERE activo = 1
UNION ALL
SELECT 'MOTIVO_NO_CORRESPONDE', COUNT(*)
FROM motivo_no_corresponde
WHERE activo = 1
UNION ALL
SELECT 'MOTIVO_CORRECCION', COUNT(*)
FROM motivo_correccion
WHERE activo = 1
UNION ALL
SELECT 'TIPO_RESOLUCION', COUNT(*)
FROM tipo_resolucion
WHERE activo = 1
ORDER BY 1;
