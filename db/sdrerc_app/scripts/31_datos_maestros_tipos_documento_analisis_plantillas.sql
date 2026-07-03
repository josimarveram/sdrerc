/* ============================================================
   SCRIPT 31 - Tipos de documento de Analisis desde plantillas
   Ejecutar conectado como SDRERC_APP.

   Alcance:
   - Actualizar el combo Tipo del panel de Analisis con nombres
     derivados de docs/plantillas, sin extensiones ni guiones bajos.
   - Mantener PROVEIDO como tipo tecnico fuera del combo de Analisis.
   - Desactivar opciones ANALISIS_% anteriores para que no se sigan
     mostrando en el combo.
   - Idempotente mediante MERGE y UPDATE controlado.
   - No elimina registros ni modifica documentos ya registrados.
   ============================================================ */

MERGE INTO tipo_documento_adjunto dst
USING (
  SELECT 'ANALISIS_DOC_01_CARTA_ABANDONO' AS codigo, 'Carta Abandono' AS nombre FROM dual
  UNION ALL SELECT 'ANALISIS_DOC_02_CARTA_EDICTO', 'Carta Edicto' FROM dual
  UNION ALL SELECT 'ANALISIS_DOC_03_CARTA_FALTA_SUSTENTO', 'Carta Falta Sustento' FROM dual
  UNION ALL SELECT 'ANALISIS_DOC_04_CARTA_INDAGATORIO', 'Carta Indagatorio' FROM dual
  UNION ALL SELECT 'ANALISIS_DOC_05_CARTA_PRECISAR_PRETENSION', 'Carta Precisar Pretensión' FROM dual
  UNION ALL SELECT 'ANALISIS_DOC_06_INFORME_ABANDONO', 'Informe Abandono' FROM dual
  UNION ALL SELECT 'ANALISIS_DOC_07_INFORME_CANCELACION', 'Informe Cancelación' FROM dual
  UNION ALL SELECT 'ANALISIS_DOC_08_INFORME_RECONSTITUCION', 'Informe Reconstitución' FROM dual
  UNION ALL SELECT 'ANALISIS_DOC_09_INFORME_RECTIFICACION', 'Informe Rectificación' FROM dual
  UNION ALL SELECT 'ANALISIS_DOC_10_OFICIO_INDAGATORIO_CANCELACION', 'Oficio Indagatorio Cancelación' FROM dual
  UNION ALL SELECT 'ANALISIS_DOC_11_OFICIO_RECONSTITUCION', 'Oficio Reconstitución' FROM dual
  UNION ALL SELECT 'ANALISIS_DOC_12_RESOLUCION_ABANDONO', 'Resolución Abandono' FROM dual
  UNION ALL SELECT 'ANALISIS_DOC_13_RESOLUCION_CANCELACION', 'Resolución Cancelación' FROM dual
  UNION ALL SELECT 'ANALISIS_DOC_14_RESOLUCION_ERROR_MATERIAL', 'Resolución Error Material' FROM dual
  UNION ALL SELECT 'ANALISIS_DOC_15_RESOLUCION_RECONSTITUCION', 'Resolución Reconstitución' FROM dual
  UNION ALL SELECT 'ANALISIS_DOC_16_RESOLUCION_RECTIFICACION', 'Resolución Rectificación' FROM dual
  UNION ALL SELECT 'ANALISIS_DOC_17_CARTA_IMPROCEDENTE', 'Carta Improcedente' FROM dual
  UNION ALL SELECT 'ANALISIS_DOC_18_CARTA_PROCEDENTE', 'Carta Procedente' FROM dual
  UNION ALL SELECT 'ANALISIS_DOC_19_CARTA_PROCEDENTE_EN_PARTE', 'Carta Procedente en Parte' FROM dual
) src
ON (UPPER(dst.codigo) = src.codigo)
WHEN MATCHED THEN UPDATE
  SET dst.nombre = src.nombre,
      dst.activo = 1,
      dst.modificado_en = SYSTIMESTAMP
WHEN NOT MATCHED THEN
  INSERT (codigo, nombre, activo)
  VALUES (src.codigo, src.nombre, 1);

UPDATE tipo_documento_adjunto dst
   SET dst.activo = 0,
       dst.modificado_en = SYSTIMESTAMP
 WHERE dst.codigo LIKE 'ANALISIS_%'
   AND dst.codigo NOT IN (
     SELECT codigo
       FROM (
         SELECT 'ANALISIS_DOC_01_CARTA_ABANDONO' AS codigo FROM dual
         UNION ALL SELECT 'ANALISIS_DOC_02_CARTA_EDICTO' FROM dual
         UNION ALL SELECT 'ANALISIS_DOC_03_CARTA_FALTA_SUSTENTO' FROM dual
         UNION ALL SELECT 'ANALISIS_DOC_04_CARTA_INDAGATORIO' FROM dual
         UNION ALL SELECT 'ANALISIS_DOC_05_CARTA_PRECISAR_PRETENSION' FROM dual
         UNION ALL SELECT 'ANALISIS_DOC_06_INFORME_ABANDONO' FROM dual
         UNION ALL SELECT 'ANALISIS_DOC_07_INFORME_CANCELACION' FROM dual
         UNION ALL SELECT 'ANALISIS_DOC_08_INFORME_RECONSTITUCION' FROM dual
         UNION ALL SELECT 'ANALISIS_DOC_09_INFORME_RECTIFICACION' FROM dual
         UNION ALL SELECT 'ANALISIS_DOC_10_OFICIO_INDAGATORIO_CANCELACION' FROM dual
         UNION ALL SELECT 'ANALISIS_DOC_11_OFICIO_RECONSTITUCION' FROM dual
         UNION ALL SELECT 'ANALISIS_DOC_12_RESOLUCION_ABANDONO' FROM dual
         UNION ALL SELECT 'ANALISIS_DOC_13_RESOLUCION_CANCELACION' FROM dual
         UNION ALL SELECT 'ANALISIS_DOC_14_RESOLUCION_ERROR_MATERIAL' FROM dual
         UNION ALL SELECT 'ANALISIS_DOC_15_RESOLUCION_RECONSTITUCION' FROM dual
         UNION ALL SELECT 'ANALISIS_DOC_16_RESOLUCION_RECTIFICACION' FROM dual
         UNION ALL SELECT 'ANALISIS_DOC_17_CARTA_IMPROCEDENTE' FROM dual
         UNION ALL SELECT 'ANALISIS_DOC_18_CARTA_PROCEDENTE' FROM dual
         UNION ALL SELECT 'ANALISIS_DOC_19_CARTA_PROCEDENTE_EN_PARTE' FROM dual
       )
   );

COMMIT;

SELECT codigo, nombre, activo
  FROM tipo_documento_adjunto
 WHERE codigo LIKE 'ANALISIS_%'
    OR codigo = 'PROVEIDO'
 ORDER BY codigo;
