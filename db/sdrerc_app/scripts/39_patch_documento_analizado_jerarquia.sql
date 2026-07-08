/* ============================================================
   SCRIPT 39 - Jerarquia de documentos analizados

   Objetivo:
   - Permitir documentos padre e hijos dentro de
     EXPEDIENTE_DOCUMENTO_ANALIZADO.
   - No elimina ni actualiza expedientes historicos.
   - No mueve expedientes de etapa.
   - Script idempotente. No ejecutar automaticamente.
   ============================================================ */

DECLARE
  v_count NUMBER;
BEGIN
  SELECT COUNT(1)
    INTO v_count
    FROM user_tab_columns
   WHERE table_name = 'EXPEDIENTE_DOCUMENTO_ANALIZADO'
     AND column_name = 'ID_DOCUMENTO_PADRE';

  IF v_count = 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE expediente_documento_analizado ADD id_documento_padre NUMBER';
  END IF;

  EXECUTE IMMEDIATE q'[
    COMMENT ON COLUMN expediente_documento_analizado.id_documento_padre IS
    'Documento analizado padre. NULL indica documento principal; valor indica documento hijo relacionado.'
  ]';
END;


DECLARE
  v_count NUMBER;
BEGIN
  SELECT COUNT(1)
    INTO v_count
    FROM user_tab_columns
   WHERE table_name = 'EXPEDIENTE_DOCUMENTO_ANALIZADO'
     AND column_name = 'NIVEL';

  IF v_count = 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE expediente_documento_analizado ADD nivel NUMBER(1) DEFAULT 0';
  END IF;

  EXECUTE IMMEDIATE q'[
    COMMENT ON COLUMN expediente_documento_analizado.nivel IS
    'Nivel visual de documento analizado: 0=principal, 1=hijo relacionado.'
  ]';
END;


DECLARE
  v_count NUMBER;
BEGIN
  SELECT COUNT(1)
    INTO v_count
    FROM user_tab_columns
   WHERE table_name = 'EXPEDIENTE_DOCUMENTO_ANALIZADO'
     AND column_name = 'ORDEN';

  IF v_count = 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE expediente_documento_analizado ADD orden NUMBER DEFAULT 0';
  END IF;

  EXECUTE IMMEDIATE q'[
    COMMENT ON COLUMN expediente_documento_analizado.orden IS
    'Orden visual del documento principal o del documento hijo dentro de su padre.'
  ]';
END;


DECLARE
  v_count NUMBER;
BEGIN
  SELECT COUNT(1)
    INTO v_count
    FROM user_tab_columns
   WHERE table_name = 'EXPEDIENTE_DOCUMENTO_ANALIZADO'
     AND column_name = 'ESTADO_RESPUESTA';

  IF v_count = 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE expediente_documento_analizado ADD estado_respuesta VARCHAR2(40)';
  END IF;

  EXECUTE IMMEDIATE q'[
    COMMENT ON COLUMN expediente_documento_analizado.estado_respuesta IS
    'Estado operativo de respuesta del documento analizado: PENDIENTE, RECIBIDA, SUBSANADA u otro valor de negocio.'
  ]';
END;


DECLARE
  v_count NUMBER;
BEGIN
  SELECT COUNT(1)
    INTO v_count
    FROM user_constraints
   WHERE table_name = 'EXPEDIENTE_DOCUMENTO_ANALIZADO'
     AND constraint_name = 'FK_DOC_ANA_PADRE';

  IF v_count = 0 THEN
    EXECUTE IMMEDIATE q'[
      ALTER TABLE expediente_documento_analizado
      ADD CONSTRAINT fk_doc_ana_padre
      FOREIGN KEY (id_documento_padre)
      REFERENCES expediente_documento_analizado(id_documento_analizado)
    ]';
  END IF;
END;


DECLARE
  v_count NUMBER;
BEGIN
  SELECT COUNT(1)
    INTO v_count
    FROM user_constraints
   WHERE table_name = 'EXPEDIENTE_DOCUMENTO_ANALIZADO'
     AND constraint_name = 'CK_DOC_ANA_NIVEL';

  IF v_count = 0 THEN
    EXECUTE IMMEDIATE q'[
      ALTER TABLE expediente_documento_analizado
      ADD CONSTRAINT ck_doc_ana_nivel
      CHECK (nivel IS NULL OR nivel IN (0, 1))
    ]';
  END IF;
END;


DECLARE
  v_count NUMBER;
BEGIN
  SELECT COUNT(1)
    INTO v_count
    FROM user_indexes
   WHERE index_name = 'IX_DOC_ANA_PADRE';

  IF v_count = 0 THEN
    EXECUTE IMMEDIATE 'CREATE INDEX ix_doc_ana_padre ON expediente_documento_analizado(id_expediente, id_documento_padre, activo, orden)';
  END IF;
END;


SELECT column_name, data_type, data_length, nullable, data_default
  FROM user_tab_columns
 WHERE table_name = 'EXPEDIENTE_DOCUMENTO_ANALIZADO'
   AND column_name IN ('ID_DOCUMENTO_PADRE', 'NIVEL', 'ORDEN', 'ESTADO_RESPUESTA')
 ORDER BY column_id;

SELECT constraint_name, constraint_type, status
  FROM user_constraints
 WHERE table_name = 'EXPEDIENTE_DOCUMENTO_ANALIZADO'
   AND constraint_name IN ('FK_DOC_ANA_PADRE', 'CK_DOC_ANA_NIVEL')
 ORDER BY constraint_name;
