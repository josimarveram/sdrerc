/* ============================================================
   SCRIPT 107 - Columna GLOSA en UNIDADES_ORGANICAS
   Documenta la columna GLOSA (siglas de la dirección/sub dirección,
   ej. "SDPRC" para SUB DIRECCIÓN DE PROCESAMIENTO DE REGISTROS
   CIVILES) que el usuario ya agregó manualmente a la tabla en Oracle
   y ya pobló con los valores por fila. Este script solo deja la
   estructura reflejada en el repo (patrón del proyecto: toda columna
   real en BD debe tener su script correspondiente) - NO inserta ni
   modifica datos, porque los valores de GLOSA por unidad ya fueron
   cargados manualmente y no deben sobrescribirse sin autorización.

   Habilita que Carga diaria detecte la Unidad Orgánica también
   cuando "SOLICITADO POR" trae las siglas en vez del nombre completo
   (ej. "SDPRC/INVESTIGACION DE ACTAS REGISTRALES - Nombre del Jefe
   Responsable"), comparando contra GLOSA además de NOMBRE
   (CargaDiariaReglasService.clasificarTipoSolicitante).

   Ejecutar conectado como SDRERC_APP. Idempotente. No se ejecuta
   automáticamente: requiere autorización explícita.
   ============================================================ */

DECLARE
  v_count NUMBER;
BEGIN
  SELECT COUNT(1) INTO v_count FROM user_tab_columns
   WHERE table_name = 'UNIDADES_ORGANICAS' AND column_name = 'GLOSA';
  IF v_count = 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE unidades_organicas ADD glosa VARCHAR2(20)';
  END IF;

  EXECUTE IMMEDIATE q'[
    COMMENT ON COLUMN unidades_organicas.glosa IS
    'Siglas de la dirección/sub dirección (ej. SDPRC). Usada por Carga diaria para detectar la '
    || 'unidad cuando "SOLICITADO POR" trae las siglas en vez del nombre completo.'
  ]';
END;


-- Verificación
SELECT id_unidad_organica, nombre, glosa
  FROM unidades_organicas
 WHERE activo = 1
 ORDER BY nombre;
