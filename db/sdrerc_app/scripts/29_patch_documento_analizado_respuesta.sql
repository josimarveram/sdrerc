DECLARE
  v_count NUMBER;
BEGIN
  SELECT COUNT(1)
    INTO v_count
    FROM user_tab_columns
   WHERE table_name = 'EXPEDIENTE_DOCUMENTO_ANALIZADO'
     AND column_name = 'NOTIFICADO';

  IF v_count = 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE expediente_documento_analizado ADD notificado NUMBER(1) DEFAULT 0 NOT NULL';
  END IF;

  EXECUTE IMMEDIATE q'[
    COMMENT ON COLUMN expediente_documento_analizado.notificado IS
    'Indica si el documento analizado/carta fue notificado: 1=Si, 0=No.'
  ]';
END;
/

DECLARE
  v_count NUMBER;
BEGIN
  SELECT COUNT(1)
    INTO v_count
    FROM user_tab_columns
   WHERE table_name = 'EXPEDIENTE_DOCUMENTO_ANALIZADO'
     AND column_name = 'FECHA_ACUSE';

  IF v_count = 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE expediente_documento_analizado ADD fecha_acuse DATE';
  END IF;

  EXECUTE IMMEDIATE q'[
    COMMENT ON COLUMN expediente_documento_analizado.fecha_acuse IS
    'Fecha de acuse de notificacion del documento analizado/carta.'
  ]';
END;
/

DECLARE
  v_count NUMBER;
BEGIN
  SELECT COUNT(1)
    INTO v_count
    FROM user_tab_columns
   WHERE table_name = 'EXPEDIENTE_DOCUMENTO_ANALIZADO'
     AND column_name = 'REQUIERE_RESPUESTA';

  IF v_count = 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE expediente_documento_analizado ADD requiere_respuesta NUMBER(1) DEFAULT 0 NOT NULL';
  END IF;

  EXECUTE IMMEDIATE q'[
    COMMENT ON COLUMN expediente_documento_analizado.requiere_respuesta IS
    'Indica si el documento requiere respuesta del administrado segun Analisis: 1=Si, 0=No.'
  ]';
END;
/

DECLARE
  v_count NUMBER;
BEGIN
  SELECT COUNT(1)
    INTO v_count
    FROM user_tab_columns
   WHERE table_name = 'EXPEDIENTE_DOCUMENTO_ANALIZADO'
     AND column_name = 'CONFIRMACION_RESPUESTA';

  IF v_count = 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE expediente_documento_analizado ADD confirmacion_respuesta VARCHAR2(20)';
  END IF;

  EXECUTE IMMEDIATE q'[
    COMMENT ON COLUMN expediente_documento_analizado.confirmacion_respuesta IS
    'Confirmacion de respuesta recibida: SI, NO o PENDIENTE.'
  ]';
END;
/

DECLARE
  v_count NUMBER;
BEGIN
  SELECT COUNT(1)
    INTO v_count
    FROM user_tab_columns
   WHERE table_name = 'EXPEDIENTE_DOCUMENTO_ANALIZADO'
     AND column_name = 'FECHA_RESPUESTA';

  IF v_count = 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE expediente_documento_analizado ADD fecha_respuesta DATE';
  END IF;

  EXECUTE IMMEDIATE q'[
    COMMENT ON COLUMN expediente_documento_analizado.fecha_respuesta IS
    'Fecha de recepcion de la respuesta del administrado.'
  ]';
END;
/

DECLARE
  v_count NUMBER;
BEGIN
  SELECT COUNT(1)
    INTO v_count
    FROM user_tab_columns
   WHERE table_name = 'EXPEDIENTE_DOCUMENTO_ANALIZADO'
     AND column_name = 'NUMERO_HOJA_ENVIO_RESPUESTA';

  IF v_count = 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE expediente_documento_analizado ADD numero_hoja_envio_respuesta VARCHAR2(120)';
  END IF;

  EXECUTE IMMEDIATE q'[
    COMMENT ON COLUMN expediente_documento_analizado.numero_hoja_envio_respuesta IS
    'Numero de hoja de envio de la respuesta asociada al documento analizado/carta.'
  ]';
END;
/

DECLARE
  v_count NUMBER;
BEGIN
  SELECT COUNT(1)
    INTO v_count
    FROM user_constraints
   WHERE table_name = 'EXPEDIENTE_DOCUMENTO_ANALIZADO'
     AND constraint_name = 'CK_EXP_DOC_ANA_NOTIFICADO';

  IF v_count = 0 THEN
    EXECUTE IMMEDIATE q'[
      ALTER TABLE expediente_documento_analizado
      ADD CONSTRAINT ck_exp_doc_ana_notificado
      CHECK (notificado IN (0, 1))
    ]';
  END IF;
END;
/

DECLARE
  v_count NUMBER;
BEGIN
  SELECT COUNT(1)
    INTO v_count
    FROM user_constraints
   WHERE table_name = 'EXPEDIENTE_DOCUMENTO_ANALIZADO'
     AND constraint_name = 'CK_EXP_DOC_ANA_REQ_RESP';

  IF v_count = 0 THEN
    EXECUTE IMMEDIATE q'[
      ALTER TABLE expediente_documento_analizado
      ADD CONSTRAINT ck_exp_doc_ana_req_resp
      CHECK (requiere_respuesta IN (0, 1))
    ]';
  END IF;
END;
/

DECLARE
  v_count NUMBER;
BEGIN
  SELECT COUNT(1)
    INTO v_count
    FROM user_constraints
   WHERE table_name = 'EXPEDIENTE_DOCUMENTO_ANALIZADO'
     AND constraint_name = 'CK_EXP_DOC_ANA_CONF_RESP';

  IF v_count = 0 THEN
    EXECUTE IMMEDIATE q'[
      ALTER TABLE expediente_documento_analizado
      ADD CONSTRAINT ck_exp_doc_ana_conf_resp
      CHECK (confirmacion_respuesta IS NULL OR confirmacion_respuesta IN ('SI', 'NO', 'PENDIENTE'))
    ]';
  END IF;
END;
/

SELECT column_name, data_type, data_length, nullable, data_default
  FROM user_tab_columns
 WHERE table_name = 'EXPEDIENTE_DOCUMENTO_ANALIZADO'
   AND column_name IN (
       'NOTIFICADO',
       'FECHA_ACUSE',
       'REQUIERE_RESPUESTA',
       'CONFIRMACION_RESPUESTA',
       'FECHA_RESPUESTA',
       'NUMERO_HOJA_ENVIO_RESPUESTA'
   )
 ORDER BY column_id;

SELECT constraint_name, constraint_type, status
  FROM user_constraints
 WHERE table_name = 'EXPEDIENTE_DOCUMENTO_ANALIZADO'
   AND constraint_name IN (
       'CK_EXP_DOC_ANA_NOTIFICADO',
       'CK_EXP_DOC_ANA_REQ_RESP',
       'CK_EXP_DOC_ANA_CONF_RESP'
   )
 ORDER BY constraint_name;
