/* ============================================================
   SCRIPT 45 - Soporte de esquema para intentos de notificación por documento
   Ejecutar conectado como SDRERC_APP.

   Alcance:
   - Vincula expediente_notificacion a un documento específico
     (expediente_documento_analizado), ya que la Bandeja de
     Notificación es por documento y un expediente puede tener
     más de un documento en proceso de notificación.
   - Agrega columna para el código de notificación (seguimiento).
   - Idempotente en su totalidad.
   ============================================================ */

DECLARE
  v_count NUMBER;
BEGIN
  SELECT COUNT(1)
    INTO v_count
    FROM user_tab_columns
   WHERE table_name = 'EXPEDIENTE_NOTIFICACION'
     AND column_name = 'ID_DOCUMENTO_ANALIZADO';

  IF v_count = 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE expediente_notificacion ADD (id_documento_analizado NUMBER)';
  END IF;
END;


DECLARE
  v_count NUMBER;
BEGIN
  SELECT COUNT(1)
    INTO v_count
    FROM user_tab_columns
   WHERE table_name = 'EXPEDIENTE_NOTIFICACION'
     AND column_name = 'CODIGO_NOTIFICACION';

  IF v_count = 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE expediente_notificacion ADD (codigo_notificacion VARCHAR2(60))';
  END IF;
END;


DECLARE
  v_count NUMBER;
BEGIN
  SELECT COUNT(1)
    INTO v_count
    FROM user_constraints
   WHERE table_name = 'EXPEDIENTE_NOTIFICACION'
     AND constraint_name = 'FK_EXP_NOTIF_DOC_ANALIZADO';

  IF v_count = 0 THEN
    EXECUTE IMMEDIATE q'[
      ALTER TABLE expediente_notificacion
      ADD CONSTRAINT fk_exp_notif_doc_analizado
      FOREIGN KEY (id_documento_analizado) REFERENCES expediente_documento_analizado (id_documento_analizado)
    ]';
  END IF;
END;


SELECT column_name FROM user_tab_columns
 WHERE table_name = 'EXPEDIENTE_NOTIFICACION'
   AND column_name IN ('ID_DOCUMENTO_ANALIZADO', 'CODIGO_NOTIFICACION')
 ORDER BY column_name;
