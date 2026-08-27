/* ============================================================
   SCRIPT 105 - Solicitante con 4 tipos: agrega MUNICIPALIDAD
   Fase 2 del 4to tipo de solicitante "Municipalidad" (ver
   v3/CLAUDE.md). Requiere los scripts 101 (tipo_solicitante/
   id_centro_atencion/id_unidad_organica en EXPEDIENTE_SOLICITUD) y
   103/104 (tabla MUNICIPALIDADES + datos) ya ejecutados.
   Ejecutar conectado como SDRERC_APP.

   Alcance de esta fase (pedido explícito del usuario: "esta fase es
   solo de estructura... NO incluye todavía la detección automática
   en Carga diaria"): agrega la columna de referencia
   id_municipalidad + el 4to valor válido de tipo_solicitante. NO
   toca la lógica de Carga diaria.

   Deliberadamente aditivo. Idempotente en su totalidad. No se
   ejecuta automáticamente: requiere autorización explícita antes de
   correr contra la base (igual que el resto de scripts).
   ============================================================ */

-- 1) Columna de referencia id_municipalidad -------------------------------
DECLARE
  v_count NUMBER;
BEGIN
  SELECT COUNT(1) INTO v_count FROM user_tab_columns
   WHERE table_name = 'EXPEDIENTE_SOLICITUD' AND column_name = 'ID_MUNICIPALIDAD';
  IF v_count = 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE expediente_solicitud ADD id_municipalidad NUMBER';
    EXECUTE IMMEDIATE 'ALTER TABLE expediente_solicitud ADD CONSTRAINT fk_expsol_municipalidad '
      || 'FOREIGN KEY (id_municipalidad) REFERENCES municipalidades (id_municipalidad)';
  END IF;

  EXECUTE IMMEDIATE q'[
    COMMENT ON COLUMN expediente_solicitud.id_municipalidad IS
    'Solo cuando tipo_solicitante=MUNICIPALIDAD. NULL en cualquier otro caso.'
  ]';
END;


-- 2) Amplía el CHECK de tipo_solicitante para incluir MUNICIPALIDAD -------
--    Idempotente: si el constraint no existe todavía (script 101 no
--    ejecutado), lo crea directo con los 4 valores. Si ya existe con los 3
--    valores anteriores, lo reemplaza (DROP + ADD, Oracle no permite alterar
--    un CHECK existente) solo si 'MUNICIPALIDAD' todavía no está incluido.
DECLARE
  v_search_condition VARCHAR2(4000);
BEGIN
  SELECT search_condition INTO v_search_condition
    FROM user_constraints
   WHERE table_name = 'EXPEDIENTE_SOLICITUD' AND constraint_name = 'CK_EXPSOL_TIPO_SOLICITANTE';

  IF v_search_condition IS NULL OR UPPER(v_search_condition) NOT LIKE '%MUNICIPALIDAD%' THEN
    EXECUTE IMMEDIATE 'ALTER TABLE expediente_solicitud DROP CONSTRAINT ck_expsol_tipo_solicitante';
    EXECUTE IMMEDIATE q'[
      ALTER TABLE expediente_solicitud ADD CONSTRAINT ck_expsol_tipo_solicitante
      CHECK (tipo_solicitante IS NULL OR tipo_solicitante IN
        ('CIUDADANO','CENTRO_ATENCION','UNIDAD_ORGANICA','MUNICIPALIDAD'))
    ]';
  END IF;
EXCEPTION
  WHEN NO_DATA_FOUND THEN
    -- El constraint todavía no existe (script 101 no ejecutado) -> se crea
    -- directo con los 4 valores, sin necesidad de DROP previo.
    EXECUTE IMMEDIATE q'[
      ALTER TABLE expediente_solicitud ADD CONSTRAINT ck_expsol_tipo_solicitante
      CHECK (tipo_solicitante IS NULL OR tipo_solicitante IN
        ('CIUDADANO','CENTRO_ATENCION','UNIDAD_ORGANICA','MUNICIPALIDAD'))
    ]';
END;


-- Verificación
SELECT column_name, data_type, nullable
  FROM user_tab_columns
 WHERE table_name = 'EXPEDIENTE_SOLICITUD'
   AND column_name IN ('TIPO_SOLICITANTE', 'ID_CENTRO_ATENCION', 'ID_UNIDAD_ORGANICA', 'ID_MUNICIPALIDAD')
 ORDER BY column_id;

SELECT constraint_name, search_condition
  FROM user_constraints
 WHERE table_name = 'EXPEDIENTE_SOLICITUD' AND constraint_name = 'CK_EXPSOL_TIPO_SOLICITANTE';
