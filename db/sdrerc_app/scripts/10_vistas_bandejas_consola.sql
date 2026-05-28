/* ============================================================
   SCRIPT 10 - Vistas para bandejas y consola
   Ejecutar conectado como SDRERC_APP.
   ============================================================ */

CREATE OR REPLACE VIEW vw_expediente_bandeja AS
SELECT e.id_expediente,
       e.numero_expediente,
       e.numero_tramite_documentario,
       et.codigo AS etapa_codigo,
       es.codigo AS estado_codigo,
       ua.nombre_completo AS abogado_inicial,
       ur.nombre_completo AS responsable_actual,
       eq.nombre AS equipo_actual,
       e.fecha_registro,
       e.fecha_ultimo_movimiento,
       e.fecha_vencimiento,
       e.requiere_publicacion,
       e.expediente_digital_completo
FROM expediente e
JOIN etapa_expediente et ON et.id_etapa = e.id_etapa_actual
JOIN estado_expediente es ON es.id_estado = e.id_estado_actual
LEFT JOIN usuario ua ON ua.id_usuario = e.id_usuario_abogado_inicial
LEFT JOIN usuario ur ON ur.id_usuario = e.id_usuario_responsable_actual
LEFT JOIN equipo eq ON eq.id_equipo = e.id_equipo_responsable_actual
WHERE e.activo = 1;

CREATE OR REPLACE VIEW vw_bandeja_registro AS
SELECT * FROM vw_expediente_bandeja WHERE etapa_codigo = 'REGISTRO';

CREATE OR REPLACE VIEW vw_bandeja_asignacion AS
SELECT * FROM vw_expediente_bandeja WHERE etapa_codigo = 'ASIGNACION';

CREATE OR REPLACE VIEW vw_bandeja_abogado_analisis AS
SELECT * FROM vw_expediente_bandeja WHERE etapa_codigo = 'ANALISIS';

CREATE OR REPLACE VIEW vw_bandeja_verificacion AS
SELECT * FROM vw_expediente_bandeja WHERE etapa_codigo = 'VERIFICACION';

CREATE OR REPLACE VIEW vw_bandeja_firma_emision AS
SELECT * FROM vw_expediente_bandeja WHERE etapa_codigo = 'FIRMA_EMISION';

CREATE OR REPLACE VIEW vw_bandeja_ejecucion AS
SELECT * FROM vw_expediente_bandeja WHERE etapa_codigo = 'EJECUCION';

CREATE OR REPLACE VIEW vw_bandeja_notificacion AS
SELECT * FROM vw_expediente_bandeja WHERE etapa_codigo = 'NOTIFICACION';

CREATE OR REPLACE VIEW vw_bandeja_cargos_acuse AS
SELECT ca.id_expediente_cargo_acuse,
       ca.id_expediente,
       e.numero_expediente,
       n.numero_intento,
       tn.codigo AS tipo_notificacion,
       ec.codigo AS estado_cargo,
       ca.fecha_recepcion,
       ca.recibido_por,
       ca.observacion
FROM expediente_cargo_acuse ca
JOIN expediente e ON e.id_expediente = ca.id_expediente
JOIN expediente_notificacion n ON n.id_expediente_notificacion = ca.id_expediente_notificacion
JOIN tipo_notificacion tn ON tn.id_tipo_notificacion = n.id_tipo_notificacion
JOIN estado_cargo_acuse ec ON ec.id_estado_cargo_acuse = ca.id_estado_cargo_acuse
WHERE ca.activo = 1;

CREATE OR REPLACE VIEW vw_bandeja_publicacion AS
SELECT *
FROM vw_expediente_bandeja
WHERE requiere_publicacion = 1
   OR estado_codigo = 'REQUIERE_PUBLICACION';

CREATE OR REPLACE VIEW vw_bandeja_expediente_digital AS
SELECT e.id_expediente,
       e.numero_expediente,
       d.codigo_expediente_digital,
       d.ruta_carpeta,
       d.enlace_carpeta,
       d.documentos_cargados,
       d.completo,
       ur.nombre_completo AS responsable,
       uc.nombre_completo AS custodio
FROM expediente e
LEFT JOIN expediente_digital d ON d.id_expediente = e.id_expediente AND d.activo = 1
LEFT JOIN usuario ur ON ur.id_usuario = d.id_usuario_responsable
LEFT JOIN usuario uc ON uc.id_usuario = d.id_usuario_custodio
WHERE e.activo = 1;

CREATE OR REPLACE VIEW vw_bandeja_cierre_archivo AS
SELECT * FROM vw_expediente_bandeja WHERE etapa_codigo = 'CIERRE_ARCHIVO';

CREATE OR REPLACE VIEW vw_expediente_consola AS
SELECT b.*,
       (SELECT COUNT(*) FROM expediente_documento d WHERE d.id_expediente = b.id_expediente AND d.activo = 1) AS total_documentos,
       (SELECT COUNT(*) FROM expediente_observacion o WHERE o.id_expediente = b.id_expediente AND o.subsanada = 0 AND o.activo = 1) AS observaciones_pendientes,
       (SELECT COUNT(*) FROM expediente_notificacion n WHERE n.id_expediente = b.id_expediente AND n.activo = 1) AS total_notificaciones,
       (SELECT COUNT(*) FROM expediente_cargo_acuse c WHERE c.id_expediente = b.id_expediente AND c.activo = 1) AS total_cargos
FROM vw_expediente_bandeja b;

CREATE OR REPLACE VIEW vw_expediente_timeline AS
SELECT h.id_expediente_historial,
       h.id_expediente,
       h.fecha_movimiento,
       tm.codigo AS movimiento,
       eo.codigo AS etapa_origen,
       ed.codigo AS etapa_destino,
       so.codigo AS estado_origen,
       sd.codigo AS estado_destino,
       uo.nombre_completo AS usuario_origen,
       ud.nombre_completo AS usuario_destino,
       h.comentario,
       h.motivo
FROM expediente_historial h
JOIN tipo_movimiento tm ON tm.id_tipo_movimiento = h.id_tipo_movimiento
LEFT JOIN etapa_expediente eo ON eo.id_etapa = h.id_etapa_origen
LEFT JOIN etapa_expediente ed ON ed.id_etapa = h.id_etapa_destino
LEFT JOIN estado_expediente so ON so.id_estado = h.id_estado_origen
LEFT JOIN estado_expediente sd ON sd.id_estado = h.id_estado_destino
LEFT JOIN usuario uo ON uo.id_usuario = h.id_usuario_origen
LEFT JOIN usuario ud ON ud.id_usuario = h.id_usuario_destino
WHERE h.activo = 1;

CREATE OR REPLACE VIEW vw_expediente_documentos AS
SELECT * FROM expediente_documento WHERE activo = 1;

CREATE OR REPLACE VIEW vw_expediente_documentos_analizados AS
SELECT * FROM expediente_documento_analizado WHERE activo = 1;

CREATE OR REPLACE VIEW vw_expediente_personas AS
SELECT ep.id_expediente, ep.tipo_relacion_persona, p.*
FROM expediente_persona ep
JOIN persona p ON p.id_persona = ep.id_persona
WHERE ep.activo = 1;

CREATE OR REPLACE VIEW vw_expediente_evaluaciones AS
SELECT * FROM expediente_evaluacion WHERE activo = 1;

CREATE OR REPLACE VIEW vw_expediente_resoluciones AS
SELECT * FROM expediente_resolucion WHERE activo = 1;

CREATE OR REPLACE VIEW vw_expediente_notificaciones AS
SELECT * FROM expediente_notificacion WHERE activo = 1;

CREATE OR REPLACE VIEW vw_expediente_cargos_acuse AS
SELECT * FROM expediente_cargo_acuse WHERE activo = 1;

CREATE OR REPLACE VIEW vw_expediente_digital AS
SELECT * FROM expediente_digital WHERE activo = 1;

CREATE OR REPLACE VIEW vw_expediente_acciones_permitidas AS
SELECT e.id_expediente,
       ft.codigo_accion,
       ft.nombre_accion,
       ft.requiere_comentario,
       ft.requiere_documento
FROM expediente e
JOIN flujo f ON f.codigo = 'SDRERC_TO_BE' AND f.activo = 1
JOIN flujo_transicion ft
  ON ft.id_flujo = f.id_flujo
 AND (ft.id_etapa_origen = e.id_etapa_actual OR ft.id_etapa_origen IS NULL)
 AND (ft.id_estado_origen = e.id_estado_actual OR ft.id_estado_origen IS NULL)
WHERE e.activo = 1
  AND ft.activo = 1;

