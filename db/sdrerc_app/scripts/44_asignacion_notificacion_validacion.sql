/* ============================================================
   SCRIPT 44 - Soporte de esquema para Asignación/Validación de Notificación
   Ejecutar conectado como SDRERC_APP.

   Alcance:
   - Agrega estados de documento ASIGNADO y VALIDADO usados por la
     Bandeja de Asignación de Notificación y la Bandeja de Validación.
   - Agrega el equipo "Validación" (EQ_VALIDACION), ya que solo existía
     el equipo "Notificación" (EQ_NOTIFICACION).
   - Agrega columnas a expediente_documento_analizado para registrar
     el destino de la asignación de notificación (equipo, usuario,
     hoja de envío), sin reutilizar las columnas de "carta de
     respuesta" que tienen otro significado funcional.
   - Idempotente en su totalidad.
   ============================================================ */

MERGE INTO estado_documento dst
USING (
  SELECT 'ASIGNADO' AS codigo, 'Asignado' AS nombre FROM dual
  UNION ALL SELECT 'VALIDADO', 'Validado' FROM dual
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

MERGE INTO equipo dst
USING (
  SELECT 'EQ_VALIDACION' AS codigo, 'Validacion' AS nombre FROM dual
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
     AND column_name = 'ID_EQUIPO_NOTIFICACION';

  IF v_count = 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE expediente_documento_analizado ADD (id_equipo_notificacion NUMBER)';
  END IF;
END;


DECLARE
  v_count NUMBER;
BEGIN
  SELECT COUNT(1)
    INTO v_count
    FROM user_tab_columns
   WHERE table_name = 'EXPEDIENTE_DOCUMENTO_ANALIZADO'
     AND column_name = 'ID_USUARIO_NOTIFICACION';

  IF v_count = 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE expediente_documento_analizado ADD (id_usuario_notificacion NUMBER)';
  END IF;
END;


DECLARE
  v_count NUMBER;
BEGIN
  SELECT COUNT(1)
    INTO v_count
    FROM user_tab_columns
   WHERE table_name = 'EXPEDIENTE_DOCUMENTO_ANALIZADO'
     AND column_name = 'NUMERO_HOJA_ENVIO_NOTIFICACION';

  IF v_count = 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE expediente_documento_analizado ADD (numero_hoja_envio_notificacion VARCHAR2(120))';
  END IF;
END;


DECLARE
  v_count NUMBER;
BEGIN
  SELECT COUNT(1)
    INTO v_count
    FROM user_constraints
   WHERE table_name = 'EXPEDIENTE_DOCUMENTO_ANALIZADO'
     AND constraint_name = 'FK_EXP_DOC_ANA_EQ_NOTIF';

  IF v_count = 0 THEN
    EXECUTE IMMEDIATE q'[
      ALTER TABLE expediente_documento_analizado
      ADD CONSTRAINT fk_exp_doc_ana_eq_notif
      FOREIGN KEY (id_equipo_notificacion) REFERENCES equipo (id_equipo)
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
     AND constraint_name = 'FK_EXP_DOC_ANA_USR_NOTIF';

  IF v_count = 0 THEN
    EXECUTE IMMEDIATE q'[
      ALTER TABLE expediente_documento_analizado
      ADD CONSTRAINT fk_exp_doc_ana_usr_notif
      FOREIGN KEY (id_usuario_notificacion) REFERENCES usuario (id_usuario)
    ]';
  END IF;
END;


SELECT codigo, nombre, activo FROM estado_documento WHERE codigo IN ('ASIGNADO', 'VALIDADO') ORDER BY codigo;
SELECT codigo, nombre, activo FROM equipo WHERE codigo IN ('EQ_NOTIFICACION', 'EQ_VALIDACION') ORDER BY codigo;
SELECT column_name FROM user_tab_columns
 WHERE table_name = 'EXPEDIENTE_DOCUMENTO_ANALIZADO'
   AND column_name IN ('ID_EQUIPO_NOTIFICACION', 'ID_USUARIO_NOTIFICACION', 'NUMERO_HOJA_ENVIO_NOTIFICACION')
 ORDER BY column_name;
