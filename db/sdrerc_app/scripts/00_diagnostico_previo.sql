/* ============================================================
   SCRIPT 00 - Diagnostico previo
   Ejecutar en DBeaver contra la base legacy solo para lectura.
   No modifica datos ni estructura.
   ============================================================ */

SELECT table_name, tablespace_name, status, num_rows, last_analyzed
FROM user_tables
ORDER BY table_name;

SELECT table_name, column_id, column_name, data_type, data_length,
       data_precision, data_scale, nullable, data_default
FROM user_tab_columns
WHERE table_name IN (
  'EXPEDIENTE',
  'EXPEDIENTE_ASIGNACION',
  'EXPEDIENTE_ANALISIS_ABOGADO',
  'EXPEDIENTE_ANALISIS_ABOGADO_DET_DOC',
  'EXPEDIENTE_OBSERVACION_VERIFICACION',
  'EXPEDIENTE_OBSERVACION_EJECUCION',
  'CATALOGO',
  'CATALOGO_ITEM',
  'APP_USERS',
  'APP_ROLES',
  'APP_USER_ROLES',
  'APP_USER_SUPERVISION',
  'TECNICO',
  'PLAZO_ATENCION_DOCUMENTO'
)
ORDER BY table_name, column_id;

SELECT e.estado,
       ci.descripcion AS estado_descripcion,
       COUNT(*) AS cantidad
FROM expediente e
LEFT JOIN catalogo_item ci ON ci.id_catalogo_item = e.estado
GROUP BY e.estado, ci.descripcion
ORDER BY e.estado;

SELECT c.id_catalogo,
       c.descripcion AS catalogo,
       i.id_catalogo_item,
       i.descripcion AS item,
       i.active
FROM catalogo c
JOIN catalogo_item i ON i.id_catalogo = c.id_catalogo
ORDER BY c.id_catalogo, i.id_catalogo_item;

SELECT id_expediente, num_expediente, numero_tramite_documento, estado
FROM expediente
WHERE estado IS NULL;

SELECT e.id_expediente, e.num_expediente, e.estado
FROM expediente e
WHERE NOT EXISTS (
  SELECT 1
  FROM expediente_asignacion ea
  WHERE ea.id_expediente = e.id_expediente
    AND NVL(ea.active, 1) = 1
);

SELECT id_expediente, etapa_flujo, COUNT(*) AS cantidad
FROM expediente_asignacion
WHERE NVL(active, 1) = 1
GROUP BY id_expediente, etapa_flujo
HAVING COUNT(*) > 1
ORDER BY cantidad DESC, id_expediente, etapa_flujo;

SELECT num_expediente, COUNT(*) AS cantidad
FROM expediente
WHERE num_expediente IS NOT NULL
GROUP BY num_expediente
HAVING COUNT(*) > 1
ORDER BY cantidad DESC, num_expediente;

SELECT numero_acta,
       UPPER(TRIM(apellido_nombre_titular)) AS titular_normalizado,
       COUNT(*) AS cantidad
FROM expediente
WHERE numero_acta IS NOT NULL
  AND apellido_nombre_titular IS NOT NULL
GROUP BY numero_acta, UPPER(TRIM(apellido_nombre_titular))
HAVING COUNT(*) > 1
ORDER BY cantidad DESC, numero_acta;

SELECT 'EXPEDIENTE' AS tabla, COUNT(*) AS cantidad FROM expediente
UNION ALL SELECT 'EXPEDIENTE_ASIGNACION', COUNT(*) FROM expediente_asignacion
UNION ALL SELECT 'EXPEDIENTE_ANALISIS_ABOGADO', COUNT(*) FROM expediente_analisis_abogado
UNION ALL SELECT 'EXPEDIENTE_ANALISIS_ABOGADO_DET_DOC', COUNT(*) FROM expediente_analisis_abogado_det_doc
UNION ALL SELECT 'EXPEDIENTE_OBSERVACION_VERIFICACION', COUNT(*) FROM expediente_observacion_verificacion
UNION ALL SELECT 'EXPEDIENTE_OBSERVACION_EJECUCION', COUNT(*) FROM expediente_observacion_ejecucion
UNION ALL SELECT 'CATALOGO', COUNT(*) FROM catalogo
UNION ALL SELECT 'CATALOGO_ITEM', COUNT(*) FROM catalogo_item
UNION ALL SELECT 'APP_USERS', COUNT(*) FROM app_users
UNION ALL SELECT 'APP_ROLES', COUNT(*) FROM app_roles
UNION ALL SELECT 'APP_USER_ROLES', COUNT(*) FROM app_user_roles
UNION ALL SELECT 'APP_USER_SUPERVISION', COUNT(*) FROM app_user_supervision
UNION ALL SELECT 'TECNICO', COUNT(*) FROM tecnico;

