/* ---------------------------------------------------------------------------
   SDRERC_APP - Canal OR Pasivo y fecha de recepcion SDRERC

   Contexto:
   - FECHA_RECEPCION existente en EXPEDIENTE_SOLICITUD se mantiene como fecha
     de solicitud/origen y sigue siendo la base de plazos, filtros y vencimiento.
   - FECHA_RECEPCION_SDRERC registra la fecha en que SDRERC recibe la solicitud.
     Es un dato adicional para reportes y no recalcula plazos historicos.
   --------------------------------------------------------------------------- */

DECLARE
  v_count NUMBER;
BEGIN
  SELECT COUNT(1)
    INTO v_count
    FROM user_tab_columns
   WHERE table_name = 'EXPEDIENTE_SOLICITUD'
     AND column_name = 'FECHA_RECEPCION_SDRERC';

  IF v_count = 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE expediente_solicitud ADD fecha_recepcion_sdrerc DATE';
  END IF;

  EXECUTE IMMEDIATE q'[
    COMMENT ON COLUMN expediente_solicitud.fecha_recepcion_sdrerc IS
    'Fecha en que SDRERC recibe la solicitud. Dato adicional para reportes; no reemplaza la fecha de solicitud/origen ni recalcula plazos.'
  ]';
END;


MERGE INTO canal_recepcion dst
USING (
  SELECT 'OR_PASIVO' AS codigo, 'OR Pasivo' AS nombre FROM dual
) src
ON (UPPER(dst.codigo) = src.codigo)
WHEN MATCHED THEN UPDATE
  SET dst.nombre = src.nombre,
      dst.activo = 1,
      dst.modificado_en = SYSTIMESTAMP
WHEN NOT MATCHED THEN
  INSERT (codigo, nombre, activo)
  VALUES (src.codigo, src.nombre, 1);

SELECT column_name, data_type, nullable
  FROM user_tab_columns
 WHERE table_name = 'EXPEDIENTE_SOLICITUD'
   AND column_name = 'FECHA_RECEPCION_SDRERC';

SELECT codigo, nombre, activo
  FROM canal_recepcion
 WHERE codigo = 'OR_PASIVO';
