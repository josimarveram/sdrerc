/* ============================================================
   SCRIPT 32 - Numero de documento en documentos analizados
   Ejecutar conectado como SDRERC_APP.

   Alcance:
   - Agrega EXPEDIENTE_DOCUMENTO_ANALIZADO.NUMERO_DOCUMENTO.
   - Permite registrar el numero del documento analizado desde el
     panel de Analisis.
   - Idempotente y no modifica datos historicos existentes.
   ============================================================ */

DECLARE
  v_count NUMBER;
BEGIN
  SELECT COUNT(1)
    INTO v_count
    FROM user_tab_columns
   WHERE table_name = 'EXPEDIENTE_DOCUMENTO_ANALIZADO'
     AND column_name = 'NUMERO_DOCUMENTO';

  IF v_count = 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE expediente_documento_analizado ADD (numero_documento VARCHAR2(120))';
  END IF;
END;


COMMENT ON COLUMN expediente_documento_analizado.numero_documento
  IS 'Numero del documento analizado registrado desde el modulo Analisis.';

SELECT column_name, data_type, data_length, nullable
  FROM user_tab_columns
 WHERE table_name = 'EXPEDIENTE_DOCUMENTO_ANALIZADO'
   AND column_name = 'NUMERO_DOCUMENTO';
