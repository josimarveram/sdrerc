-- Patch idempotente: renombra la referencia externa anterior a expediente SGD.
-- Conserva los datos existentes cuando la columna anterior ya existe.
-- No ejecuta borrados, no trunca tablas y no elimina columnas.

DECLARE
    v_old_count NUMBER;
    v_new_count NUMBER;
BEGIN
    SELECT COUNT(*)
      INTO v_old_count
      FROM user_tab_columns
     WHERE table_name = 'EXPEDIENTE_SOLICITUD'
       AND column_name = 'NUMERO_EXPEDIENTE_DIGITAL_SITD';

    SELECT COUNT(*)
      INTO v_new_count
      FROM user_tab_columns
     WHERE table_name = 'EXPEDIENTE_SOLICITUD'
       AND column_name = 'NUMERO_EXPEDIENTE_SGD';

    IF v_old_count > 0 AND v_new_count = 0 THEN
        EXECUTE IMMEDIATE 'ALTER TABLE expediente_solicitud RENAME COLUMN numero_expediente_digital_sitd TO numero_expediente_sgd';
    ELSIF v_old_count = 0 AND v_new_count = 0 THEN
        EXECUTE IMMEDIATE 'ALTER TABLE expediente_solicitud ADD numero_expediente_sgd VARCHAR2(80 CHAR)';
    END IF;
END;
/


DECLARE
    v_old_column NUMBER;
    v_old_index NUMBER;
    v_new_index NUMBER;
BEGIN
    SELECT COUNT(*)
      INTO v_old_column
      FROM user_tab_columns
     WHERE table_name = 'EXPEDIENTE_SOLICITUD'
       AND column_name = 'NUMERO_EXPEDIENTE_DIGITAL_SITD';

    SELECT COUNT(*)
      INTO v_old_index
      FROM user_indexes
     WHERE index_name = 'IX_EXP_SOL_DIGITAL_SITD';

    SELECT COUNT(*)
      INTO v_new_index
      FROM user_indexes
     WHERE index_name = 'IX_EXP_SOL_SGD';

    IF v_old_column = 0 AND v_old_index > 0 AND v_new_index = 0 THEN
        EXECUTE IMMEDIATE 'ALTER INDEX ix_exp_sol_digital_sitd RENAME TO ix_exp_sol_sgd';
    ELSIF v_new_index = 0 THEN
        EXECUTE IMMEDIATE 'CREATE INDEX ix_exp_sol_sgd ON expediente_solicitud(numero_expediente_sgd)';
    END IF;
END;
/


COMMENT ON COLUMN expediente_solicitud.numero_expediente_sgd IS
    'Numero de expediente SGD, referencia externa institucional';

SELECT column_name, data_type, data_length
  FROM user_tab_columns
 WHERE table_name = 'EXPEDIENTE_SOLICITUD'
   AND column_name = 'NUMERO_EXPEDIENTE_SGD';
