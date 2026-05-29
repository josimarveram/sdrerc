/* ============================================================
   00. TABLAS DEL ESQUEMA SYSTEM
   ============================================================ */
SELECT table_name,
       tablespace_name,
       status,
       num_rows,
       last_analyzed
FROM user_tables
ORDER BY table_name;


/* ============================================================
   01. POSIBLES TABLAS SDRERC DETECTADAS POR NOMBRE
   ============================================================ */
SELECT table_name,
       tablespace_name,
       status,
       num_rows,
       last_analyzed
FROM user_tables
WHERE table_name IN (
    'EXPEDIENTE',
    'EXPEDIENTE_ASIGNACION',
    'EXPEDIENTE_ANALISIS_ABOGADO',
    'EXPEDIENTE_ANALISIS_ABOGADO_DET_DOC',
    'EXPEDIENTE_OBSERVACION_VERIFICACION',
    'EXPEDIENTE_OBSERVACION_EJECUCION',
    'CATALOGO',
    'CATALOGO_ITEM',
    'PLAZO_ATENCION_DOCUMENTO',
    'APP_USERS',
    'APP_ROLES',
    'APP_USER_ROLES',
    'APP_USER_SUPERVISION',
    'TECNICO',
    'DEPARTAMENTO',
    'PROVINCIA',
    'DISTRITO'
)
ORDER BY table_name;


/* ============================================================
   02. COLUMNAS, TIPOS DE DATOS, NULLABLE Y DEFAULT
   ============================================================ */
SELECT table_name,
       column_id,
       column_name,
       data_type,
       data_length,
       data_precision,
       data_scale,
       nullable,
       data_default
FROM user_tab_columns
WHERE table_name IN (
    SELECT table_name
    FROM user_tables
)
ORDER BY table_name, column_id;


/* ============================================================
   03. COLUMNAS SOLO DE TABLAS SDRERC DETECTADAS
   ============================================================ */
SELECT table_name,
       column_id,
       column_name,
       data_type,
       data_length,
       data_precision,
       data_scale,
       nullable,
       data_default
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
    'PLAZO_ATENCION_DOCUMENTO',
    'APP_USERS',
    'APP_ROLES',
    'APP_USER_ROLES',
    'APP_USER_SUPERVISION',
    'TECNICO',
    'DEPARTAMENTO',
    'PROVINCIA',
    'DISTRITO'
)
ORDER BY table_name, column_id;


/* ============================================================
   04. PRIMARY KEYS
   ============================================================ */
SELECT uc.table_name,
       uc.constraint_name,
       ucc.column_name,
       ucc.position
FROM user_constraints uc
JOIN user_cons_columns ucc
  ON uc.constraint_name = ucc.constraint_name
WHERE uc.constraint_type = 'P'
ORDER BY uc.table_name, uc.constraint_name, ucc.position;


/* ============================================================
   05. FOREIGN KEYS
   ============================================================ */
SELECT fk.table_name              AS child_table,
       fk.constraint_name         AS fk_name,
       fkc.column_name            AS child_column,
       pk.table_name              AS parent_table,
       pk.constraint_name         AS parent_constraint,
       pkc.column_name            AS parent_column,
       fkc.position
FROM user_constraints fk
JOIN user_cons_columns fkc
  ON fk.constraint_name = fkc.constraint_name
JOIN user_constraints pk
  ON fk.r_constraint_name = pk.constraint_name
JOIN user_cons_columns pkc
  ON pk.constraint_name = pkc.constraint_name
 AND fkc.position = pkc.position
WHERE fk.constraint_type = 'R'
ORDER BY fk.table_name, fk.constraint_name, fkc.position;


/* ============================================================
   06. UNIQUE CONSTRAINTS
   ============================================================ */
SELECT uc.table_name,
       uc.constraint_name,
       ucc.column_name,
       ucc.position
FROM user_constraints uc
JOIN user_cons_columns ucc
  ON uc.constraint_name = ucc.constraint_name
WHERE uc.constraint_type = 'U'
ORDER BY uc.table_name, uc.constraint_name, ucc.position;


/* ============================================================
   07. CHECK CONSTRAINTS
   ============================================================ */
SELECT table_name,
       constraint_name,
       search_condition,
       status
FROM user_constraints
WHERE constraint_type = 'C'
ORDER BY table_name, constraint_name;


/* ============================================================
   08. TODOS LOS CONSTRAINTS RESUMIDOS
   ============================================================ */
SELECT uc.table_name,
       uc.constraint_name,
       uc.constraint_type,
       uc.status,
       uc.validated,
       ucc.column_name,
       ucc.position
FROM user_constraints uc
LEFT JOIN user_cons_columns ucc
  ON uc.constraint_name = ucc.constraint_name
ORDER BY uc.table_name, uc.constraint_type, uc.constraint_name, ucc.position;


/* ============================================================
   09. ÍNDICES
   ============================================================ */
SELECT table_name,
       index_name,
       uniqueness,
       status,
       index_type
FROM user_indexes
ORDER BY table_name, index_name;


/* ============================================================
   10. COLUMNAS DE ÍNDICES
   ============================================================ */
SELECT table_name,
       index_name,
       column_name,
       column_position,
       descend
FROM user_ind_columns
ORDER BY table_name, index_name, column_position;


/* ============================================================
   11. SECUENCIAS
   ============================================================ */
SELECT sequence_name,
       min_value,
       max_value,
       increment_by,
       last_number,
       cache_size,
       cycle_flag,
       order_flag
FROM user_sequences
ORDER BY sequence_name;


/* ============================================================
   12. VISTAS
   ============================================================ */
SELECT view_name,
       text_length
FROM user_views
ORDER BY view_name;


/* ============================================================
   13. PROCEDIMIENTOS, FUNCIONES, PAQUETES, TRIGGERS
   ============================================================ */
SELECT object_name,
       object_type,
       status,
       created,
       last_ddl_time
FROM user_objects
WHERE object_type IN (
    'PROCEDURE',
    'FUNCTION',
    'PACKAGE',
    'PACKAGE BODY',
    'TRIGGER',
    'VIEW',
    'SEQUENCE',
    'TABLE'
)
ORDER BY object_type, object_name;


/* ============================================================
   14. TRIGGERS
   ============================================================ */
SELECT trigger_name,
       table_name,
       triggering_event,
       status,
       trigger_type
FROM user_triggers
ORDER BY table_name, trigger_name;


/* ============================================================
   15. DEPENDENCIAS ENTRE OBJETOS
   ============================================================ */
SELECT name,
       type,
       referenced_name,
       referenced_type
FROM user_dependencies
ORDER BY name, referenced_name;


/* ============================================================
   16. CATÁLOGOS ACTUALES
   ============================================================ */
SELECT c.id_catalogo,
       c.descripcion AS catalogo,
       c.active
FROM catalogo c
ORDER BY c.id_catalogo;


/* ============================================================
   17. ÍTEMS DE CATÁLOGO
   ============================================================ */
SELECT c.id_catalogo,
       c.descripcion AS catalogo,
       i.id_catalogo_item,
       i.descripcion AS item,
       i.active
FROM catalogo c
JOIN catalogo_item i
  ON i.id_catalogo = c.id_catalogo
ORDER BY c.id_catalogo, i.id_catalogo_item;


/* ============================================================
   18. ESTADOS ACTUALES USADOS EN EXPEDIENTE
   ============================================================ */
SELECT e.estado,
       ci.descripcion AS estado_descripcion,
       COUNT(*) AS cantidad
FROM expediente e
LEFT JOIN catalogo_item ci
  ON ci.id_catalogo_item = e.estado
GROUP BY e.estado, ci.descripcion
ORDER BY e.estado;


/* ============================================================
   19. EXPEDIENTES POR ESTADO
   ============================================================ */
SELECT NVL(ci.descripcion, 'SIN DESCRIPCION') AS estado,
       e.estado AS id_estado,
       COUNT(*) AS cantidad
FROM expediente e
LEFT JOIN catalogo_item ci
  ON ci.id_catalogo_item = e.estado
GROUP BY ci.descripcion, e.estado
ORDER BY cantidad DESC;


/* ============================================================
   20. EXPEDIENTES SIN ESTADO
   ============================================================ */
SELECT id_expediente,
       num_expediente,
       numero_tramite_documento,
       fecha_solicitud,
       estado
FROM expediente
WHERE estado IS NULL;


/* ============================================================
   21. EXPEDIENTES SIN FECHA DE REGISTRO
   ============================================================ */
SELECT id_expediente,
       num_expediente,
       numero_tramite_documento,
       fecha_solicitud,
       fecha_registra
FROM expediente
WHERE fecha_registra IS NULL;


/* ============================================================
   22. EXPEDIENTES SIN RESPONSABLE ACTUAL
   Nota: en el modelo actual el responsable se infiere desde asignación.
   ============================================================ */
SELECT e.id_expediente,
       e.num_expediente,
       e.estado,
       ci.descripcion AS estado_descripcion
FROM expediente e
LEFT JOIN catalogo_item ci
  ON ci.id_catalogo_item = e.estado
WHERE NOT EXISTS (
    SELECT 1
    FROM expediente_asignacion ea
    WHERE ea.id_expediente = e.id_expediente
      AND NVL(ea.active, 1) = 1
);


/* ============================================================
   23. ASIGNACIONES ACTIVAS
   ============================================================ */
SELECT ea.*
FROM expediente_asignacion ea
WHERE NVL(ea.active, 1) = 1
ORDER BY ea.id_expediente, ea.etapa_flujo, ea.fecha_asignacion;


/* ============================================================
   24. ASIGNACIONES ACTIVAS DUPLICADAS POR EXPEDIENTE Y ETAPA
   ============================================================ */
SELECT id_expediente,
       etapa_flujo,
       COUNT(*) AS cantidad
FROM expediente_asignacion
WHERE NVL(active, 1) = 1
GROUP BY id_expediente, etapa_flujo
HAVING COUNT(*) > 1
ORDER BY cantidad DESC, id_expediente, etapa_flujo;


/* ============================================================
   25. ASIGNACIONES ACTIVAS DUPLICADAS SIN ETAPA_FLUJO
   ============================================================ */
SELECT id_expediente,
       COUNT(*) AS cantidad
FROM expediente_asignacion
WHERE NVL(active, 1) = 1
  AND etapa_flujo IS NULL
GROUP BY id_expediente
HAVING COUNT(*) > 1
ORDER BY cantidad DESC, id_expediente;


/* ============================================================
   26. EXPEDIENTES POR ETAPA_FLUJO SI APLICA
   ============================================================ */
SELECT ea.etapa_flujo,
       ci.descripcion AS etapa_descripcion,
       COUNT(DISTINCT ea.id_expediente) AS cantidad
FROM expediente_asignacion ea
LEFT JOIN catalogo_item ci
  ON ci.id_catalogo_item = ea.etapa_flujo
GROUP BY ea.etapa_flujo, ci.descripcion
ORDER BY ea.etapa_flujo;


/* ============================================================
   27. EXPEDIENTES CON MÁS DE UNA SOLICITUD POR NUM_EXPEDIENTE
   ============================================================ */
SELECT num_expediente,
       COUNT(*) AS cantidad
FROM expediente
WHERE num_expediente IS NOT NULL
GROUP BY num_expediente
HAVING COUNT(*) > 1
ORDER BY cantidad DESC, num_expediente;


/* ============================================================
   28. DUPLICIDAD FUNCIONAL POR NÚMERO DE ACTA Y TITULAR 1
   ============================================================ */
SELECT numero_acta,
       UPPER(TRIM(apellido_nombre_titular)) AS titular_normalizado,
       COUNT(*) AS cantidad
FROM expediente
WHERE numero_acta IS NOT NULL
  AND apellido_nombre_titular IS NOT NULL
GROUP BY numero_acta, UPPER(TRIM(apellido_nombre_titular))
HAVING COUNT(*) > 1
ORDER BY cantidad DESC, numero_acta;


/* ============================================================
   29. DUPLICIDAD FUNCIONAL CON TITULAR 2 EN MATRIMONIO
   ============================================================ */
SELECT numero_acta,
       UPPER(TRIM(apellido_nombre_titular)) AS titular_1,
       UPPER(TRIM(apellido_nombre_titular_2)) AS titular_2,
       COUNT(*) AS cantidad
FROM expediente
WHERE numero_acta IS NOT NULL
  AND (
       apellido_nombre_titular IS NOT NULL
       OR apellido_nombre_titular_2 IS NOT NULL
  )
GROUP BY numero_acta,
         UPPER(TRIM(apellido_nombre_titular)),
         UPPER(TRIM(apellido_nombre_titular_2))
HAVING COUNT(*) > 1
ORDER BY cantidad DESC, numero_acta;


/* ============================================================
   30. EXPEDIENTES SIN NÚMERO DE EXPEDIENTE
   ============================================================ */
SELECT id_expediente,
       num_expediente,
       numero_acta,
       apellido_nombre_titular,
       estado,
       fecha_solicitud
FROM expediente
WHERE num_expediente IS NULL
   OR TRIM(num_expediente) IS NULL
ORDER BY fecha_solicitud, id_expediente;


/* ============================================================
   31. EXPEDIENTES SIN FECHA DE SOLICITUD
   ============================================================ */
SELECT id_expediente,
       num_expediente,
       numero_tramite_documento,
       numero_acta,
       apellido_nombre_titular
FROM expediente
WHERE fecha_solicitud IS NULL;


/* ============================================================
   32. ANÁLISIS POR RESULTADO
   ============================================================ */
SELECT eaa.id_analisis,
       ci.descripcion AS resultado,
       COUNT(*) AS cantidad
FROM expediente_analisis_abogado eaa
LEFT JOIN catalogo_item ci
  ON ci.id_catalogo_item = eaa.id_analisis
GROUP BY eaa.id_analisis, ci.descripcion
ORDER BY eaa.id_analisis;


/* ============================================================
   33. DOCUMENTOS ANALIZADOS POR TIPO
   ============================================================ */
SELECT d.id_tipo_documento_analizado,
       ci.descripcion AS tipo_documento_analizado,
       COUNT(*) AS cantidad
FROM expediente_analisis_abogado_det_doc d
LEFT JOIN catalogo_item ci
  ON ci.id_catalogo_item = d.id_tipo_documento_analizado
GROUP BY d.id_tipo_documento_analizado, ci.descripcion
ORDER BY cantidad DESC;


/* ============================================================
   34. OBSERVACIONES DE VERIFICACIÓN
   ============================================================ */
SELECT tiene_observacion,
       tipo_observacion,
       COUNT(*) AS cantidad
FROM expediente_observacion_verificacion
GROUP BY tiene_observacion, tipo_observacion
ORDER BY tiene_observacion, tipo_observacion;


/* ============================================================
   35. OBSERVACIONES DE EJECUCIÓN
   ============================================================ */
SELECT id_estado_ejecucion,
       tiene_observacion,
       COUNT(*) AS cantidad
FROM expediente_observacion_ejecucion
GROUP BY id_estado_ejecucion, tiene_observacion
ORDER BY id_estado_ejecucion, tiene_observacion;


/* ============================================================
   36. USUARIOS Y ROLES
   ============================================================ */
SELECT u.user_id,
       u.username,
       u.full_name,
       u.status,
       u.id_tecnico,
       LISTAGG(r.role_name, ', ') WITHIN GROUP (ORDER BY r.role_name) AS roles
FROM app_users u
LEFT JOIN app_user_roles ur
  ON ur.user_id = u.user_id
LEFT JOIN app_roles r
  ON r.role_id = ur.role_id
GROUP BY u.user_id, u.username, u.full_name, u.status, u.id_tecnico
ORDER BY u.user_id;


/* ============================================================
   37. ROLES ACTUALES
   ============================================================ */
SELECT role_id,
       role_name,
       description,
       status
FROM app_roles
ORDER BY role_id;


/* ============================================================
   38. EQUIPO JURÍDICO / TÉCNICOS
   ============================================================ */
SELECT t.id_tecnico,
       t.apellido_paterno,
       t.apellido_materno,
       t.nombres,
       t.numero_documento,
       t.id_tipo_personal,
       ci.descripcion AS tipo_personal,
       t.active
FROM tecnico t
LEFT JOIN catalogo_item ci
  ON ci.id_catalogo_item = t.id_tipo_personal
ORDER BY t.id_tecnico;


/* ============================================================
   39. RELACIÓN SUPERVISOR - ABOGADO
   ============================================================ */
SELECT s.supervisor_id,
       sup.username AS supervisor_usuario,
       sup.full_name AS supervisor_nombre,
       s.abogado_id,
       abo.username AS abogado_usuario,
       abo.full_name AS abogado_nombre
FROM app_user_supervision s
LEFT JOIN app_users sup
  ON sup.user_id = s.supervisor_id
LEFT JOIN app_users abo
  ON abo.user_id = s.abogado_id
ORDER BY supervisor_nombre, abogado_nombre;


/* ============================================================
   40. PLAZOS CONFIGURADOS
   ============================================================ */
SELECT p.*
FROM plazo_atencion_documento p
ORDER BY p.id_tipo_documento;


/* ============================================================
   41. CONTEO DE REGISTROS POR TABLA SDRERC
   Ejecutar como bloque de consultas individuales si DBeaver no permite todo junto.
   ============================================================ */
SELECT 'EXPEDIENTE' AS tabla, COUNT(*) AS cantidad FROM expediente
UNION ALL SELECT 'EXPEDIENTE_ASIGNACION', COUNT(*) FROM expediente_asignacion
UNION ALL SELECT 'EXPEDIENTE_ANALISIS_ABOGADO', COUNT(*) FROM expediente_analisis_abogado
UNION ALL SELECT 'EXPEDIENTE_ANALISIS_ABOGADO_DET_DOC', COUNT(*) FROM expediente_analisis_abogado_det_doc
UNION ALL SELECT 'EXPEDIENTE_OBSERVACION_VERIFICACION', COUNT(*) FROM expediente_observacion_verificacion
UNION ALL SELECT 'EXPEDIENTE_OBSERVACION_EJECUCION', COUNT(*) FROM expediente_observacion_ejecucion
UNION ALL SELECT 'CATALOGO', COUNT(*) FROM catalogo
UNION ALL SELECT 'CATALOGO_ITEM', COUNT(*) FROM catalogo_item
UNION ALL SELECT 'PLAZO_ATENCION_DOCUMENTO', COUNT(*) FROM plazo_atencion_documento
UNION ALL SELECT 'APP_USERS', COUNT(*) FROM app_users
UNION ALL SELECT 'APP_ROLES', COUNT(*) FROM app_roles
UNION ALL SELECT 'APP_USER_ROLES', COUNT(*) FROM app_user_roles
UNION ALL SELECT 'APP_USER_SUPERVISION', COUNT(*) FROM app_user_supervision
UNION ALL SELECT 'TECNICO', COUNT(*) FROM tecnico
UNION ALL SELECT 'DEPARTAMENTO', COUNT(*) FROM departamento
UNION ALL SELECT 'PROVINCIA', COUNT(*) FROM provincia
UNION ALL SELECT 'DISTRITO', COUNT(*) FROM distrito;


/* ============================================================
   42. OBJETOS SDRERC DENTRO DE SYSTEM POR PATRÓN DE NOMBRE
   ============================================================ */
SELECT object_name,
       object_type,
       status,
       created,
       last_ddl_time
FROM user_objects
WHERE object_name LIKE '%EXPEDIENTE%'
   OR object_name LIKE '%CATALOGO%'
   OR object_name LIKE '%USUARIO%'
   OR object_name LIKE '%USER%'
   OR object_name LIKE '%ROLE%'
   OR object_name LIKE '%TECNICO%'
   OR object_name LIKE '%PLAZO%'
   OR object_name LIKE '%DEPARTAMENTO%'
   OR object_name LIKE '%PROVINCIA%'
   OR object_name LIKE '%DISTRITO%'
ORDER BY object_type, object_name;


/* ============================================================
   43. TABLAS SIN PRIMARY KEY
   ============================================================ */
SELECT t.table_name
FROM user_tables t
WHERE NOT EXISTS (
    SELECT 1
    FROM user_constraints c
    WHERE c.table_name = t.table_name
      AND c.constraint_type = 'P'
)
ORDER BY t.table_name;


/* ============================================================
   44. TABLAS SIN FOREIGN KEYS SALIENTES
   ============================================================ */
SELECT t.table_name
FROM user_tables t
WHERE NOT EXISTS (
    SELECT 1
    FROM user_constraints c
    WHERE c.table_name = t.table_name
      AND c.constraint_type = 'R'
)
ORDER BY t.table_name;


/* ============================================================
   45. TABLAS REFERENCIADAS POR FOREIGN KEYS
   ============================================================ */


SELECT fk.table_name AS child_table,
       fk.constraint_name AS fk_name,
       pk.table_name AS referenced_table,
       pk.constraint_name AS referenced_pk
FROM user_constraints fk
JOIN user_constraints pk
  ON pk.constraint_name = fk.r_constraint_name
WHERE fk.constraint_type = 'R'
ORDER BY fk.table_name, fk.constraint_name;