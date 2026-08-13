/* ============================================================
   SCRIPT 77 - Seccion (marcador nombrado) en bloques de plantilla
   Ejecutar conectado como SDRERC_APP.

   Contexto: pedido explicito del usuario. Una misma plantilla base puede
   necesitar mas de un punto de insercion de bloques (por ejemplo
   "Antecedentes" y "Recomendaciones" en informe_rectificacion.docx).
   Se agrega una columna SECCION a PLANTILLA_BLOQUE: si esta vacia, el
   bloque sigue usando el marcador sin nombre [[CONTENIDO]] (compatible
   con lo ya construido); si tiene un valor (ej. "antecedentes"), el
   bloque se inserta solo en el marcador nombrado [[CONTENIDO:antecedentes]]
   de la plantilla base, y un documento puede tener varios marcadores
   nombrados distintos, cada uno con su propio grupo de bloques.

   Idempotente: ALTER TABLE ADD COLUMN protegido por chequeo de
   existencia de la columna.
   ============================================================ */

DECLARE
  v_existe NUMBER;
BEGIN
  SELECT COUNT(*) INTO v_existe FROM user_tab_columns
   WHERE table_name = 'PLANTILLA_BLOQUE' AND column_name = 'SECCION';
  IF v_existe = 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE plantilla_bloque ADD seccion VARCHAR2(80)';
  END IF;
END;


/* ============================================================
   Verificacion posterior
   ============================================================ */

SELECT column_name, data_type, data_length, nullable
  FROM user_tab_columns
 WHERE table_name = 'PLANTILLA_BLOQUE'
 ORDER BY column_id;
