-- Patch idempotente: referencia externa SITD en solicitudes de expediente.
-- No ejecuta borrados ni modifica datos existentes.

DECLARE
    v_count NUMBER;
BEGIN
    SELECT COUNT(*)
      INTO v_count
      FROM user_tab_columns
     WHERE table_name = 'EXPEDIENTE_SOLICITUD'
       AND column_name = 'NUMERO_EXPEDIENTE_DIGITAL_SITD';

    IF v_count = 0 THEN
        EXECUTE IMMEDIATE 'ALTER TABLE expediente_solicitud ADD numero_expediente_digital_sitd VARCHAR2(80 CHAR)';
    END IF;
END;
/

COMMENT ON COLUMN expediente_solicitud.numero_expediente_digital_sitd IS
    'Numero de expediente digital del Sistema Integral de Tramite Documentario (SITD), referencia externa institucional';

DECLARE
    v_count NUMBER;
BEGIN
    SELECT COUNT(*)
      INTO v_count
      FROM user_indexes
     WHERE index_name = 'IX_EXP_SOL_DIGITAL_SITD';

    IF v_count = 0 THEN
        EXECUTE IMMEDIATE 'CREATE INDEX ix_exp_sol_digital_sitd ON expediente_solicitud(numero_expediente_digital_sitd)';
    END IF;
END;
/

SELECT column_name, data_type, data_length
  FROM user_tab_columns
 WHERE table_name = 'EXPEDIENTE_SOLICITUD'
   AND column_name = 'NUMERO_EXPEDIENTE_DIGITAL_SITD';
