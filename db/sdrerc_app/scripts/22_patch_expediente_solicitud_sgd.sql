-- Patch idempotente: referencia externa SGD en solicitudes de expediente.
-- No ejecuta borrados ni modifica datos existentes.

DECLARE
    v_count NUMBER;
BEGIN
    SELECT COUNT(*)
      INTO v_count
      FROM user_tab_columns
     WHERE table_name = 'EXPEDIENTE_SOLICITUD'
       AND column_name = 'NUMERO_EXPEDIENTE_SGD';

    IF v_count = 0 THEN
        EXECUTE IMMEDIATE 'ALTER TABLE expediente_solicitud ADD numero_expediente_sgd VARCHAR2(80 CHAR)';
    END IF;
END;
/


COMMENT ON COLUMN expediente_solicitud.numero_expediente_sgd IS
    'Numero de expediente SGD, referencia externa institucional';

DECLARE
    v_count NUMBER;
BEGIN
    SELECT COUNT(*)
      INTO v_count
      FROM user_indexes
     WHERE index_name = 'IX_EXP_SOL_SGD';

    IF v_count = 0 THEN
        EXECUTE IMMEDIATE 'CREATE INDEX IX_EXP_SOL_SGD ON expediente_solicitud(numero_expediente_sgd)';
    END IF;
END;
/


SELECT column_name, data_type, data_length
  FROM user_tab_columns
 WHERE table_name = 'EXPEDIENTE_SOLICITUD'
   AND column_name = 'NUMERO_EXPEDIENTE_SGD';
