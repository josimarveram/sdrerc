DECLARE
  v_count NUMBER;
BEGIN
  SELECT COUNT(1)
    INTO v_count
    FROM user_tab_columns
   WHERE table_name = 'EXPEDIENTE_SOLICITUD'
     AND column_name = 'GRUPO_FAMILIAR';

  IF v_count = 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE expediente_solicitud ADD grupo_familiar NUMBER(1) DEFAULT 0 NOT NULL';
  END IF;

  EXECUTE IMMEDIATE q'[
    COMMENT ON COLUMN expediente_solicitud.grupo_familiar IS
    'Marca funcional de grupo familiar: 1=Si, 0=No. No bloquea registro ni importacion.'
  ]';
END;
/

DECLARE
  v_count NUMBER;
BEGIN
  SELECT COUNT(1)
    INTO v_count
    FROM user_tab_columns
   WHERE table_name = 'EXPEDIENTE_SOLICITUD'
     AND column_name = 'CRITERIO_GRUPO_FAMILIAR';

  IF v_count = 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE expediente_solicitud ADD criterio_grupo_familiar VARCHAR2(80)';
  END IF;

  EXECUTE IMMEDIATE q'[
    COMMENT ON COLUMN expediente_solicitud.criterio_grupo_familiar IS
    'Criterio de marca o deteccion de grupo familiar: MANUAL, EXCEL, COINCIDENCIA_APELLIDOS_EXCEL o COINCIDENCIA_APELLIDOS_BD.'
  ]';
END;
/

DECLARE
  v_count NUMBER;
BEGIN
  SELECT COUNT(1)
    INTO v_count
    FROM user_tab_columns
   WHERE table_name = 'EXPEDIENTE_SOLICITUD'
     AND column_name = 'OBSERVACION_GRUPO_FAMILIAR';

  IF v_count = 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE expediente_solicitud ADD observacion_grupo_familiar VARCHAR2(500)';
  END IF;

  EXECUTE IMMEDIATE q'[
    COMMENT ON COLUMN expediente_solicitud.observacion_grupo_familiar IS
    'Observacion o alerta no bloqueante asociada a grupo familiar.'
  ]';
END;
/

DECLARE
  v_count NUMBER;
BEGIN
  SELECT COUNT(1)
    INTO v_count
    FROM user_constraints
   WHERE table_name = 'EXPEDIENTE_SOLICITUD'
     AND constraint_name = 'CK_EXP_SOL_GRUPO_FAMILIAR';

  IF v_count = 0 THEN
    EXECUTE IMMEDIATE q'[
      ALTER TABLE expediente_solicitud
      ADD CONSTRAINT ck_exp_sol_grupo_familiar
      CHECK (grupo_familiar IN (0, 1))
    ]';
  END IF;
END;
/

SELECT column_name, data_type, data_length, nullable, data_default
  FROM user_tab_columns
 WHERE table_name = 'EXPEDIENTE_SOLICITUD'
   AND column_name IN (
       'GRUPO_FAMILIAR',
       'CRITERIO_GRUPO_FAMILIAR',
       'OBSERVACION_GRUPO_FAMILIAR'
   )
 ORDER BY column_id;

SELECT constraint_name, constraint_type, status
  FROM user_constraints
 WHERE table_name = 'EXPEDIENTE_SOLICITUD'
   AND constraint_name = 'CK_EXP_SOL_GRUPO_FAMILIAR';
