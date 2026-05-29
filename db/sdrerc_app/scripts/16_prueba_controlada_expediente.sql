/* ============================================================
   SCRIPT 16 - Prueba controlada de expediente ficticio
   Ejecutar conectado como SDRERC_APP.
   No modifica Java ni borra datos.
   Idempotente: inserta solo si no existen las claves de prueba.
   ============================================================ */

/* Usuario controlado para asignacion inicial. */
INSERT INTO usuario (
  username, nombre_completo, tipo_documento, numero_documento, correo, estado, activo
)
SELECT 'USR_PRUEBA_SDRERC',
       'Usuario Prueba SDRERC',
       'DNI',
       '99999990',
       'prueba.sdrerc@reniec.gob.pe',
       'ACTIVO',
       1
FROM dual
WHERE NOT EXISTS (
  SELECT 1
  FROM usuario
  WHERE username = 'USR_PRUEBA_SDRERC'
);

INSERT INTO usuario_rol (id_usuario, id_rol, activo)
SELECT u.id_usuario, r.id_rol, 1
FROM usuario u
JOIN rol r ON r.codigo = 'ABOGADO'
WHERE u.username = 'USR_PRUEBA_SDRERC'
  AND NOT EXISTS (
    SELECT 1
    FROM usuario_rol ur
    WHERE ur.id_usuario = u.id_usuario
      AND ur.id_rol = r.id_rol
      AND ur.activo = 1
  );

INSERT INTO equipo_usuario (id_equipo, id_usuario, es_responsable, activo)
SELECT eq.id_equipo, u.id_usuario, 1, 1
FROM equipo eq
JOIN usuario u ON u.username = 'USR_PRUEBA_SDRERC'
WHERE eq.codigo = 'EQ_ASIGNACION'
  AND NOT EXISTS (
    SELECT 1
    FROM equipo_usuario eu
    WHERE eu.id_equipo = eq.id_equipo
      AND eu.id_usuario = u.id_usuario
      AND eu.activo = 1
  );

/* Persona solicitante ficticia. */
INSERT INTO persona (
  tipo_documento, numero_documento, nombres, apellidos,
  correo_electronico, telefono, direccion, activo
)
SELECT 'DNI',
       '99999991',
       'Solicitante',
       'Prueba Controlada',
       'solicitante.prueba@reniec.gob.pe',
       '999999999',
       'Direccion ficticia para prueba controlada SDRERC',
       1
FROM dual
WHERE NOT EXISTS (
  SELECT 1
  FROM persona
  WHERE tipo_documento = 'DNI'
    AND numero_documento = '99999991'
);

/* Expediente ficticio: estado inicial ASIGNACION / ASIGNADO. */
INSERT INTO expediente (
  numero_expediente,
  numero_tramite_documentario,
  id_etapa_actual,
  id_estado_actual,
  id_usuario_responsable_actual,
  id_usuario_abogado_inicial,
  id_equipo_responsable_actual,
  fecha_registro,
  fecha_ultimo_movimiento,
  fecha_vencimiento,
  prioridad,
  requiere_publicacion,
  expediente_digital_completo,
  archivado,
  cerrado,
  activo
)
SELECT 'EXP-PRUEBA-SDRERC-0001',
       'TD-PRUEBA-SDRERC-0001',
       et.id_etapa,
       es.id_estado,
       u.id_usuario,
       u.id_usuario,
       eq.id_equipo,
       SYSTIMESTAMP,
       SYSTIMESTAMP,
       TRUNC(SYSDATE) + 30,
       'NORMAL',
       0,
       0,
       0,
       0,
       1
FROM etapa_expediente et
JOIN estado_expediente es ON es.codigo = 'ASIGNADO'
JOIN usuario u ON u.username = 'USR_PRUEBA_SDRERC'
JOIN equipo eq ON eq.codigo = 'EQ_ASIGNACION'
WHERE et.codigo = 'ASIGNACION'
  AND NOT EXISTS (
    SELECT 1
    FROM expediente e
    WHERE e.numero_expediente = 'EXP-PRUEBA-SDRERC-0001'
  );

/* Solicitud asociada. */
INSERT INTO expediente_solicitud (
  id_expediente,
  id_entidad_origen,
  id_canal_recepcion,
  id_persona_solicitante,
  numero_tramite_documentario,
  fecha_recepcion,
  asunto,
  observacion,
  es_tramite_virtual,
  correo_electronico,
  potencial_duplicado,
  activo
)
SELECT e.id_expediente,
       ent.id_entidad_externa,
       c.id_canal_recepcion,
       p.id_persona,
       e.numero_tramite_documentario,
       TRUNC(SYSDATE),
       'Solicitud ficticia para prueba controlada de bandejas y acciones',
       'Registro generado por script 16_prueba_controlada_expediente.sql',
       0,
       p.correo_electronico,
       0,
       1
FROM expediente e
JOIN entidad_externa ent ON ent.codigo = 'CIUDADANO_ENTIDAD'
JOIN canal_recepcion c ON c.codigo = 'MESA_PARTES'
JOIN persona p ON p.tipo_documento = 'DNI' AND p.numero_documento = '99999991'
WHERE e.numero_expediente = 'EXP-PRUEBA-SDRERC-0001'
  AND NOT EXISTS (
    SELECT 1
    FROM expediente_solicitud s
    WHERE s.id_expediente = e.id_expediente
      AND s.numero_tramite_documentario = 'TD-PRUEBA-SDRERC-0001'
  );

/* Acta ficticia asociada. id_tipo_acta es opcional en el modelo. */
INSERT INTO expediente_acta (
  id_expediente,
  id_tipo_acta,
  numero_acta,
  anio_acta,
  oficina_registral,
  libro,
  folio,
  activo
)
SELECT e.id_expediente,
       NULL,
       'ACTA-PRUEBA-0001',
       EXTRACT(YEAR FROM SYSDATE),
       'OFICINA REGISTRAL DE PRUEBA',
       'LIBRO-PRUEBA',
       'FOLIO-PRUEBA',
       1
FROM expediente e
WHERE e.numero_expediente = 'EXP-PRUEBA-SDRERC-0001'
  AND NOT EXISTS (
    SELECT 1
    FROM expediente_acta a
    WHERE a.id_expediente = e.id_expediente
      AND a.numero_acta = 'ACTA-PRUEBA-0001'
  );

/* Relacion expediente-persona. */
INSERT INTO expediente_persona (
  id_expediente,
  id_persona,
  tipo_relacion_persona,
  activo
)
SELECT e.id_expediente,
       p.id_persona,
       'SOLICITANTE',
       1
FROM expediente e
JOIN persona p ON p.tipo_documento = 'DNI' AND p.numero_documento = '99999991'
WHERE e.numero_expediente = 'EXP-PRUEBA-SDRERC-0001'
  AND NOT EXISTS (
    SELECT 1
    FROM expediente_persona ep
    WHERE ep.id_expediente = e.id_expediente
      AND ep.id_persona = p.id_persona
      AND ep.tipo_relacion_persona = 'SOLICITANTE'
      AND ep.activo = 1
  );

/* Asignacion inicial. */
INSERT INTO expediente_asignacion (
  id_expediente,
  id_usuario_asignado,
  id_equipo_asignado,
  id_etapa,
  fecha_asignacion,
  fecha_recepcion,
  activa,
  es_abogado_principal,
  es_reasignacion_excepcional,
  motivo,
  activo
)
SELECT e.id_expediente,
       u.id_usuario,
       eq.id_equipo,
       et.id_etapa,
       SYSTIMESTAMP,
       NULL,
       1,
       1,
       0,
       'Asignacion inicial de prueba controlada',
       1
FROM expediente e
JOIN usuario u ON u.username = 'USR_PRUEBA_SDRERC'
JOIN equipo eq ON eq.codigo = 'EQ_ASIGNACION'
JOIN etapa_expediente et ON et.codigo = 'ASIGNACION'
WHERE e.numero_expediente = 'EXP-PRUEBA-SDRERC-0001'
  AND NOT EXISTS (
    SELECT 1
    FROM expediente_asignacion ea
    WHERE ea.id_expediente = e.id_expediente
      AND ea.id_usuario_asignado = u.id_usuario
      AND ea.id_etapa = et.id_etapa
      AND ea.activa = 1
      AND ea.activo = 1
  );

/* Historial inicial: REGISTRO / REGISTRADO -> ASIGNACION / ASIGNADO. */
INSERT INTO expediente_historial (
  id_expediente,
  id_tipo_movimiento,
  fecha_movimiento,
  id_etapa_origen,
  id_estado_origen,
  id_etapa_destino,
  id_estado_destino,
  id_usuario_destino,
  id_equipo_destino,
  tabla_relacionada,
  id_registro_relacionado,
  comentario,
  motivo,
  activo
)
SELECT e.id_expediente,
       tm.id_tipo_movimiento,
       SYSTIMESTAMP,
       et_o.id_etapa,
       es_o.id_estado,
       et_d.id_etapa,
       es_d.id_estado,
       u.id_usuario,
       eq.id_equipo,
       'EXPEDIENTE',
       e.id_expediente,
       'Creacion y asignacion inicial de expediente ficticio para prueba controlada',
       'PRUEBA_CONTROLADA',
       1
FROM expediente e
JOIN tipo_movimiento tm ON tm.codigo = 'ASIGNACION_ABOGADO'
JOIN etapa_expediente et_o ON et_o.codigo = 'REGISTRO'
JOIN estado_expediente es_o ON es_o.codigo = 'REGISTRADO'
JOIN etapa_expediente et_d ON et_d.codigo = 'ASIGNACION'
JOIN estado_expediente es_d ON es_d.codigo = 'ASIGNADO'
JOIN usuario u ON u.username = 'USR_PRUEBA_SDRERC'
JOIN equipo eq ON eq.codigo = 'EQ_ASIGNACION'
WHERE e.numero_expediente = 'EXP-PRUEBA-SDRERC-0001'
  AND NOT EXISTS (
    SELECT 1
    FROM expediente_historial h
    WHERE h.id_expediente = e.id_expediente
      AND h.id_tipo_movimiento = tm.id_tipo_movimiento
      AND h.id_estado_destino = es_d.id_estado
      AND h.motivo = 'PRUEBA_CONTROLADA'
      AND h.activo = 1
  );

/* Documento ficticio opcional para validar consola/documentos. */
INSERT INTO expediente_documento (
  id_expediente,
  id_tipo_documento_adjunto,
  id_estado_documento,
  nombre_documento,
  numero_documento,
  ruta_archivo,
  hash_archivo,
  fecha_documento,
  activo
)
SELECT e.id_expediente,
       NULL,
       ed.id_estado_documento,
       'Documento de prueba controlada.pdf',
       'DOC-PRUEBA-0001',
       '/tmp/sdrerc/prueba/documento-prueba-controlada.pdf',
       'HASH_PRUEBA_CONTROLADA_0001',
       TRUNC(SYSDATE),
       1
FROM expediente e
JOIN estado_documento ed ON ed.codigo = 'EN_PROYECTO'
WHERE e.numero_expediente = 'EXP-PRUEBA-SDRERC-0001'
  AND NOT EXISTS (
    SELECT 1
    FROM expediente_documento d
    WHERE d.id_expediente = e.id_expediente
      AND d.numero_documento = 'DOC-PRUEBA-0001'
      AND d.activo = 1
  );

/* Evaluacion inicial opcional para validar vistas de consola. */
INSERT INTO expediente_evaluacion (
  id_expediente,
  id_tipo_resultado_evaluacion,
  corresponde,
  incorporado,
  requiere_reconstitucion,
  tiene_legitimidad,
  cumple_medios_probatorios,
  fundamento,
  fecha_evaluacion,
  activo
)
SELECT e.id_expediente,
       tre.id_tipo_resultado_evaluacion,
       1,
       0,
       0,
       1,
       1,
       'Evaluacion ficticia inicial para prueba controlada',
       SYSTIMESTAMP,
       1
FROM expediente e
JOIN tipo_resultado_evaluacion tre ON tre.codigo = 'PROCEDENTE'
WHERE e.numero_expediente = 'EXP-PRUEBA-SDRERC-0001'
  AND NOT EXISTS (
    SELECT 1
    FROM expediente_evaluacion ev
    WHERE ev.id_expediente = e.id_expediente
      AND ev.fundamento = 'Evaluacion ficticia inicial para prueba controlada'
      AND ev.activo = 1
  );

COMMIT;

/* ============================================================
   Validaciones de prueba controlada
   ============================================================ */

SELECT *
FROM vw_expediente_bandeja
WHERE numero_expediente = 'EXP-PRUEBA-SDRERC-0001';

SELECT *
FROM vw_expediente_consola
WHERE numero_expediente = 'EXP-PRUEBA-SDRERC-0001';

SELECT *
FROM vw_expediente_timeline
WHERE id_expediente = (
  SELECT MAX(id_expediente)
  FROM expediente
  WHERE numero_expediente = 'EXP-PRUEBA-SDRERC-0001'
)
ORDER BY fecha_movimiento;

SELECT *
FROM vw_expediente_acciones_permitidas
WHERE id_expediente = (
  SELECT MAX(id_expediente)
  FROM expediente
  WHERE numero_expediente = 'EXP-PRUEBA-SDRERC-0001'
)
ORDER BY codigo_accion;
