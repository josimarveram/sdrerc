/* ============================================================
   SCRIPT 41 - Tipo de documento "Carta de Respuesta" para Analisis
   Ejecutar conectado como SDRERC_APP.

   Alcance:
   - Agregar el tipo de documento "Carta de Respuesta" al combo
     "Tipo documento" de la grilla de documentos analizados
     (padre e hijo), usado tipicamente en documentos relacionados
     de la bandeja de respuesta.
   - Idempotente mediante MERGE.
   - No elimina ni desactiva otros tipos ANALISIS_% existentes.
   - No ejecutar automaticamente: requiere autorizacion explicita.
   ============================================================ */

MERGE INTO tipo_documento_adjunto dst
USING (
  SELECT 'ANALISIS_DOC_20_CARTA_RESPUESTA' AS codigo, 'Carta de Respuesta' AS nombre FROM dual
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
