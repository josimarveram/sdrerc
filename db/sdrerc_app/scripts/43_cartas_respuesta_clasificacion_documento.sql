/* ============================================================
   SCRIPT 43 - Soporte de esquema para Bandeja/Panel de Cartas de Respuesta
   Ejecutar conectado como SDRERC_APP.

   Alcance:
   - Agrega columna CLASIFICACION a tipo_documento_adjunto
     (valores: INTERMEDIO, FINAL) para poder identificar
     "cartas intermedias" en la Bandeja de Cartas de Respuesta.
   - Clasifica los tipos de documento ANALISIS_% existentes.
   - Agrega el tipo de documento "Pedido" (iniciativa propia del
     ciudadano, no es carta de respuesta).
   - Asegura el tipo "Carta de Respuesta" (idempotente, por si el
     script 41 no fue ejecutado aun).
   - Agrega estados de documento ATENDIDO y FINALIZADO usados por
     la Bandeja de Cartas de Respuesta.
   - Agrega columna EXISTE_OPOSICION a expediente_documento_analizado
     para el detalle de la carta de respuesta.
   - Idempotente en su totalidad.
   ============================================================ */

DECLARE
  v_count NUMBER;
BEGIN
  SELECT COUNT(1)
    INTO v_count
    FROM user_tab_columns
   WHERE table_name = 'TIPO_DOCUMENTO_ADJUNTO'
     AND column_name = 'CLASIFICACION';

  IF v_count = 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE tipo_documento_adjunto ADD (clasificacion VARCHAR2(20))';
  END IF;

  EXECUTE IMMEDIATE q'[
    COMMENT ON COLUMN tipo_documento_adjunto.clasificacion IS
    'Clasificacion funcional del documento: INTERMEDIO o FINAL. Nulo si no aplica.'
  ]';
END;
/

DECLARE
  v_count NUMBER;
BEGIN
  SELECT COUNT(1)
    INTO v_count
    FROM user_constraints
   WHERE table_name = 'TIPO_DOCUMENTO_ADJUNTO'
     AND constraint_name = 'CK_TIPO_DOC_ADJ_CLASIF';

  IF v_count = 0 THEN
    EXECUTE IMMEDIATE q'[
      ALTER TABLE tipo_documento_adjunto
      ADD CONSTRAINT ck_tipo_doc_adj_clasif
      CHECK (clasificacion IS NULL OR clasificacion IN ('INTERMEDIO', 'FINAL'))
    ]';
  END IF;
END;
/

UPDATE tipo_documento_adjunto
   SET clasificacion = 'INTERMEDIO'
 WHERE UPPER(codigo) IN (
       'ANALISIS_01_CARTA_EDICTO',
       'ANALISIS_02_CARTA_FALTA_SUSTENTO',
       'ANALISIS_03_CARTA_INDAGATORIO',
       'ANALISIS_04_CARTA_PRETENSION',
       'ANALISIS_05_OFICIO_OREC_CANCELACION',
       'ANALISIS_06_OFICIO_OREC_RECONSTITUCION',
       'ANALISIS_10_OFICIO'
       )
   AND (clasificacion IS NULL OR clasificacion <> 'INTERMEDIO');

UPDATE tipo_documento_adjunto
   SET clasificacion = 'FINAL'
 WHERE UPPER(codigo) IN (
       'ANALISIS_07_CARTA_NOTIFICACION_PROCEDENTE',
       'ANALISIS_08_CARTA_NOTIFICACION_IMPROCEDENTE'
       )
   AND (clasificacion IS NULL OR clasificacion <> 'FINAL');

MERGE INTO tipo_documento_adjunto dst
USING (
  SELECT 'ANALISIS_DOC_20_CARTA_RESPUESTA' AS codigo, 'Carta de Respuesta' AS nombre FROM dual
  UNION ALL SELECT 'ANALISIS_DOC_21_PEDIDO', 'Pedido' FROM dual
) src
ON (UPPER(dst.codigo) = src.codigo)
WHEN MATCHED THEN UPDATE
  SET dst.nombre = src.nombre,
      dst.activo = 1,
      dst.modificado_en = SYSTIMESTAMP
WHEN NOT MATCHED THEN
  INSERT (codigo, nombre, activo)
  VALUES (src.codigo, src.nombre, 1);

COMMIT;

MERGE INTO estado_documento dst
USING (
  SELECT 'ATENDIDO' AS codigo, 'Atendido' AS nombre FROM dual
  UNION ALL SELECT 'FINALIZADO', 'Finalizado' FROM dual
) src
ON (UPPER(dst.codigo) = src.codigo)
WHEN MATCHED THEN UPDATE
  SET dst.nombre = src.nombre,
      dst.activo = 1,
      dst.modificado_en = SYSTIMESTAMP
WHEN NOT MATCHED THEN
  INSERT (codigo, nombre, activo)
  VALUES (src.codigo, src.nombre, 1);

COMMIT;

DECLARE
  v_count NUMBER;
BEGIN
  SELECT COUNT(1)
    INTO v_count
    FROM user_tab_columns
   WHERE table_name = 'EXPEDIENTE_DOCUMENTO_ANALIZADO'
     AND column_name = 'EXISTE_OPOSICION';

  IF v_count = 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE expediente_documento_analizado ADD (existe_oposicion NUMBER(1))';
  END IF;

  EXECUTE IMMEDIATE q'[
    COMMENT ON COLUMN expediente_documento_analizado.existe_oposicion IS
    'Indica si existe oposicion sobre la carta de respuesta: 1=Si, 0=No, NULL=No aplica.'
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
     AND constraint_name = 'CK_EXP_DOC_ANA_OPOSICION';

  IF v_count = 0 THEN
    EXECUTE IMMEDIATE q'[
      ALTER TABLE expediente_documento_analizado
      ADD CONSTRAINT ck_exp_doc_ana_oposicion
      CHECK (existe_oposicion IS NULL OR existe_oposicion IN (0, 1))
    ]';
  END IF;
END;
/

SELECT codigo, nombre, clasificacion, activo
  FROM tipo_documento_adjunto
 WHERE codigo LIKE 'ANALISIS_%'
 ORDER BY codigo;

SELECT codigo, nombre, activo
  FROM estado_documento
 WHERE codigo IN ('ATENDIDO', 'FINALIZADO')
 ORDER BY codigo;

SELECT column_name, data_type, data_length, nullable
  FROM user_tab_columns
 WHERE table_name = 'EXPEDIENTE_DOCUMENTO_ANALIZADO'
   AND column_name = 'EXISTE_OPOSICION';
