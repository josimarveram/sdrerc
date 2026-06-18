DECLARE
  v_count NUMBER;
BEGIN
  SELECT COUNT(1)
    INTO v_count
    FROM user_tab_columns
   WHERE table_name = 'EXPEDIENTE_ASIGNACION'
     AND column_name = 'NUMERO_HOJA_ENVIO';

  IF v_count = 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE expediente_asignacion ADD numero_hoja_envio VARCHAR2(120)';
  END IF;

  EXECUTE IMMEDIATE q'[
    COMMENT ON COLUMN expediente_asignacion.numero_hoja_envio IS
    'Numero de hoja de envio registrada al confirmar la asignacion del expediente.'
  ]';

  SELECT COUNT(1)
    INTO v_count
    FROM user_indexes
   WHERE index_name = 'UX_EXP_ASIG_HOJA_ENVIO_ACT';

  IF v_count = 0 THEN
    EXECUTE IMMEDIATE q'[
      CREATE UNIQUE INDEX ux_exp_asig_hoja_envio_act
      ON expediente_asignacion (
        CASE
          WHEN numero_hoja_envio IS NOT NULL AND activo = 1
          THEN UPPER(TRIM(numero_hoja_envio))
        END
      )
    ]';
  END IF;
END;
/

SELECT column_name, data_type, data_length, nullable
  FROM user_tab_columns
 WHERE table_name = 'EXPEDIENTE_ASIGNACION'
   AND column_name = 'NUMERO_HOJA_ENVIO';

SELECT index_name, uniqueness, status
  FROM user_indexes
 WHERE index_name = 'UX_EXP_ASIG_HOJA_ENVIO_ACT';
