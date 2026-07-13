/* ============================================================
   SCRIPT 58 - Codigos de tipo_movimiento para asignacion/reasignacion
   de documentos de Notificacion.
   Ejecutar conectado como SDRERC_APP.

   Contexto: el Panel de Asignacion de la Bandeja Asignacion de
   Notificacion replica el diseno del Panel de Asignacion del modulo
   Asignacion, incluyendo el bloque "Historial de asignacion /
   reasignacion". Ese historial se registra en la tabla generica
   expediente_historial (ya existente, sin crear tablas nuevas) con
   tabla_relacionada = 'EXPEDIENTE_DOCUMENTO_ANALIZADO' y
   id_registro_relacionado = id_documento_analizado. Este script solo
   agrega los codigos de tipo_movimiento necesarios para distinguir
   asignacion inicial de reasignacion; no crea tablas ni modifica
   expediente_documento_analizado.

   Idempotente: usa MERGE, no falla si los codigos ya existen.
   ============================================================ */

MERGE INTO tipo_movimiento dst
USING (
  SELECT 'ASIGNACION_NOTIFICACION' AS codigo,
         'Asignacion de documento a validador de notificacion' AS nombre FROM dual
  UNION ALL
  SELECT 'REASIGNACION_NOTIFICACION',
         'Reasignacion de documento a validador de notificacion' FROM dual
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

/* ============================================================
   Verificacion posterior
   ============================================================ */

SELECT codigo, nombre, activo
FROM tipo_movimiento
WHERE codigo IN ('ASIGNACION_NOTIFICACION', 'REASIGNACION_NOTIFICACION')
ORDER BY codigo;
