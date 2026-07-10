/* ============================================================
   SCRIPT 46 - Tipo de notificación "Publicación" (3er intento)
   Ejecutar conectado como SDRERC_APP.

   Alcance:
   - Agrega el tipo de notificación PUBLICACION, usado como 3er
     intento en la Bandeja de Publicación (después de los intentos
     VIRTUAL y PRESENCIAL_2 ya usados en la Bandeja de Notificación).
   - Idempotente mediante MERGE.
   ============================================================ */

MERGE INTO tipo_notificacion dst
USING (
  SELECT 'PUBLICACION' AS codigo, 'Publicación' AS nombre FROM dual
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

SELECT codigo, nombre, activo FROM tipo_notificacion ORDER BY codigo;
