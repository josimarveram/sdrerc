/* ============================================================
   SCRIPT 21 - Tipos de documento para Analisis SDRERC V2
   Ejecutar conectado como SDRERC_APP.

   Alcance:
   - Mantener las opciones del combo Tipo de documentos analizados.
   - Mantener PROVEIDO como tipo tecnico para EXPEDIENTE_DOCUMENTO.
   - Idempotente mediante MERGE.
   - No elimina ni desactiva tipos compartidos por otros modulos.
   ============================================================ */

MERGE INTO tipo_documento_adjunto dst
USING (
  SELECT 'ANALISIS_01_CARTA_EDICTO' AS codigo, 'Carta edicto' AS nombre FROM dual
  UNION ALL SELECT 'ANALISIS_02_CARTA_FALTA_SUSTENTO', 'Carta falta sustento' FROM dual
  UNION ALL SELECT 'ANALISIS_03_CARTA_INDAGATORIO', 'Carta indagatorio' FROM dual
  UNION ALL SELECT 'ANALISIS_04_CARTA_PRETENSION', 'Carta pretensión' FROM dual
  UNION ALL SELECT 'ANALISIS_05_OFICIO_OREC_CANCELACION', 'Oficio orec cancelación' FROM dual
  UNION ALL SELECT 'ANALISIS_06_OFICIO_OREC_RECONSTITUCION', 'Oficio orec reconstitución' FROM dual
  UNION ALL SELECT 'ANALISIS_07_CARTA_NOTIFICACION_PROCEDENTE', 'Carta notificación procedente' FROM dual
  UNION ALL SELECT 'ANALISIS_08_CARTA_NOTIFICACION_IMPROCEDENTE', 'Carta notificación improcedente' FROM dual
  UNION ALL SELECT 'ANALISIS_09_MEMORANDUM', 'Memorándum' FROM dual
  UNION ALL SELECT 'ANALISIS_10_OFICIO', 'Oficio' FROM dual
  UNION ALL SELECT 'ANALISIS_11_RESOLUCIONES', 'Resoluciones' FROM dual
  UNION ALL SELECT 'ANALISIS_12_INFORMES', 'Informes' FROM dual
  UNION ALL SELECT 'PROVEIDO', 'Proveído' FROM dual
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

SELECT codigo, nombre, activo
FROM tipo_documento_adjunto
WHERE codigo LIKE 'ANALISIS_%'
   OR codigo = 'PROVEIDO'
ORDER BY codigo;
