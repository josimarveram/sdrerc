SET DEFINE OFF;

DECLARE
  v_count NUMBER;
BEGIN
  SELECT COUNT(1)
    INTO v_count
    FROM user_tab_columns
   WHERE table_name = 'EXPEDIENTE_DOCUMENTO_ANALIZADO'
     AND column_name = 'DETALLE_OBSERVACION';

  IF v_count = 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE expediente_documento_analizado ADD (detalle_observacion VARCHAR2(1000))';
  END IF;
END;
/

COMMENT ON COLUMN expediente_documento_analizado.detalle_observacion IS
  'Detalle de observacion registrado cuando el documento analizado queda en estado Observado.';

SELECT column_name, data_type, data_length, nullable
  FROM user_tab_columns
 WHERE table_name = 'EXPEDIENTE_DOCUMENTO_ANALIZADO'
   AND column_name = 'DETALLE_OBSERVACION';
