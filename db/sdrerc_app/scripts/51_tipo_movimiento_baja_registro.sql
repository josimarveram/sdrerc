/* ============================================================
   SCRIPT 51 - Tipo de movimiento BAJA_REGISTRO
   Ejecutar conectado como SDRERC_APP.

   Contexto: se agrega la opcion "Eliminar" (baja logica, activo=0)
   para registros en Registro/Recepcion y su replica en Asignacion,
   restringida a expedientes en REGISTRO/REGISTRADO sin asignacion a
   abogado. El movimiento queda registrado en EXPEDIENTE_HISTORIAL
   para trazabilidad; no se borra ninguna fila.

   Idempotente mediante MERGE.
   ============================================================ */

MERGE INTO tipo_movimiento dst
USING (
  SELECT 'BAJA_REGISTRO' AS codigo, 'Baja logica de registro' AS nombre FROM dual
) src
ON (UPPER(dst.codigo) = src.codigo)
WHEN MATCHED THEN UPDATE
  SET dst.nombre = src.nombre,
      dst.activo = 1
WHEN NOT MATCHED THEN
  INSERT (codigo, nombre, activo)
  VALUES (src.codigo, src.nombre, 1);

COMMIT;

SELECT codigo, nombre, activo FROM tipo_movimiento WHERE codigo = 'BAJA_REGISTRO';
