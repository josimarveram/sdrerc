/* ============================================================
   SCRIPT 53 - Resultado de analisis "Precisar pretension"
   Ejecutar conectado como SDRERC_APP.

   Agrega la opcion "Precisar pretension" al combo de Resultado del
   panel de Analisis (tabla TIPO_RESULTADO_EVALUACION).
   Idempotente mediante MERGE.
   ============================================================ */

MERGE INTO tipo_resultado_evaluacion dst
USING (
  SELECT 'PRECISAR_PRETENSION' AS codigo, 'Precisar pretensión' AS nombre FROM dual
) src
ON (UPPER(dst.codigo) = src.codigo)
WHEN MATCHED THEN UPDATE
  SET dst.nombre = src.nombre,
      dst.activo = 1
WHEN NOT MATCHED THEN
  INSERT (codigo, nombre, activo)
  VALUES (src.codigo, src.nombre, 1);

COMMIT;

SELECT codigo, nombre, activo FROM tipo_resultado_evaluacion ORDER BY codigo;
