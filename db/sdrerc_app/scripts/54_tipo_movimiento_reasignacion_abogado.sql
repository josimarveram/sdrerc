/* ============================================================
   SCRIPT 54 - Tipo de movimiento REASIGNACION_ABOGADO
   Ejecutar conectado como SDRERC_APP.

   Contexto: nueva funcionalidad de reasignacion en el panel Asociar
   de Asignacion. Al reasignar un expediente a un abogado distinto se
   desactiva (activa=0) la fila anterior en EXPEDIENTE_ASIGNACION y se
   inserta una nueva con es_reasignacion_excepcional=1 y una hoja de
   envio NUEVA (no se sobreescribe la anterior). Se conserva todo el
   historial para poder listarlo en una grilla.

   Idempotente mediante MERGE.
   ============================================================ */

MERGE INTO tipo_movimiento dst
USING (
  SELECT 'REASIGNACION_ABOGADO' AS codigo, 'Reasignacion de expediente a abogado' AS nombre FROM dual
) src
ON (UPPER(dst.codigo) = src.codigo)
WHEN MATCHED THEN UPDATE
  SET dst.nombre = src.nombre,
      dst.activo = 1
WHEN NOT MATCHED THEN
  INSERT (codigo, nombre, activo)
  VALUES (src.codigo, src.nombre, 1);

COMMIT;

SELECT codigo, nombre, activo FROM tipo_movimiento WHERE codigo = 'REASIGNACION_ABOGADO';
